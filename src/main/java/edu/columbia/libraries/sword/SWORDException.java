package edu.columbia.libraries.sword;

import java.net.URI;

public class SWORDException extends Exception {
	/**
	 * The supplied format is not the same as that identified in the X-Packaging header and/or that supported by the server.
	 */
	public static final URI ERROR_CONTENT = URI.create("http://purl.org/net/sword/error/ErrorContent");
	/**
	 * Checksum sent does not match the calculated checksum.
	 * The server MUST also return a status code of 412 Precondition Failed.
	 */
	public static final URI ERROR_CHECKSUM = URI.create("http://purl.org/net/sword/error/ErrorChecksumMismatch");
	/**
	 * Some parameters sent with the POST were not understood.
	 * The server MUST also return a status code of 400 Bad Request.
	 */
	public static final URI ERROR_REQUEST = URI.create("http://purl.org/net/sword/error/ErrorBadRequest");
	/**
	 * Used in mediated deposit (see Part A Section 2) when the server does not know the identity of the X-On-Behalf-Of user.
	 */
	public static final URI OWNER_UNKNOWN = URI.create("http://purl.org/net/sword/error/TargetOwnerUnknown");
    /**
     * Used where a client has attempted a mediated deposit, but this is not supported by the server. 
     * The server MUST also return a status code of 412 Precondition Failed.
     */
	public static final URI MEDIATION_NOT_ALLOWED = URI.create("http://purl.org/net/sword/error/MediationNotAllowed");
	public static final URI FEDORA_ERROR = URI.create("http://purl.org/net/sword/error/fcrepo/ServerError");
	public static final URI FEDORA_NO_OBJECT = URI.create("http://purl.org/net/sword/error/fcrepo/ObjectNotFound");
	
	
	private final URI reason;
	public SWORDException(URI reason, Throwable e) {
		super(reason.toString(), e); //TODO: map URIs to messages and statuses
		this.reason = reason;
	}
	
	public SWORDException(URI reason) {
		super(reason.toString());
		this.reason = reason;
	}
}
