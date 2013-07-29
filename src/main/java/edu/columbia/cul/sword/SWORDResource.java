package edu.columbia.cul.sword;

import java.io.File;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBException;

import org.fcrepo.common.Constants;
import org.fcrepo.server.Context;
import org.fcrepo.server.MultiValueMap;
import org.fcrepo.server.ReadOnlyContext;
import org.fcrepo.server.Server;
import org.fcrepo.server.access.Access;
import org.fcrepo.server.access.RepositoryInfo;
import org.fcrepo.server.errors.ServerException;
import org.fcrepo.server.rest.BaseRestResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.WebApplicationContextUtils;

import edu.columbia.cul.sword.exceptions.SWORDException;
import edu.columbia.cul.sword.holder.InfoFactory;
import edu.columbia.cul.sword.holder.SwordSessionStructure;
import edu.columbia.cul.sword.impl.AtomEntryRequest;
import edu.columbia.cul.sword.impl.ServiceDocumentRequest;
import edu.columbia.cul.sword.utils.LogResutUtils;
import edu.columbia.cul.sword.utils.SwordHelper;
import edu.columbia.cul.sword.xml.SwordError;
import edu.columbia.cul.sword.xml.entry.Entry;
import edu.columbia.cul.sword.xml.entry.Feed;
import edu.columbia.cul.sword.xml.entry.Generator;
import edu.columbia.cul.sword.xml.service.ServiceDocument;


@Path("/")
@Component
public class SWORDResource extends BaseRestResource implements SwordConstants {
	
    private static final Logger LOGGER = LoggerFactory.getLogger(SWORDResource.class.getName());

    private static AtomicInteger counter = new AtomicInteger(0); 
    private static ReadOnlyContext READ_ONLY_CONTEXT = readOnlyContext();
    
    @javax.ws.rs.core.Context
    protected ServletContext m_context;

    private RepositoryService repositoryService;        
    private RepositoryInfo repositoryInfo;    
    private String m_realm;   

