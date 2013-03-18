package edu.columbia.libraries.sword.impl;

import javax.servlet.http.HttpServletRequest;

import edu.columbia.libraries.sword.SWORDRequest;


public class ServiceDocumentRequest extends SWORDRequest {

    public ServiceDocumentRequest(HttpServletRequest request) {
        super(request);
    }

}
