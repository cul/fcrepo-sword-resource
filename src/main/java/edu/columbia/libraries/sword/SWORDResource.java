package edu.columbia.libraries.sword;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.apache.commons.httpclient.HttpStatus;
import org.fcrepo.server.Context;
import org.fcrepo.server.ReadOnlyContext;
import org.fcrepo.server.Server;
import org.fcrepo.server.access.Access;
import org.fcrepo.server.access.RepositoryInfo;
import org.fcrepo.server.errors.InitializationException;
import org.fcrepo.server.errors.ServerException;
import org.fcrepo.server.resourceIndex.ResourceIndex;
import org.fcrepo.server.rest.BaseRestResource;
import org.fcrepo.server.security.Authorization;
import org.fcrepo.server.storage.DOManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;

import edu.columbia.libraries.sword.DepositHandler;
import edu.columbia.libraries.sword.impl.AtomEntryRequest;
import edu.columbia.libraries.sword.impl.DepositRequest;
import edu.columbia.libraries.sword.impl.ServiceDocumentRequest;
import edu.columbia.libraries.sword.impl.fcrepo.FedoraService;
import edu.columbia.libraries.sword.utils.ChecksumUtils;
import edu.columbia.libraries.sword.xml.SwordError;
import edu.columbia.libraries.sword.xml.entry.Entry;
import edu.columbia.libraries.sword.xml.entry.Feed;
import edu.columbia.libraries.sword.xml.entry.Generator;
import edu.columbia.libraries.sword.xml.entry.Link;
import edu.columbia.libraries.sword.xml.service.ServiceDocument;


@Path("/")
public class SWORDResource extends BaseRestResource {
    private static final Logger log = LoggerFactory.getLogger(SWORDResource.class.getName());
    
    public static final String ATOM_CONTENT_TYPE = "application/atom+xml; charset=UTF-8";
    public static final String ATOMSVC_CONTENT_TYPE = "application/atomsvc+xml; charset=UTF-8";
    public static final String UTC_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    public static final String DEFAULT_REALM_HEADER = "Basic realm=\"Fedora Repository Server\"";


    private static AtomicInteger counter = new AtomicInteger(0);

    @javax.ws.rs.core.Context
    protected ServletContext m_context;

    @javax.ws.rs.core.Context
    protected UriInfo m_uriInfo;

    private String m_authn;

    private int m_maxUpload;

    private File m_tempDir;

    private FedoraService m_sword;
    
    private Authorization m_authorization;
    
    private RepositoryInfo m_repoInfo;
    
    private JAXBContext m_bind;

    private String m_realm;
    
    Set<String> m_collectionPids = null;

    public SWORDResource(Server server) throws JAXBException, BeansException, ServerException {
    	super(server);
        m_sword = new FedoraService(server.getBean(DOManager.class), server.getBean(ResourceIndex.class), m_uriInfo);
        m_authorization = server.getBean(Authorization.class);
        m_repoInfo = server.getBean(Access.class).describeRepository(ReadOnlyContext.EMPTY);
		m_bind = JAXBContext.newInstance( ServiceDocument.class, Entry.class, SwordError.class );
    }
    
    // testing convenience method
    public void setServletContext(ServletContext context) {
    	m_context = context;
    }
    
    // testing convenience method
    public void setServletRequest(HttpServletRequest request) {
    	m_servletRequest = request;
    }
    
    // testing convenience method
    public void setUriInfo(UriInfo uriInfo) {
    	m_uriInfo = uriInfo;
    }
    
    public void setDepositHandlers(Map<String, DepositHandler> handlers) {
    	m_sword.setDepositHandlers(handlers);
    }
    
    public void setCollectionPids(Collection<String> collectionPids) {
    	m_collectionPids = new HashSet<String>(collectionPids);
    }

