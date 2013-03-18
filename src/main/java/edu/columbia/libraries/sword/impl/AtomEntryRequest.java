package edu.columbia.libraries.sword.impl;

import javax.servlet.http.HttpServletRequest;

import edu.columbia.libraries.sword.SWORDRequest;


public class AtomEntryRequest extends SWORDRequest {

    public AtomEntryRequest(HttpServletRequest request) {
        super(request);
    }

}
