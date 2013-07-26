package edu.columbia.cul.sword.holder;

import java.util.StringTokenizer;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.fcrepo.server.Context;
import org.fcrepo.server.access.RepositoryInfo;
import org.fcrepo.utilities.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.columbia.cul.sword.HttpHeaders;
import edu.columbia.cul.sword.SWORDResource;
import edu.columbia.cul.sword.exceptions.SWORDException;
import edu.columbia.cul.sword.names.ContextNames;
import edu.columbia.cul.sword.utils.SwordHelper;

public class InfoFactory {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(InfoFactory.class.getName());

	public static SwordSessionStructure makeNewIfoHolder(HttpServletRequest request, ServletContext context, RepositoryInfo repositoryInfo) throws SWORDException {

		SwordSessionStructure structure = new SwordSessionStructure();
		
		HttpHeaderStructure httpHeaderStructure = makeHttpStructure(request);
		WebContextStructure contextStructure = makeContextStructure(context);
		RepositoryInfo fedoraInfoStructure = repositoryInfo;
		
		
		structure.httpHeader = httpHeaderStructure;
		structure.webContext = contextStructure;
		structure.repositoryInfo = fedoraInfoStructure;
		
		structure.authenticated = Credentials(structure, request);
		structure.proxied = httpHeaderStructure.onBehalfOf != null && !(httpHeaderStructure.onBehalfOf.equals(structure.userName));		
		structure.fileName = httpHeaderStructure.contentDisposition == null ? null : httpHeaderStructure.contentDisposition.replace("filename=", "");
		
		structure.contentLength = httpHeaderStructure.contentLength == null ? 0 : Integer.parseInt(httpHeaderStructure.contentLength);	
		structure.verbose = Boolean.valueOf(httpHeaderStructure.verbose);
		structure.noOp = Boolean.valueOf(httpHeaderStructure.noOp);
		structure.ipAddress = request.getRemoteAddr();
		//structure.location = getURL(request);
		
		Context fedoraContext = SwordHelper.getContext(structure.proxied, structure.httpHeader.onBehalfOf, request, LOGGER);
		structure.fedoraContext = fedoraContext;
		

		return structure;
	}
	
	public static WebContextStructure makeContextStructure(ServletContext context){
		
		WebContextStructure contextStructure = new WebContextStructure();
		
		contextStructure.authenticationMethod = context.getInitParameter(ContextNames.AUTHENTICATION_METHOD);
		contextStructure.maxUploadSize = context.getInitParameter(ContextNames.MAX_UPLOAD_SIZE);
		contextStructure.uploadTempDirectory = context.getInitParameter(ContextNames.UPLOAD_TEMP_DIRECTORY);
		
		contextStructure.propertyConfigLocation = context.getInitParameter(ContextNames.PROPERTY_CONFIG_LOCATION);
		contextStructure.fedoraHome = context.getInitParameter(ContextNames.FEDOR_HOME);
		contextStructure.contextConfigLocation = context.getInitParameter(ContextNames.CONTEXT_CONFIG_LOCATION);

		return contextStructure;
		
	}
	
	public static HttpHeaderStructure makeHttpStructure(HttpServletRequest request){
		
		HttpHeaderStructure header = new HttpHeaderStructure();

		header.userAgent = request.getHeader(HttpHeaders.USER_AGENT);
		header.onBehalfOf = request.getHeader(HttpHeaders.X_ON_BEHALF_OF);
		
		header.contentDisposition = request.getHeader(HttpHeaders.CONTENT_DISPOSITION);		
		header.md5 = request.getHeader(HttpHeaders.CONTENT_MD5);
		header.contentType = request.getContentType();		
		header.slug = request.getHeader(HttpHeaders.SLUG);	
		header.packaging = request.getHeader(HttpHeaders.X_PACKAGING);
		header.noOp = request.getHeader(HttpHeaders.X_NO_OP);		
		header.verbose = request.getHeader(HttpHeaders.X_VERBOSE);
		header.contentLength = request.getHeader(HttpHeaders.CONTENT_LENGTH);
		
		return header;
	}
	
    public static String getURL(HttpServletRequest req) {
        String reqUrl = req.getRequestURL().toString();
        String queryString = req.getQueryString();
        if (queryString != null) {
            reqUrl += "?" + queryString;
        }
        return reqUrl;
    }
    
    public static boolean Credentials(SwordSessionStructure holder, HttpServletRequest request) {
        
    	String usernamePassword = getUsernamePassword(request);

        if ((usernamePassword != null) && (!usernamePassword.equals(""))) {
            int p = usernamePassword.indexOf(":");
            if (p != -1) {
            	holder.userName = usernamePassword.substring(0, p);
            	holder.password = usernamePassword.substring(p + 1);
                return true;
            }
        }
        return false;
    }
    /**
     * Utility method to return the username and password (separated by a colon
     * ':')
     *
     * @param request
     * @return The username and password combination
     */
    public static String getUsernamePassword(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null) {
                StringTokenizer st = new StringTokenizer(authHeader);
                if (st.hasMoreTokens()) {
                    String basic = st.nextToken();
                    if (basic.equalsIgnoreCase("Basic")) {
                        String credentials = st.nextToken();
                        String userPass = new String(Base64
                                .decode(credentials.getBytes()));
                        return userPass;
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.debug(e.toString());
        }
        return null;
    }    
	
} // ====================================================== //