    public void init() throws InitializationException {
        m_authn = m_context.getInitParameter("authentication-method");
        if ((m_authn == null) || (m_authn.equals(""))) {
            m_authn = "None";
        }
        log.info("Authentication type set to: " + m_authn);
        
        if (m_collectionPids == null) {
        	m_collectionPids = new HashSet<String>(0);
        }

        String maxUploadSizeStr = m_context.getInitParameter("maxUploadSize");
        if ((maxUploadSizeStr == null) ||
                (maxUploadSizeStr.equals("")) ||
                (maxUploadSizeStr.equals("-1"))) {
            m_maxUpload = -1;
            log.warn("No maxUploadSize set, so setting max file upload size to unlimited.");
        } else {
            try {
                m_maxUpload = Integer.parseInt(maxUploadSizeStr);
                log.info("Setting max file upload size to " + m_maxUpload);
            } catch (NumberFormatException nfe) {
                m_maxUpload = -1;
                log.warn("maxUploadSize not a number, so setting max file upload size to unlimited.");
            }
        }

        String tempDirectory = m_context.getInitParameter(
                "upload-temp-directory");
        if ((tempDirectory == null) || (tempDirectory.equals(""))) {
            tempDirectory = System.getProperty("java.io.tmpdir");
        }
        if (!tempDirectory.endsWith(System.getProperty("file.separator")))
        {
            tempDirectory += System.getProperty("file.separator");
        }
        m_tempDir = new File(tempDirectory);
        log.info("Upload temporary directory set to: " + m_tempDir);
        if (!m_tempDir.exists()) {
            if (!m_tempDir.mkdirs()) {
                throw new InitializationException(
                        "Upload directory did not exist and I can't create it. "
                                + m_tempDir);
            }
        }
        if (!m_tempDir.isDirectory()) {
            log.error("Upload temporary directory is not a directory: {}", m_tempDir);
            throw new InitializationException(
                    "Upload temporary directory is not a directory: " + m_tempDir);
        }
        if (!m_tempDir.canWrite()) {
            log.error("Upload temporary directory cannot be written to: {}", m_tempDir);
            throw new InitializationException(
                    "Upload temporary directory cannot be written to: "
                            + m_tempDir);
        }

    }

    public void setSword(FedoraService sword) {
        m_sword = sword;
    }

    public void setRealm(String realm) {
        m_realm = realm;
    }
    
    @GET
    @Path("/service.atomsvc")
    @Produces("text/xml")
    public Response getDefaultServiceDocument() {
    	ServiceDocumentRequest request = new ServiceDocumentRequest(m_servletRequest);

    	if (!request.authenticated() && authenticateWithBasic()) {
            return authnRequiredResponse(m_realm);
        }
    	try {
    		Context authzContext;
    		if (request.isProxied()){
    			Context context = getContext();
    			// do some authZ to see if this user is allowed to proxy
    			//m_authorization.
    			try {
    				authzContext = ReadOnlyContext.getContext(m_servletRequest.getProtocol(), request.getOnBehalfOf(), null, true);
    			} catch (Exception e) {
    				throw new SWORDException(SWORDException.MEDIATION_NOT_ALLOWED, e);
    			}
    		} else {
    			authzContext = getContext();
    		}

    		ServiceDocument atomsvc = m_sword.getDefaultServiceDocument(authzContext);
    		Response response = Response.status(200)
    				.entity(atomsvc)
    				.header("Content-Type", "application/atom+xml; charset=UTF-8")
    				.build();
    		return response;
    	} catch (SWORDException e) {
    		SwordError error = new SwordError();
    		error.treatment = "Failed to retrieve service document.";
    		error.generator = new Generator("FCRepo", "3.6");
    		error.reason = e.reason;
    		Response response = Response.status(e.status).entity(error).build();
    		return response;
    	}
    }

    @GET
    @Path("/{collection}/service.atomsvc")
    @Produces("text/xml")
    public Response getServiceDocument(@PathParam("collection") String collection) {
    	ServiceDocumentRequest request = new ServiceDocumentRequest(m_servletRequest);

    	if (!request.authenticated() && authenticateWithBasic()) {
            return authnRequiredResponse(m_realm);
        }
    	try {
    	Context authzContext;
    	if (request.isProxied()){
    		Context context = getContext();
    		// do some authZ to see if this user is allowed to proxy
    		//m_authorization.
    		try {
				authzContext = ReadOnlyContext.getContext(m_servletRequest.getProtocol(), request.getOnBehalfOf(), null, true);
			} catch (Exception e) {
				throw new SWORDException(SWORDException.MEDIATION_NOT_ALLOWED, e);
			}
    	} else {
    		authzContext = getContext();
    	}
    	
    	ServiceDocument atomsvc = m_sword.getServiceDocument(collection, authzContext);
    	Response response = Response.status(200)
    			.entity(atomsvc)
    			.header("Content-Type", "application/atom+xml; charset=UTF-8")
    			.build();
    	return response;
    	} catch (SWORDException e) {
    		SwordError error = new SwordError();
    		error.treatment = "Failed to retrieve service document.";
    		error.generator = new Generator("FCRepo", "3.6");
    		error.reason = e.reason;
    		Response response = Response.status(e.status).entity(error).build();
    		return response;
    	}
    }
    
