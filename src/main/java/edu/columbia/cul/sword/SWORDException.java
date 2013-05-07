package edu.columbia.cul.sword;

import java.net.URI;

public class SWORDException extends Exception {
	/**
	 * The supplied format is not the same as that identified in the X-Packaging header
	 *  and/or that supported by the server.
	 */
	public static final SWORDErrorInfo ERROR_CONTENT = new SWORDErrorInfo(
			URI.create("http://purl.org/net/sword/error/ErrorContent"),
			412);
	/**
	 * Checksum sent does not match the calculated checksum.
	 * The server MUST also return a status code of 412 Precondition Failed.
	 */
	public static final SWORDErrorInfo ERROR_CHECKSUM = new SWORDErrorInfo(
			URI.create("http://purl.org/net/sword/error/ErrorChecksumMismatch"),
			412);
	/**
	 * The MD5 Checksum algorithm is not available on the server
	 */
	public static final SWORDErrorInfo MD5_MISSING = new SWORDErrorInfo(
			URI.create("http://purl.org/net/sword/error/MD5Missing"),
			500);

	/**
	 * There was an I/O error on the server
	 */
	public static final SWORDErrorInfo IO_ERROR = new SWORDErrorInfo(
			URI.create("http://purl.org/net/sword/error/IOException"),
			500);
	
	/**
	 * Some parameters sent with the POST were not understood.
	 * The server MUST also return a status code of 400 Bad Request.
	 */
	public static final SWORDErrorInfo ERROR_REQUEST = new SWORDErrorInfo(
			URI.create("http://purl.org/net/sword/error/ErrorBadRequest"),
			400);
	/**
	 * Used in mediated deposit (see Part A Section 2) when the server does not know the identity of the X-On-Behalf-Of user.
	 */
	public static final SWORDErrorInfo OWNER_UNKNOWN = new SWORDErrorInfo(
			URI.create("http://purl.org/net/sword/error/TargetOwnerUnknown"),
			400);
    /**
     * Used where a client has attempted a mediated deposit, but this is not supported by the server. 
     * The server MUST also return a status code of 412 Precondition Failed.
     */
	public static final SWORDErrorInfo MEDIATION_NOT_ALLOWED = new SWORDErrorInfo(
			URI.create("http://purl.org/net/sword/error/MediationNotAllowed"),
			412);
	
	/**
	 * Used when a client has attempted to deposit a file exceeding the maximum allowable size
	 */
	public static final SWORDErrorInfo MAX_UPLOAD_SIZE_EXCEEDED = new SWORDErrorInfo(
			URI.create("http://purl.org/net/sword/error/MAX_UPLOAD_SIZE_EXCEEDED"),
			412);
	
	public static final SWORDErrorInfo FEDORA_ERROR = new SWORDErrorInfo(
			URI.create("http://purl.org/net/sword/error/fcrepo/ServerError"),
			500);
	
	public static final SWORDErrorInfo FEDORA_NO_OBJECT = new SWORDErrorInfo(
			URI.create("http://purl.org/net/sword/error/fcrepo/ObjectNotFound"),
			404);
	
	
	public final URI reason;
	public final int status;
	public String message;
	
	public SWORDException(SWORDErrorInfo reason, Throwable e) {
		super(reason.error.toString(), e); //TODO: map URIs to messages
		this.reason = reason.error;
		this.status = reason.status;
	}
	
	public SWORDException(SWORDErrorInfo reason) {
		super(reason.error.toString()); //TODO: map URIs to messages
		this.reason = reason.error;
		this.status = reason.status;
	}
}
