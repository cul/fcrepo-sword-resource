package edu.columbia.libraries.sword;

import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.UriInfo;

import org.fcrepo.server.access.RepositoryInfo;
import org.fcrepo.utilities.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.columbia.libraries.sword.xml.entry.Generator;


public abstract class SWORDRequest {
    private static final Logger log = LoggerFactory.getLogger(SWORDRequest.class.getName());

    protected String m_userName;

    protected String m_password;

    protected String m_ipAddress;

    protected String m_location;

    protected boolean m_authenticated;

    protected String m_onBehalfOf;
    
    protected boolean m_proxied;
    
    protected UriInfo m_base;
    
    protected Generator m_generator;
    
    public SWORDRequest() {
    	
    }

    public SWORDRequest(HttpServletRequest request) {
        m_authenticated = SWORDRequest.setCredentials(this, request);
        setIPAddress(request.getRemoteAddr());
        setLocation(getURL(request));
        String onBehalfOf = request.getHeader(org.purl.sword.base.HttpHeaders.X_ON_BEHALF_OF
                .toString());
        if (onBehalfOf == null) onBehalfOf = getUserName();
        setOnBehalfOf(onBehalfOf);
        setProxied(m_onBehalfOf != null && !(m_onBehalfOf.equals(m_userName)));
    }

    public String getUserName() {
        return m_userName;
    }

    public String getPassword() {
        return m_password;
    }
    
    public void setOnBehalfOf(String onBehalfOf) {
    	m_onBehalfOf = onBehalfOf;
    }

    public String getOnBehalfOf() {
        return m_onBehalfOf;
    }

    public String getLocation() {
        return m_location;
    }

    public String getIPAddress() {
        return m_ipAddress;
    }

    public boolean authenticated() {
        return m_authenticated;
    }

    public void setUserName(String userName) {
        m_userName = userName;
        m_authenticated = true;
    }

    public void setPassword(String password) {
        m_password = password;
    }

    public void setIPAddress(String ipAddress) {
        m_ipAddress = ipAddress;
    }

    public void setLocation(String location) {
        m_location = location;
    }
    
    public void setProxied(boolean proxied) {
    	m_proxied = proxied;
    }
    
    public boolean isProxied() {
    	return m_proxied;
    }
    
    public void setBaseUri(UriInfo base) {
    	m_base = base;
    }
    
    public UriInfo getBaseUri(){
    	return m_base;
    }
    
    public void setGenerator(Generator generator) {
    	m_generator = generator;
    }
    
    public Generator getGenerator() {
    	return m_generator;
    }
    
    /**
     * Utility method to set up generator
     * @param info
     */
    public void setGenerator(RepositoryInfo info) {
    	m_generator = new Generator(info.repositoryName, info.repositoryVersion);
    }

    public static boolean setCredentials(SWORDRequest sr, HttpServletRequest request) {
        String usernamePassword = getUsernamePassword(request);
        if ((usernamePassword != null) && (!usernamePassword.equals(""))) {
            int p = usernamePassword.indexOf(":");
            if (p != -1) {
                sr.setUserName(usernamePassword.substring(0, p));
                sr.setPassword(usernamePassword.substring(p + 1));
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
    private static String getUsernamePassword(HttpServletRequest request) {
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
            log.debug(e.toString());
        }
        return null;
    }

    /**
     * Utility method to construct the URL called for this Servlet
     *
     * @param req The request object
     * @return The URL
     */
    protected static String getURL(HttpServletRequest req) {
        String reqUrl = req.getRequestURL().toString();
        String queryString = req.getQueryString();
        if (queryString != null) {
            reqUrl += "?" + queryString;
        }
        return reqUrl;
    }
}
