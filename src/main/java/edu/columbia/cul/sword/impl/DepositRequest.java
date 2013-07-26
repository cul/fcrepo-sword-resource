package edu.columbia.cul.sword.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.columbia.cul.sword.HttpHeaders;
import edu.columbia.cul.sword.SWORDRequest;
import edu.columbia.cul.sword.exceptions.SWORDException;
import edu.columbia.cul.sword.utils.SwordHttpHeaders;

/* will be removed */
public class DepositRequest extends SWORDRequest {
	
	private static final Logger log = LoggerFactory.getLogger(DepositRequest.class.getName());

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
    private InputStream file;

    public DepositRequest(HttpServletRequest request) throws SWORDException {
        
    	super(request);
        
        contentDisposition = request.getHeader(HttpHeaders.CONTENT_DISPOSITION.toString());
        
        fileName = (contentDisposition == null ? null : contentDisposition.replace("filename=", ""));
        md5 = request.getHeader(org.purl.sword.base.HttpHeaders.CONTENT_MD5.toString());
        contentType = request.getContentType();
        String len = request.getHeader(HttpHeaders.CONTENT_LENGTH);
        if (len != null && !"".equals(len)) contentLength = Integer.parseInt(len);
        packaging = request.getHeader(org.purl.sword.base.HttpHeaders.X_PACKAGING);
        noOp = booleanValue(request.getHeader(org.purl.sword.base.HttpHeaders.X_NO_OP), "noOp");
        verbose = booleanValue(request.getHeader(HttpHeaders.X_VERBOSE), "verbose");
        slug = request.getHeader(org.purl.sword.base.HttpHeaders.SLUG);

        
        
        
        log.info("ipAddress:           {}", getIPAddress());
        log.info("Content-Disposition: {}", getContentDisposition());
        log.info("Content-MD5:         {}", getMD5());
        
        log.info("Content-Type:        {}", getContentType());
        log.info("contentLength:       {}", getContentLength());

        log.info("packaging:           {}", getPackaging());
        log.info("isNoOp:              {}", isNoOp());
        log.info("isVerbose:           {}", isVerbose());
        log.info("slug:                {}", getSlug());
        
        log.info("isProxied:           {}", isProxied()); 
        log.info("onBehalfOf():        {}", getOnBehalfOf());
        log.info("location:            {}", getLocation());
        
        log.info("userAgent:           {}", this.getUserAgent());
        log.info("userAgent:           {}", this.getUserAgent());
        
        log.info("authenticated:       {}", authenticated());
        
        
        log.info("Authorization:       {}", request.getHeader("Authorization"));
        
        
        System.out.println("1 ==============================================================");
        
        Enumeration<String>names = request.getParameterNames();
        while(names.hasMoreElements()){
        	String name = names.nextElement();
        	System.out.println("======= Parameter name: " + name + " = " + request.getParameter(name));
        }   

        System.out.println("2 ==============================================================");
        
        names = request.getAttributeNames();
        while(names.hasMoreElements()){
        	String name = names.nextElement();
        	System.out.println("======= Attribute name: " + name + " = " + request.getAttribute(name));
        }
        
        System.out.println("3 ==============================================================");
        
        System.out.println("===++==   Header name: " + HttpHeaders.USER_AGENT + " = " + request.getHeader(HttpHeaders.USER_AGENT));
        System.out.println("===++==   Header name: " + HttpHeaders.USER_AGENT + " = " + request.getHeader(HttpHeaders.USER_AGENT));
        System.out.println("===+!+==   Header name: " + SwordHttpHeaders.USER_AGENT.getName() + " = " + SwordHttpHeaders.USER_AGENT.getValue(request));
        
        
        
        System.out.println("===++== Authorization: " + " = " + request.getHeader("Authorization"));
        System.out.println("===++== WWW-Authenticate: " + " = " + request.getHeader("WWW-Authenticate"));
        

        names = request.getHeaderNames();
        while(names.hasMoreElements()){
        	String name = names.nextElement();
        	System.out.println("======= Header name: " + name + " = " + request.getHeader(name));
        	
        }

        System.out.println("4 ==============================================================");
        
        
        log.info("userName:            {}", getUserName());
        log.info("password:            {}", getPassword());
    }
    
    public void setContentType(String contentType) {
    	this.contentType = contentType;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentLength(int contentLength) {
    	this.contentLength = contentLength;
    }

    public int getContentLength(){
        return contentLength;
    }

    public void setCollection(String id) {
    	this.collectionId = id;
    }

    public String getCollection(){
        return collectionId;
    }
    
    public void setDepositId(String id) {
    	this.depositId = id;
    }
    
    public String getDepositId() {
    	return depositId;
    }

    public void setPackaging(String packaging) {
    	this.packaging = packaging;
    }

    public String getPackaging() {
        return packaging;
    }

    public void setFile(InputStream file) {
    	this.file = file;
    }

    public void setFile(File file) throws SWORDException {
        try {
        	this.file = (new FileInputStream(file));
		} catch (FileNotFoundException e) {
			throw new SWORDException(SWORDException.IO_ERROR, e);
		}
    }

    public InputStream getFile() {
        return file;
    }

    public void setContentDisposition(String contentDisposition) {
    	this.contentDisposition = contentDisposition;
    }

    public String getContentDisposition() {
        return contentDisposition;
    }

    public void setMD5(String md5) {
    	this.md5 = md5;
    }

    public String getMD5() {
        return md5;
    }

    public void setNoOp(boolean noOp) {
    	this.noOp = noOp;
    }

    public boolean isNoOp() {
        return noOp;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getSlug() {
        return slug;
    }

    public void setVerbose(boolean verbose) {
    	this.verbose = verbose;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public String getFileName() {
        return fileName;
    }
    
    private static boolean booleanValue(String input, String field) throws SWORDException {
    	boolean candidate = Boolean.valueOf(input);
    	if (candidate && !"true".equalsIgnoreCase(input)) {
    		// message = "Bad " + field + " value: " + input
            throw new SWORDException(SWORDException.ERROR_REQUEST);
    	}
    	return candidate;
    }

}
