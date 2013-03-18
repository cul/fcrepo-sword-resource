package edu.columbia.libraries.fcrepo;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.fcrepo.utilities.Base64;
import org.purl.sword.atom.Summary;
import org.purl.sword.atom.Title;
import org.purl.sword.base.AtomDocumentRequest;
import org.purl.sword.base.SWORDErrorDocument;
import org.purl.sword.base.ServiceDocumentRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AbstractDepositResource {
    private static final Logger log = LoggerFactory.getLogger(SwordResource.class.getName());

    public static final String ATOM_CONTENT_TYPE = "application/atom+xml; charset=UTF-8";
    public static final String ATOMSVC_CONTENT_TYPE = "application/atomsvc+xml; charset=UTF-8";
    public static final String UTC_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    public static final String DEFAULT_REALM_HEADER = "Basic realm=\"Fedora Repository Server\"";

    public static Response authnRequiredResponse(String realm) {
        String headerValue;
        if (realm == null) headerValue = DEFAULT_REALM_HEADER;
        else headerValue = "Basic realm=\"" + realm + "\"";
        Response response = Response.status(401).header("WWW-Authenticate", headerValue).build();
        return response;
    }

    public static Response errorResponse(String errorURI, int status, String summary, HttpServletRequest request) {
        SWORDErrorDocument sed = new SWORDErrorDocument(errorURI);
        Title title = new Title();
        title.setContent("ERROR");
        sed.setTitle(title);
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat zulu = new SimpleDateFormat(UTC_DATE_FORMAT);
        String serializeddate = zulu.format(calendar.getTime());
        sed.setUpdated(serializeddate);
        Summary sum = new Summary();
        sum.setContent(summary);
        sed.setSummary(sum);
        if (request.getHeader(HttpHeaders.USER_AGENT) != null) {
            sed.setUserAgent(request.getHeader(HttpHeaders.USER_AGENT));
        }
        String entity = sed.marshall().toXML();
        Response response = Response.status(status)
                                    .header(HttpHeaders.CONTENT_TYPE, ATOM_CONTENT_TYPE)
                                    .entity(entity).build();
        return response;
    }

    public static boolean setCredentials(ServiceDocumentRequest sdr, HttpServletRequest request) {
        String usernamePassword = getUsernamePassword(request);
        if ((usernamePassword != null) && (!usernamePassword.equals(""))) {
            int p = usernamePassword.indexOf(":");
            if (p != -1) {
                sdr.setUsername(usernamePassword.substring(0, p));
                sdr.setPassword(usernamePassword.substring(p + 1));
                return true;
            }
        }
        return false;
    }

    public static boolean setCredentials(AtomDocumentRequest adr, HttpServletRequest request) {
        String usernamePassword = getUsernamePassword(request);
        if ((usernamePassword != null) && (!usernamePassword.equals(""))) {
            int p = usernamePassword.indexOf(":");
            if (p != -1) {
                adr.setUsername(usernamePassword.substring(0, p));
                adr.setPassword(usernamePassword.substring(p + 1));
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

}
