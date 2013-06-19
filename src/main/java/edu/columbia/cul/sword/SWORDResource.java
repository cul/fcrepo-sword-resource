package edu.columbia.cul.sword;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
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
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBException;

import org.apache.commons.httpclient.HttpStatus;
import org.fcrepo.common.Constants;
import org.fcrepo.server.Context;
import org.fcrepo.server.MultiValueMap;
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
import org.springframework.stereotype.Component;

import edu.columbia.cul.sword.impl.AtomEntryRequest;
import edu.columbia.cul.sword.impl.DepositRequest;
import edu.columbia.cul.sword.impl.ServiceDocumentRequest;
import edu.columbia.cul.sword.impl.fcrepo.FedoraService;
import edu.columbia.cul.sword.utils.ChecksumUtils;
import edu.columbia.cul.sword.xml.SwordError;
import edu.columbia.cul.sword.xml.entry.Entry;
import edu.columbia.cul.sword.xml.entry.Feed;
import edu.columbia.cul.sword.xml.entry.Generator;
import edu.columbia.cul.sword.xml.entry.Link;
import edu.columbia.cul.sword.xml.service.ServiceDocument;


@Path("/")
@Component
public class SWORDResource extends BaseRestResource {
    private static final Logger log = LoggerFactory.getLogger(SWORDResource.class.getName());
    
    public static final String ATOM_CONTENT_TYPE = "application/atom+xml; charset=UTF-8";
    public static final String ATOMSVC_CONTENT_TYPE = "application/atomsvc+xml; charset=UTF-8";
    public static final String UTC_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    public static final String DEFAULT_REALM_HEADER = "Basic realm=\"Fedora Repository Server\"";


    private static AtomicInteger counter = new AtomicInteger(0);
    
    private static ReadOnlyContext READ_ONLY_CONTEXT = readOnlyContext();

    @javax.ws.rs.core.Context
    protected ServletContext m_context;

    private int m_maxUpload;

    private File m_tempDir;

    private FedoraService m_sword;
        
    private RepositoryInfo m_repoInfo;
    
    private String m_realm;
    
    Set<String> m_collectionPids = null;
    
    private static final ReadOnlyContext readOnlyContext() {
    	try {
            ReadOnlyContext roc = ReadOnlyContext.getContext("internal", "", null, false);
            MultiValueMap env = roc.getEnvironmentAttributes();
            MultiValueMap newEnv = new MultiValueMap();
            Iterator<String> names = env.names();
            while(names.hasNext()) {
            	String name = names.next();
            	newEnv.set(name, env.getStringArray(name));
            }
            newEnv.set(Constants.HTTP_REQUEST.SECURITY.uri,
            		Constants.HTTP_REQUEST.INSECURE.uri);
            roc.setEnvironmentValues(newEnv);
            return roc;
    	} catch (Exception e) {
    		log.warn(e.getMessage(), e);
    		return ReadOnlyContext.EMPTY;
    	}
    }

    public SWORDResource(Server server) throws JAXBException, BeansException, ServerException {
    	super(server);
        
    	m_sword = new FedoraService(
        		server.getBean(Authorization.class),
        		server.getBean(DOManager.class),
        		server.getBean(ResourceIndex.class));
    	
        m_repoInfo = ((Access)server.getBean(Access.class.getName())).describeRepository(READ_ONLY_CONTEXT);
   
        log.debug("RepositoryInfo.repositoryName:          {}", m_repoInfo.repositoryName);
        log.debug("RepositoryInfo.repositoryBaseURL:       {}", m_repoInfo.repositoryBaseURL);
        log.debug("RepositoryInfo.repositoryVersion:       {}", m_repoInfo.repositoryVersion);
        log.debug("RepositoryInfo.repositoryPIDNamespace:  {}", m_repoInfo.repositoryPIDNamespace);
        log.debug("RepositoryInfo.defaultExportFormat:     {}", m_repoInfo.defaultExportFormat);
        log.debug("RepositoryInfo.OAINamespace:            {}", m_repoInfo.OAINamespace);
        log.debug("RepositoryInfo.adminEmailList:          {}", Arrays.asList(m_repoInfo.adminEmailList));
        log.debug("RepositoryInfo.samplePID:               {}", m_repoInfo.samplePID);
        log.debug("RepositoryInfo.sampleOAIIdentifer:      {}", m_repoInfo.sampleOAIIdentifer);
        log.debug("RepositoryInfo.sampleSearchURL:         {}", m_repoInfo.sampleSearchURL);
        log.debug("RepositoryInfo.sampleAccessURL:         {}", m_repoInfo.sampleAccessURL);
        log.debug("RepositoryInfo.sampleOAIURL:            {}", m_repoInfo.sampleOAIURL);
        log.debug("RepositoryInfo.retainPIDs:              {}", Arrays.asList(m_repoInfo.retainPIDs));
        
        init();
    }
    