    private String maxUploadSize;
    private String tempUploadDir;
    
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
    		LOGGER.warn(e.getMessage(), e);
    		return ReadOnlyContext.EMPTY;
    	}
    }

    //public SWORDResource(Server server, RepositoryService repositoryService, org.apache.cxf.jaxb.JAXBDataBinding dataBinding) throws JAXBException, BeansException, ServerException {
    public SWORDResource(Server server, RepositoryService repositoryService) throws JAXBException, BeansException, ServerException {

    	super(server);

    	LOGGER.debug("=== SWORDResource created ===");
    	
    	repositoryService.init(server);
    	this.repositoryService = repositoryService;
    	
        repositoryInfo = ((Access)server.getBean(Access.class.getName())).describeRepository(READ_ONLY_CONTEXT);
        

        
        //org.apache.cxf.jaxb.JAXBDataBinding dataBinding = server.getBean(org.apache.cxf.jaxb.JAXBDataBinding.class.getName(), org.apache.cxf.jaxb.JAXBDataBinding.class);

       // Map oldPrefixes = ep.
        
        for (String name : server.getBeanDefinitionNames()){
        	System.out.println("bean name: " + name);
        }
        
//        Map<String,String>prefixes = new HashMap<String,String>();
//        
//        prefixes.put("http://www.w3.org/2005/Atom", "atom");
//        prefixes.put("atom", "http://www.w3.org/2005/Atom");
//        
//        ep.setMarshallerProperties(prefixes);
        
        
        
        //dataBinding.getNamespaceMap().put("http://www.w3.org/2005/Atom", "atom");
    }

    private String getAuthenticationMethod() {
        String authn = m_context.getInitParameter("authentication-method");
        if ((authn == null) || (authn.equals(""))) {
        	authn = "None";
        }
        
        LOGGER.info("Authentication type set to:{}", authn);
        return authn;
    }

    // testing convenience method
    public void repositoryService(RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }

    public void ssetRealm(String realm) {
        m_realm = realm;
    }
    
    @GET
    @Path("/" + SwordConstants.SERVICEDOCUMENT)
    @Produces("application/atomsvc+xml")
    public Response getServiceDocument(@javax.ws.rs.core.Context ServletContext servletContext, 
    		                           @javax.ws.rs.core.Context HttpServletRequest servletRequest,
    		                           @javax.ws.rs.core.Context UriInfo uriInfo) {
    	return getDefaultServiceDocument(servletContext, servletRequest, uriInfo);
    }
    
    @GET
    @Path("/service.atomsvc")
    @Produces("application/atomsvc+xml")
    public Response getDefaultServiceDocument(@javax.ws.rs.core.Context ServletContext servletContext, 
    		                                  @javax.ws.rs.core.Context HttpServletRequest servletRequest,
    		                                  @javax.ws.rs.core.Context UriInfo uriInfo) {

    	LOGGER.debug("Started getDefaultServiceDocument");
	
    	try {
    		
        	SwordSessionStructure swordSession = InfoFactory.makeNewIfoHolder(servletRequest, 
															                    servletContext, 
															                    repositoryInfo, 
															                    uriInfo);
    		LOGGER.debug(" infoStucture: \n" + LogResutUtils.printablePublicValues(swordSession) + "\n");
    		
    		
//        	ServiceDocumentRequest request = new ServiceDocumentRequest(servletRequest);
//
//        	if (!request.authenticated() && authenticateWithBasic()) {
//                return authnRequiredResponse(m_realm);
//            }   		
    		
    		
    		ServiceDocument atomsvc = repositoryService.getDefaultServiceDocument(swordSession);
    		
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
    @Path("/{collection}/" + SwordConstants.SERVICEDOCUMENT)
    @Produces("application/atomsvc+xml")
    public Response getServiceDocument(
    		@PathParam("collection") String collection,
    		@javax.ws.rs.core.Context ServletContext servletContext,
    		@javax.ws.rs.core.Context HttpServletRequest servletRequest,
    		@javax.ws.rs.core.Context UriInfo uriInfo
    		) {
    	
    	return getDefaultServiceDocument(collection, servletContext, servletRequest, uriInfo);
    }

    @GET
    @Path("/{collection}/service.atomsvc")
    @Produces("application/atomsvc+xml")
    public Response getDefaultServiceDocument(
    		@PathParam("collection") String collection,
    		@javax.ws.rs.core.Context ServletContext servletContext,
    		@javax.ws.rs.core.Context HttpServletRequest servletRequest,
    		@javax.ws.rs.core.Context UriInfo uriInfo
    		) {
    	
    	System.out.println("==== Started getDefaultServiceDocument for collection ====");
    	LOGGER.debug("Started getDefaultServiceDocument for collection");

//    	ServiceDocumentRequest request = new ServiceDocumentRequest(m_servletRequest);
////    	DepositRequest request = null;
////		try {
////			request = new DepositRequest(m_servletRequest);
////		} catch (SWORDException e1) {
////			e1.printStackTrace();
////		}
//
//    	if (!request.authenticated() && authenticateWithBasic()) {
//            return authnRequiredResponse(m_realm);
//        }
		try {

        	SwordSessionStructure swordSession = InfoFactory.makeNewIfoHolder(servletRequest, 
															                    servletContext, 
															                    repositoryInfo, 
															                    uriInfo);
			LOGGER.debug(" infoStucture: \n" + LogResutUtils.printablePublicValues(swordSession) + "\n");

			ServiceDocument atomsvc = repositoryService.getServiceDocument(collection, swordSession);
			Response response = Response
									.status(200)
									.entity(atomsvc)
									.header("Content-Type",
											"application/atom+xml; charset=UTF-8").build();
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
    	LOGGER.debug("Started postService");
    	
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
    	
    	LOGGER.debug("Started getFeed");
    	//setServletContext(servletContext);

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
            Feed dr = repositoryService.getEntryFeed(collection, null, authzContext);

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
    @Produces("application/atomsvc+xml")
    public Response postDeposit(
    		@PathParam("collection") String collectionId,
    		@javax.ws.rs.core.Context ServletContext servletContext,
    		@javax.ws.rs.core.Context UriInfo uriInfo,
    		@javax.ws.rs.core.Context HttpServletRequest servletRequest) {

    	LOGGER.debug("== Started postDeposit ==");

    	File tempFile = null;
    	 
        try {
        	
        	SwordSessionStructure swordSession = InfoFactory.makeNewIfoHolder(servletRequest, 
        			                                                          servletContext, 
        			                                                          repositoryInfo, 
        			                                                          uriInfo);

        	swordSession.collectionId = collectionId;
        	
        	// do all validations/authorization here (I mean call validations method) and throw exception if any 
        	repositoryService.isContentSupported(swordSession);
        	
			swordSession.tempDir = SwordHelper.verifyTempDirectoryName(tempUploadDir);
			File tempDir = SwordHelper.createTempDirectory(swordSession.tempDir);

			swordSession.maxUploadSizeInt = SwordHelper.makeMaxUploadSize(maxUploadSize);

			tempFile = SwordHelper.receiveFile(tempDir, servletRequest, counter, swordSession.maxUploadSizeInt);
			swordSession.tempFile = tempFile;

            LOGGER.debug(" infoStucture: \n" + LogResutUtils.printablePublicValues(swordSession) + "\n");
            // do MD5 validations here (I mean call validations method) and throw exception if any 


            Entry newDepositedEntry = repositoryService.createEntry(swordSession);
  
            return SwordHelper.makeResutResponce(newDepositedEntry);  
            
        } catch (SWORDException e) {
        	
        	System.out.println("=== SWORDException === " + e.status + " - " + e.getMessage() + " - " + e.reason);
            return SwordHelper.errorResponse(e.reason,
            		e.status,
                    //HttpServletResponse.SC_BAD_REQUEST,
                    e.getMessage(),
                    servletRequest);	
        } finally {
        	if(tempFile != null && tempFile.exists()){
        		tempFile.delete();
        	}
        }
    }   
    
   
    
    @GET
    @Path("/{collection}/{deposit}")
    //@Produces("text/xml")
    @Produces("application/atom+xml;type=entry")
    public Response getDepositEntry(
    		@PathParam("collection") String collectionId,
    		@PathParam("deposit") String depositId,
    		@javax.ws.rs.core.Context ServletContext servletContext,
    		@javax.ws.rs.core.Context UriInfo uriInfo,
    		@javax.ws.rs.core.Context HttpServletRequest servletRequest,
    		@javax.ws.rs.core.Context javax.ws.rs.ext.Providers providers
    		) {

    	LOGGER.debug("Started getDepositEntry");

        try {
        	
        	SwordSessionStructure swordSession = InfoFactory.makeNewIfoHolder(servletRequest, 
															                    servletContext, 
															                    repositoryInfo, 
															                    uriInfo);
        	
        	swordSession.depositId = depositId;
        	swordSession.collectionId = collectionId;
        	swordSession.uriInfo = uriInfo;
			
			LOGGER.debug(" infoStucture: \n" + LogResutUtils.printablePublicValues(swordSession));

			Entry entry = repositoryService.getEntry(swordSession);
            
            return SwordHelper.makeResutResponce(entry);

        } catch (SWORDException e) {
            return SwordHelper.errorResponse(e.reason,
							                   e.status,
							                   e.getMessage(),
							                   servletRequest);
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

    
    // ----------------------------- set methods set from sword-jaxrs.xml ----------------------------- //

	public void setMaxUploadSize(String maxUploadSize){
		this.maxUploadSize = maxUploadSize;
	}
	
	public void setTempUploadDir(String tempUploadDir){
		this.tempUploadDir = tempUploadDir;
	}	

} // ============================================== //
