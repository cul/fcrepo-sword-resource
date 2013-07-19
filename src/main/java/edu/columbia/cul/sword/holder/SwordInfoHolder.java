package edu.columbia.cul.sword.holder;

import java.io.File;

import javax.ws.rs.core.UriInfo;

import edu.columbia.cul.sword.xml.entry.Generator;

public class SwordInfoHolder {
	
    protected String userName;
    protected String password;
    protected String ipAddress;
    protected String location;
    protected boolean authenticated;
    protected String onBehalfOf;   
    protected boolean proxied;   
    protected UriInfo base;  
    protected Generator generator;  
    protected String userAgent;
	
    private String depositId;
    private String collectionId;
    private String contentType;
    private String contentDisposition;
    private String md5;
    private int contentLength;
    private String packaging;
    private boolean noOp;
    private String slug;
    private boolean verbose;
    private String fileName;
    private File file;
    
	public String getUserName() {
		return userName;
	}
	
	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	public String getPassword() {
		return password;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	public String getIpAddress() {
		return ipAddress;
	}
	
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}
	
	public String getLocation() {
		return location;
	}
	
	public void setLocation(String location) {
		this.location = location;
	}
	
	public boolean isAuthenticated() {
		return authenticated;
	}
	
	public void setAuthenticated(boolean authenticated) {
		this.authenticated = authenticated;
	}
	
	public String getOnBehalfOf() {
		return onBehalfOf;
	}
	
	public void setOnBehalfOf(String onBehalfOf) {
		this.onBehalfOf = onBehalfOf;
	}
	
	public boolean isProxied() {
		return proxied;
	}
	
	public void setProxied(boolean proxied) {
		this.proxied = proxied;
	}
	
	public UriInfo getBase() {
		return base;
	}
	
	public void setBase(UriInfo base) {
		this.base = base;
	}
	
	public Generator getGenerator() {
		return generator;
	}
	
	public void setGenerator(Generator generator) {
		this.generator = generator;
	}
	
	public String getUserAgent() {
		return userAgent;
	}
	
	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}
	
	public String getDepositId() {
		return depositId;
	}
	
	public void setDepositId(String depositId) {
		this.depositId = depositId;
	}
	
	public String getCollectionId() {
		return collectionId;
	}
	
	public void setCollectionId(String collectionId) {
		this.collectionId = collectionId;
	}
	
	public String getContentType() {
		return contentType;
	}
	
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	
	public String getContentDisposition() {
		return contentDisposition;
	}
	
	public void setContentDisposition(String contentDisposition) {
		this.contentDisposition = contentDisposition;
	}
	
	public String getMd5() {
		return md5;
	}
	
	public void setMd5(String md5) {
		this.md5 = md5;
	}
	
	public int getContentLength() {
		return contentLength;
	}
	
	public void setContentLength(int contentLength) {
		this.contentLength = contentLength;
	}
	
	public String getPackaging() {
		return packaging;
	}
	
	public void setPackaging(String packaging) {
		this.packaging = packaging;
	}
	
	public boolean isNoOp() {
		return noOp;
	}
	
	public void setNoOp(boolean noOp) {
		this.noOp = noOp;
	}
	
	public String getSlug() {
		return slug;
	}
	
	public void setSlug(String slug) {
		this.slug = slug;
	}
	
	public boolean isVerbose() {
		return verbose;
	}
	
	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}
	
	public String getFileName() {
		return fileName;
	}
	
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	public File getFile() {
		return file;
	}
	
	public void setFile(File file) {
		this.file = file;
	}
    

} // ====================================================== //
