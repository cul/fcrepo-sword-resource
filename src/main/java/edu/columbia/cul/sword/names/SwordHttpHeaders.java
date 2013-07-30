package edu.columbia.cul.sword.names;

import javax.servlet.http.HttpServletRequest;

import org.purl.sword.base.HttpHeaders;

public enum SwordHttpHeaders {
	
	CONTENT_MD5(HttpHeaders.CONTENT_MD5), 
	CONTENT_LENGTH(HttpHeaders.CONTENT_LENGTH), 
	X_ON_BEHALF_OF(HttpHeaders.X_ON_BEHALF_OF),
	X_PACKAGING(HttpHeaders.X_PACKAGING),
	X_VERBOSE(HttpHeaders.X_VERBOSE),
	X_NO_OP(HttpHeaders.X_NO_OP),
	X_CORRUPT(HttpHeaders.X_CORRUPT),
	X_ERROR_CODE(HttpHeaders.X_ERROR_CODE),
	USER_AGENT(HttpHeaders.USER_AGENT),
	SLUG(HttpHeaders.SLUG),
	CONTENT_DISPOSITION(HttpHeaders.CONTENT_DISPOSITION);
	
	private String httpHeaderName;
	private String swordName;
	
	private SwordHttpHeaders(String httpHeaderName) {
		this.httpHeaderName = httpHeaderName;
	}
	
//	private SwordHttpHeaders(String httpHeaderName, String swordName) {
//		this.httpHeaderName = httpHeaderName;
//		this.swordName = swordName;
//	}	
	
	public String getValue(HttpServletRequest request){
		return request.getHeader(httpHeaderName);
	}
	
	public String getName(){
		return httpHeaderName;
	}

} // ====================================================== //

