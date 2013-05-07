package edu.columbia.cul.sword;

import java.net.URI;

public class SWORDErrorInfo {
    public final URI error;
    public final int status;
    SWORDErrorInfo(URI error, int status) {
    	this.error = error;
    	this.status = status;
    }
}
