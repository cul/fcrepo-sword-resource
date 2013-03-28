package edu.columbia.libraries.sword;

public class SWORDException extends Exception {

	public SWORDException(String message, Throwable e) {
		super(message, e);
	}
	
	public SWORDException(String message) {
		super(message);
	}
}
