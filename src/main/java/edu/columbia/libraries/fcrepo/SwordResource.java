package edu.columbia.libraries.fcrepo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
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
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.fcrepo.common.Constants;
import org.fcrepo.server.Context;
import org.fcrepo.server.ReadOnlyContext;
import org.fcrepo.server.Server;
import org.fcrepo.server.errors.InitializationException;
import org.fcrepo.server.rest.BaseRestResource;
import org.fcrepo.server.security.Authorization;
import org.purl.sword.base.AtomDocumentResponse;
import org.purl.sword.base.ChecksumUtils;
import org.purl.sword.base.DepositResponse;
import org.purl.sword.base.ErrorCodes;
import org.purl.sword.base.SWORDAuthenticationException;
import org.purl.sword.base.SWORDErrorException;
import org.purl.sword.base.SWORDException;
import org.purl.sword.base.ServiceDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.columbia.libraries.sword.DepositHandler;
import edu.columbia.libraries.sword.SWORDServer;
import edu.columbia.libraries.sword.impl.AtomEntryRequest;
import edu.columbia.libraries.sword.impl.DepositRequest;
import edu.columbia.libraries.sword.impl.ServiceDocumentRequest;


@Path("/")
public class SwordResource extends BaseRestResource {
    private static final Logger log = LoggerFactory.getLogger(SwordResource.class.getName());

    private static AtomicInteger counter = new AtomicInteger(0);

    @javax.ws.rs.core.Context
    protected ServletContext m_context;

    private String m_authn;

    private int m_maxUpload;

    private File m_tempDir;

    private SWORDServer m_sword;
    
    private Authorization m_authorization;

    private String m_realm;
    
    Set<String> m_collectionPids = null;

    public SwordResource(Server server) {
    	super(server);
        m_sword = new FedoraServer(m_management, null);
        m_authorization = server.getBean(Authorization.class);
    }
    
