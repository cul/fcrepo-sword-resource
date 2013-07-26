package edu.columbia.cul.sword.impl;

/* will be removed */
import javax.servlet.http.HttpServletRequest;

import edu.columbia.cul.sword.SWORDRequest;


public class ServiceDocumentRequest extends SWORDRequest {

	public ServiceDocumentRequest(){
		super();
	}
	
    public ServiceDocumentRequest(HttpServletRequest request) {
        super(request);
    }

}