    /**
     * POST to the service document is not supported
     * @return
     */
    @POST
    @Path("/service.atomsvc")
    public Response postService() {
        return Response.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR)
                .entity("POST OPERATION NOT IMPLEMENTED").build();
    }


    @GET
    @Path("/{collection}")
    @Produces("text/xml")
    public Response getFeed(@PathParam("collection") String collection) {

        AtomEntryRequest adr = new AtomEntryRequest(m_servletRequest);
        if (!adr.authenticated() && authenticateWithBasic()) {
            return authnRequiredResponse(m_realm);
        }
        try{
        	Context authzContext;
        	if (adr.isProxied()){
                Context context = getContext();
                // do some authZ to see if this user is allowed to proxy
                authzContext = ReadOnlyContext.getContext(m_servletRequest.getProtocol(), adr.getOnBehalfOf(), null, true);
        	} else {
        		authzContext = getContext();
        	}

            // Generate the response
            Feed dr = m_sword.getEntryFeed(collection, null, authzContext);

            // Print out the Deposit Response

            Response response = Response.status(200)
                    .entity(dr)
                    .header("Content-Type", "application/atom+xml; charset=UTF-8")
                    .build();
            return response;
        } catch (SWORDException se) {
        	//TODO if this is an authn exception, send the right response
        	if (se.status == 401) {
                return authnRequiredResponse(m_realm);
        	} else {
                return Response.status(se.status)
                    .entity(se.toString()).build();
        	}
        } catch (Exception e) {
            return Response.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR)
                    .entity(e.toString()).build();
        }
    }

    /**
     * A POST to a Collection should create a resource.
     * A success returns a 201 with a Location header of the ATOM entry for the created resource
     * and an ATOM entry for the entity (xmlns="http://www.w3.org/2005/Atom") with:
     * a unique id (/entry/id)
     * a title (/entry/title)
     * a date of update (/entry/updated)
     * a link to the ATOM entry (/entry/link[@rel="edit"])
     * a link to the resource content (/entry/link[@rel="edit-media"]) 
     * @param collection
     * @return Response
     */
    @POST
    @Path("/{collection}")
    @Produces("text/xml")
    public Response postDeposit(@PathParam("collection") String collection) {
        DepositRequest deposit = null;
        try {
            deposit = new DepositRequest(m_servletRequest);
            deposit.setCollection(collection);
            deposit.setBaseUri(m_uriInfo);
            deposit.setGenerator(m_repoInfo);
        } catch (SWORDException e) {
            return errorResponse(e.reason,
                    HttpServletResponse.SC_BAD_REQUEST,
                    e.getMessage(),
                    m_servletRequest);
        }
        if ("reject".equals(deposit.getOnBehalfOf())){
            return errorResponse(SWORDException.OWNER_UNKNOWN.error,
                    HttpServletResponse.SC_FORBIDDEN,
                    "unknown use \"reject\"",
                    m_servletRequest);
        }
    	    	
        Date date = new Date();
        log.debug("Starting deposit processing at {} by {}", date.toString(), deposit.getIPAddress());
        if (!deposit.authenticated() && authenticateWithBasic()) {
            return authnRequiredResponse(m_realm);
        }
        File tempFile = new File(m_tempDir, "SWORD-" + deposit.getIPAddress() + "_" + counter.addAndGet(1));
        InputStream in = null;
        OutputStream out = null;
        try {

        	Context authzContext;
        	if (deposit.isProxied()){
                Context context = getContext();
                // do some authZ to see if this user is allowed to proxy
                try {
					authzContext = ReadOnlyContext.getContext(m_servletRequest.getProtocol(), deposit.getOnBehalfOf(), null, true);
				} catch (Exception e) {
					throw new SWORDException(SWORDException.FEDORA_ERROR, e);
				}
        	} else {
        		authzContext = getContext();
        	}

            try {
                out = new FileOutputStream(tempFile);
                in = m_servletRequest.getInputStream();
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > -1){
                    out.write(buf, 0, len);
                }
            } catch (IOException e) {
                return errorResponse(SWORDException.IO_ERROR.error,
                        HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                        e.getMessage(),
                        m_servletRequest);
            } finally {
            	try {
            		if (out != null) {
            			out.flush();
            			out.close();
            		}
            		if (in != null ) in.close();
            	} catch (IOException e) {
            		e.printStackTrace();
            	}
            }
            long fLen = tempFile.length();
            if ((m_maxUpload != -1) && (fLen > m_maxUpload)) {
                return errorResponse(SWORDException.MAX_UPLOAD_SIZE_EXCEEDED.error,
                        HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE,
                        "Maximum upload size (" + m_maxUpload + ") exceeded by input (" + fLen + ")",
                        m_servletRequest);
            }
            String actualMD5 = ChecksumUtils.generateMD5(tempFile.getPath());
            log.debug("Received checksum header {}", deposit.getMD5());
            log.debug("Calculated file checksum {}", actualMD5);
            if (!actualMD5.equals(deposit.getMD5())){
                log.debug("Bad MD5 for file. Aborting with appropriate error message");
                return errorResponse(SWORDException.ERROR_CHECKSUM.error,
                        HttpServletResponse.SC_PRECONDITION_FAILED,
                        "Received upload MD5 (" + deposit.getMD5() + ") did not match actual (" + actualMD5 + ")",
                        m_servletRequest);
            }
            deposit.setFile(tempFile);
            String location = null;
            Entry entry = m_sword.createEntry(deposit, authzContext);
            for (Link link: entry.getLinks()){
            	if (link.isDescription()) location = link.getHref().toString();
            }

            Response response = Response.status(HttpStatus.SC_CREATED)
                    .header(HttpHeaders.LOCATION, location)
                    .header(HttpHeaders.CONTENT_TYPE, ATOM_CONTENT_TYPE)
                    .entity(entry).build();
            return response;
        } catch (SWORDException e) {
        	System.err.println(e.toString());
        	e.printStackTrace();
            return Response.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR)
                    .entity(e.toString()).build();
        }
    }
    
    @GET
    @Path("/{collection}/{deposit}")
    @Produces("text/xml")
    public Response getDepositEntry(
    		@PathParam("collection") String collectionId,
    		@PathParam("deposit") String depositId) {

    	DepositRequest deposit = null;
        try {
        	deposit = new DepositRequest(m_servletRequest);
        	deposit.setCollection(collectionId);
        	deposit.setDepositId(depositId);
        	deposit.setBaseUri(m_uriInfo);
        	deposit.setGenerator(m_repoInfo);
        	Context authzContext;
        	if (deposit.isProxied()){
        		Context context = getContext();
        		// do some authZ to see if this user is allowed to proxy
        		try {
        			authzContext = ReadOnlyContext.getContext(m_servletRequest.getProtocol(), deposit.getOnBehalfOf(), null, true);
        		} catch (Exception e) {
        			throw new SWORDException(SWORDException.FEDORA_ERROR, e);
        		}
        	} else {
        		authzContext = getContext();
        	}

            String location = null;
            Entry entry = m_sword.getEntry(deposit, authzContext);
            for (Link link: entry.getLinks()){
            	if (link.isDescription()) location = link.getHref().toString();
            }

            Response response = Response.status(HttpStatus.SC_CREATED)
                    .header(HttpHeaders.LOCATION, location)
                    .header(HttpHeaders.CONTENT_TYPE, ATOM_CONTENT_TYPE)
                    .entity(entry).build();
            return response;
        } catch (SWORDException e) {
            return errorResponse(e.reason,
                    e.status,
                    e.getMessage(),
                    m_servletRequest);
        }
    }    

    /**
     * Utility method to decide if we are using HTTP Basic authentication
     *
     * @return if HTTP Basic authentication is in use or not
     */
    protected boolean authenticateWithBasic() {
        return "Basic".equalsIgnoreCase(m_authn);
    }
    
    public static Response authnRequiredResponse(String realm) {
        String headerValue;
        if (realm == null) headerValue = DEFAULT_REALM_HEADER;
        else headerValue = "Basic realm=\"" + realm + "\"";
        Response response = Response.status(401).header("WWW-Authenticate", headerValue).build();
        return response;
    }


    public static Response errorResponse(URI errorURI, int status, String summary, HttpServletRequest request) {
        SwordError sed = new SwordError();
        sed.reason = errorURI;
        sed.title = "ERROR";
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat zulu = new SimpleDateFormat(UTC_DATE_FORMAT);
        String serializedDate = zulu.format(calendar.getTime());
        sed.updated = serializedDate;
        sed.summary = summary;
        if (request.getHeader(HttpHeaders.USER_AGENT) != null) {
            sed.setUserAgent(request.getHeader(HttpHeaders.USER_AGENT));
        }
        Response response = Response.status(status)
                                    .header(HttpHeaders.CONTENT_TYPE, ATOM_CONTENT_TYPE)
                                    .entity(sed).build();
        return response;
    }

}