    public void setServletContext(ServletContext context) {

    	if (m_context == null) {
    		m_context = context;
    	}
    	
    	if(m_context == null){
    		log.error("ServletContext not provided +3+");
    	}
    	
    	log.debug("started setServletContext +3+");
    	
    	
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
    	log.info("Upload temporary directory set to: " + m_tempDir.getPath());
    	if (!m_tempDir.exists()) {
    		if (!m_tempDir.mkdirs()) {
        		log.error("Upload directory did not exist and I can't create it. {}", m_tempDir.getPath());
    			throw new IllegalArgumentException(
    					"Upload directory did not exist and I can't create it. "
    							+ m_tempDir.getPath());
    		}
    	}
    	
    	if (!m_tempDir.isDirectory()) {
    		log.error("Upload temporary directory is not a directory: {}", m_tempDir.getPath());
    		throw new IllegalArgumentException(
    				"Upload temporary directory is not a directory: " + m_tempDir.getPath());
    	}
    	if (!m_tempDir.canWrite()) {
    		log.error("Upload temporary directory cannot be written to: {}", m_tempDir.getPath());
    		throw new IllegalArgumentException(
    				"Upload temporary directory cannot be written to: "
    						+ m_tempDir.getPath());
    	}
        String maxUploadSizeStr = m_context.getInitParameter("maxUploadSize");
        if ((maxUploadSizeStr == null) ||
                (maxUploadSizeStr.equals("")) ||
                (maxUploadSizeStr.equals("-1"))) {
            m_maxUpload = -1;
            log.warn("++ No maxUploadSize set, so setting max file upload size to unlimited.");
        } else {
            try {
                m_maxUpload = Integer.parseInt(maxUploadSizeStr);
                log.info("Setting max file upload size to " + m_maxUpload);
            } catch (NumberFormatException nfe) {
                m_maxUpload = -1;
                log.warn("maxUploadSize not a number, so setting max file upload size to unlimited.");
            }
        }

    }
    
    // testing convenience method
    public void setServletRequest(HttpServletRequest request) {
    	m_servletRequest = request;
    }
    
    public void setMembershipPredicate(String predicate) {
    	m_sword.setMembershipRel(predicate);
    }
    
    public void setDepositHandlers(Map<String, DepositHandler> handlers) {
    	m_sword.setDepositHandlers(handlers);
    }
    
    public void setCollectionPids(Collection<String> collectionPids) {
    	m_collectionPids = new HashSet<String>(collectionPids);
		m_sword.setCollections(m_collectionPids);

		int count = 0;
		log.info("collectionIds size: " + m_collectionPids.size());
		for (String collection : m_collectionPids) {
			log.info("collection-{} {}", ++count, collection);
		}

	}
    
    private String getAuthenticationMethod() {
        String authn = m_context.getInitParameter("authentication-method");
        if ((authn == null) || (authn.equals(""))) {
        	authn = "None";
        }
        
        log.info("Authentication type set to:{}", authn);
        return authn;
    }

    public void init() throws InitializationException {
        
        if (m_collectionPids == null) {
        	setCollectionPids(new HashSet<String>(0));
        }

    	m_sword.init();

    }

    // testing convenience method
    public void setSword(FedoraService sword) {
        m_sword = sword;
    }

    public void setRealm(String realm) {
        m_realm = realm;
    }
    
    @GET
    @Path("/service.atomsvc")
    @Produces("text/xml")
    public Response getDefaultServiceDocument(@javax.ws.rs.core.Context ServletContext servletContext) {

    	setServletContext(servletContext);

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
    public Response getServiceDocument(
    		@PathParam("collection") String collection,
    		@javax.ws.rs.core.Context ServletContext servletContext
    		) {
    	
    	setServletContext(servletContext);

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
    public Response getFeed(
    		@PathParam("collection") String collection,
    		@javax.ws.rs.core.Context ServletContext servletContext
    		) {
    	
    	setServletContext(servletContext);

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
    public Response postDeposit(
    		@PathParam("collection") String collection,
    		@javax.ws.rs.core.Context ServletContext servletContext,
    		@javax.ws.rs.core.Context UriInfo uriInfo) {

        setServletContext(servletContext);

        DepositRequest deposit = null;

        try {
        	
            deposit = new DepositRequest(m_servletRequest);
            deposit.setCollection(collection);
            deposit.setBaseUri(uriInfo);
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
        
        log.debug("Temp file: {}", tempFile.getAbsolutePath());

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
            	
            	String errMsg = "Maximum upload size (" + m_maxUpload + ") exceeded by input (" + fLen + ")";
            	log.error(errMsg);
                
            	return errorResponse(SWORDException.MAX_UPLOAD_SIZE_EXCEEDED.error,
                        HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE,
                        errMsg,
                        m_servletRequest);
            }
            
            String actualMD5 = ChecksumUtils.generateMD5(tempFile.getPath());
            
            log.debug("Received checksum header {}", deposit.getMD5());
            log.debug("Calculated file checksum {}", actualMD5);
            
            if (!actualMD5.equals(deposit.getMD5())){
            	String errMsg = "Received upload MD5 (" + deposit.getMD5() + ") did not match actual (" + actualMD5 + ")";
            	log.error(errMsg);
            	
                return errorResponse(SWORDException.ERROR_CHECKSUM.error,
                        HttpServletResponse.SC_PRECONDITION_FAILED,
                        errMsg,
                        m_servletRequest);
            }
            
            deposit.setFile(tempFile);
            String location = null;
            Entry entry = m_sword.createEntry(deposit, authzContext);

            for (Link link: entry.getLinks()){
            	if (link.isDescription()) location = link.getHref().toString();
            }

            ResponseBuilder responseBuilder  = Response.status(HttpStatus.SC_CREATED);
            if (location != null) { responseBuilder.header(HttpHeaders.LOCATION, location); }
            responseBuilder.header(HttpHeaders.CONTENT_TYPE, ATOM_CONTENT_TYPE);
            responseBuilder.entity(entry);
            
            return responseBuilder.build();
            
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
    		@PathParam("deposit") String depositId,
    		@javax.ws.rs.core.Context ServletContext servletContext) {
    	
    	setServletContext(servletContext);

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
        return "Basic".equalsIgnoreCase(getAuthenticationMethod());
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
