package edu.columbia.cul.sword.holder;

import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;

import org.fcrepo.utilities.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.columbia.cul.sword.HttpHeaders;

public class SwordInfoInitializer {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SwordInfoInitializer.class.getName());

	public static SwordInfoHolder makeNewIfoHolder(HttpServletRequest request){
		
		SwordInfoHolder holder = new SwordInfoHolder();
	
		holder.setUserAgent(request.getHeader(HttpHeaders.USER_AGENT));
		holder.setOnBehalfOf(request.getHeader(HttpHeaders.X_ON_BEHALF_OF));
		holder.setIpAddress(request.getRemoteAddr());
		holder.setLocation(getURL(request));
		
		holder.setAuthenticated(setCredentials(holder, request));
		holder.setProxied(holder.getOnBehalfOf() != null && !(holder.getOnBehalfOf().equals(holder.getUserName())));
		
		holder.setContentDisposition(request.getHeader(HttpHeaders.CONTENT_DISPOSITION));
		holder.setFileName(holder.getContentDisposition() == null ? null : holder.getContentDisposition().replace("filename=", ""));
		
		holder.setMd5(request.getHeader(HttpHeaders.CONTENT_MD5));
		holder.setContentType(request.getContentType());
		
		holder.setContentLength(request.getHeader(HttpHeaders.CONTENT_LENGTH) == null ? 0 : Integer.parseInt(request.getHeader(HttpHeaders.CONTENT_LENGTH)));
		
		holder.setPackaging(request.getHeader(HttpHeaders.X_PACKAGING));
		holder.setNoOp(Boolean.valueOf(request.getHeader(HttpHeaders.X_NO_OP)));
		
		holder.setVerbose(Boolean.valueOf(request.getHeader(HttpHeaders.X_VERBOSE)));
		holder.setSlug(request.getHeader(HttpHeaders.SLUG));
		
		
		return holder;
	}
	
    public static String getURL(HttpServletRequest req) {
        String reqUrl = req.getRequestURL().toString();
        String queryString = req.getQueryString();
        if (queryString != null) {
            reqUrl += "?" + queryString;
        }
        return reqUrl;
    }
    
    public static boolean setCredentials(SwordInfoHolder holder, HttpServletRequest request) {
        
    	String usernamePassword = getUsernamePassword(request);

        if ((usernamePassword != null) && (!usernamePassword.equals(""))) {
            int p = usernamePassword.indexOf(":");
            if (p != -1) {
            	holder.setUserName(usernamePassword.substring(0, p));
            	holder.setPassword(usernamePassword.substring(p + 1));
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