    public void setServletRequest(HttpServletRequest request) {
    	m_servletRequest = request;
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

    public void setSword(SWORDServer sword) {
        m_sword = sword;
    }

    public void setRealm(String realm) {
        m_realm = realm;
    }
    
    @GET
    @Path("/service.atomsvc")
    public Response getServiceDocument() {
    	ServiceDocumentRequest request = new ServiceDocumentRequest(m_servletRequest);

    	if (!request.authenticated() && authenticateWithBasic()) {
            return AbstractDepositResource.authnRequiredResponse(m_realm);
        }
    	try {
    	Context authzContext;
    	if (request.isProxied()){
    		Context context = getContext();
    		// do some authZ to see if this user is allowed to proxy
    		//m_authorization.
    		authzContext = ReadOnlyContext.getContext(m_servletRequest.getProtocol(), request.getOnBehalfOf(), null, true);
    	} else {
    		authzContext = getContext();
    	}
    	
    	ServiceDocument atomsvc = m_sword.doServiceDocument(request, authzContext);
    	Response response = Response.status(200)
    			.entity(atomsvc.marshall())
    			.header("Content-Type", "application/atom+xml; charset=UTF-8")
    			.build();
    	return response;
    	} catch (Exception e) {
    		Response response = Response.status(500).build();
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
    public Response getDeposit(@PathParam("collection") String collection) {

        AtomEntryRequest adr = new AtomEntryRequest(m_servletRequest);
        if (!adr.authenticated() && authenticateWithBasic()) {
            return AbstractDepositResource.authnRequiredResponse(m_realm);
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
            AtomDocumentResponse dr = m_sword.doAtomDocument(adr, authzContext);

            // Print out the Deposit Response

            Response response = Response.status(dr.getHttpResponse())
                    .entity(dr.marshall())
                    .header("Content-Type", "application/atom+xml; charset=UTF-8")
                    .build();
            return response;
        } catch (SWORDAuthenticationException sae) {
            // Ask for credentials again
            return AbstractDepositResource.authnRequiredResponse(m_realm);
        } catch (SWORDException se) {
            return Response.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR)
                    .entity(se.toString()).build();
        } catch (SWORDErrorException se) {
            // Get the details and send the right SWORD error document
            return AbstractDepositResource.errorResponse(se.getErrorURI(),
                    se.getStatus(),
                    se.getDescription(),
                    m_servletRequest);
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
    public Response postDeposit(@PathParam("collection") String collection) {
        DepositRequest deposit = null;
        try {
            deposit = new DepositRequest(m_servletRequest);
        } catch (SWORDErrorException e) {
            return AbstractDepositResource.errorResponse(e.getErrorURI(),
                    HttpServletResponse.SC_BAD_REQUEST,
                    e.getMessage(),
                    m_servletRequest);
        }
        if ("reject".equals(deposit.getOnBehalfOf())){
            return AbstractDepositResource.errorResponse(ErrorCodes.TARGET_OWNER_UKNOWN,
                    HttpServletResponse.SC_FORBIDDEN,
                    "unknown use \"reject\"",
                    m_servletRequest);
        }
    	    	
        Date date = new Date();
        log.debug("Starting deposit processing at {} by {}", date.toString(), deposit.getIPAddress());
        if (!deposit.authenticated() && authenticateWithBasic()) {
            return AbstractDepositResource.authnRequiredResponse(m_realm);
        }
        File tempFile = new File(m_tempDir, "SWORD-" + deposit.getIPAddress() + "_" + counter.addAndGet(1));
        InputStream in = null;
        OutputStream out = null;
        try {

        	Context authzContext;
        	if (deposit.isProxied()){
                Context context = getContext();
                // do some authZ to see if this user is allowed to proxy
                authzContext = ReadOnlyContext.getContext(m_servletRequest.getProtocol(), deposit.getOnBehalfOf(), null, true);
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
            } finally {
                if (out != null) {
                    out.flush();
                    out.close();
                }
                if (in != null ) in.close();
            }
            long fLen = tempFile.length();
            if ((m_maxUpload != -1) && (fLen > m_maxUpload)) {
                return AbstractDepositResource.errorResponse(ErrorCodes.MAX_UPLOAD_SIZE_EXCEEDED,
                        HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE,
                        "Maximum upload size (" + m_maxUpload + ") exceeded by input (" + fLen + ")",
                        m_servletRequest);
            }
            String actualMD5 = ChecksumUtils.generateMD5(tempFile.getPath());
            log.debug("Received checksum header {}", deposit.getMD5());
            log.debug("Received file checksum {}", actualMD5);
            if (!actualMD5.equals(deposit.getMD5())){
                log.debug("Bad MD5 for file. Aborting with appropriate error message");
                return AbstractDepositResource.errorResponse(ErrorCodes.ERROR_CHECKSUM_MISMATCH,
                        HttpServletResponse.SC_PRECONDITION_FAILED,
                        "Received upload MD5 (" + deposit.getMD5() + ") did not match actual (" + actualMD5 + ")",
                        m_servletRequest);
            }
            deposit.setFile(tempFile);
            DepositResponse depositResponse = m_sword.doDeposit(deposit.getDeposit(), authzContext);
            System.err.println(depositResponse.marshall());
            Response response = Response.status(depositResponse.getHttpResponse())
                    .header(HttpHeaders.LOCATION, depositResponse.getLocation())
                    .header(HttpHeaders.CONTENT_TYPE, AbstractDepositResource.ATOM_CONTENT_TYPE)
                    .entity(depositResponse.marshall()).build();
            return response;
        } catch (IOException e) {
        	System.err.println(e.toString());
            return AbstractDepositResource.errorResponse("http://purl.org/net/sword/error/IOException",
                    HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    e.getMessage(),
                    m_servletRequest);
        } catch (NoSuchAlgorithmException e) {
        	System.err.println(e.toString());
            return AbstractDepositResource.errorResponse("http://purl.org/net/sword/error/MD5Missing",
                    HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    e.getMessage(),
                    m_servletRequest);
        } catch (SWORDAuthenticationException e) {
        	System.err.println(e.toString());
            return AbstractDepositResource.authnRequiredResponse(m_realm);
        } catch (SWORDErrorException e) {
        	System.err.println(e.toString());
            return AbstractDepositResource.errorResponse(e.getErrorURI(),
                    e.getStatus(),
                    e.getMessage(),
                    m_servletRequest);
        } catch (SWORDException e) {
        	System.err.println(e.toString());
        	e.printStackTrace();
            return Response.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR)
                    .entity(e.toString()).build();
        } catch (Exception e) {
        	e.printStackTrace();
            return Response.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR)
                    .entity(e.toString()).build();
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

    /**
     * Utility method to construct the URL called for this Servlet
     *
     * @param req The request object
     * @return The URL
     */
    protected static String getURL2(HttpServletRequest req) {
        String reqUrl = req.getRequestURL().toString();
        String queryString = req.getQueryString();
        if (queryString != null) {
            reqUrl += "?" + queryString;
        }
        return reqUrl;
    }


}
