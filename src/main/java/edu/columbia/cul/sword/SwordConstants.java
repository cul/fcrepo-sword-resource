package edu.columbia.cul.sword;

import java.util.concurrent.atomic.AtomicInteger;

import org.fcrepo.server.ReadOnlyContext;

public interface SwordConstants {
	
	public static final SwordNamespace SWORD = new SwordNamespace();
	
    public static final String ATOM_CONTENT_TYPE = "application/atom+xml; charset=UTF-8";
    public static final String ATOMSVC_CONTENT_TYPE = "application/atomsvc+xml; charset=UTF-8";
    public static final String UTC_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    public static final String DEFAULT_REALM_HEADER = "Basic realm=\"Fedora Repository Server\"";
     

    public static final String SERVICEDOCUMENT = "servicedocument";
	
}
