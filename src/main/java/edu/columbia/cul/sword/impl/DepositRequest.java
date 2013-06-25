package edu.columbia.cul.sword.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.columbia.cul.sword.HttpHeaders;
import edu.columbia.cul.sword.SWORDException;
import edu.columbia.cul.sword.SWORDRequest;
import edu.columbia.cul.sword.SWORDResource;


public class DepositRequest extends SWORDRequest {
	
	private static final Logger log = LoggerFactory.getLogger(DepositRequest.class.getName());

    public static Pattern FILENAME = Pattern.compile(".*filename=(.*?)((; *.*)|( +)){0,1}");
    private String m_depositId;
    private String m_collectionId;
    private String m_contentType;
    private String m_contentDisposition;
    private String m_md5;
    private int m_contentLength;
    private String m_packaging;
    private boolean m_noOp;
    private String m_slug;
    private boolean m_verbose;
    private String m_fileName;
    private InputStream m_file;

    public DepositRequest(HttpServletRequest request) throws SWORDException {
        
    	super(request);
        
        m_contentDisposition = request.getHeader(HttpHeaders.CONTENT_DISPOSITION.toString());
        m_md5 = request.getHeader(org.purl.sword.base.HttpHeaders.CONTENT_MD5.toString());
        m_contentType = request.getContentType();
        String len = request.getHeader(HttpHeaders.CONTENT_LENGTH);
        if (len != null && !"".equals(len)) m_contentLength = Integer.parseInt(len);
        m_packaging = request.getHeader(org.purl.sword.base.HttpHeaders.X_PACKAGING);
        m_noOp = booleanValue(request.getHeader(org.purl.sword.base.HttpHeaders.X_NO_OP), "noOp");
        m_verbose = booleanValue(request.getHeader(org.purl.sword.base.HttpHeaders.X_VERBOSE), "verbose");
        m_slug = request.getHeader(org.purl.sword.base.HttpHeaders.SLUG);

        log.debug("m_contentDisposition: {}", m_contentDisposition);
        log.debug("m_md5:                {}", m_md5);
        log.debug("m_contentType:        {}", m_contentType);
        log.debug("m_contentLength:      {}", m_contentLength);
        log.debug("m_packaging:          {}", m_packaging);
        log.debug("m_noOp:               {}", m_noOp);
        log.debug("m_verbose:            {}", m_verbose);
        log.debug("m_slug:               {}", m_slug);
    }
    
    public void setContentType(String contentType) {
    	m_contentType = contentType;
    }

    public String getContentType() {
        return m_contentType;
    }

    public void setContentLength(int contentLength) {
        m_contentLength = contentLength;
    }

    public int getContentLength(){
        return m_contentLength;
    }

    public void setCollection(String id) {
      m_collectionId = id;
    }

    public String getCollection(){
        return m_collectionId;
    }
    
    public void setDepositId(String id) {
    	m_depositId = id;
    }
    
    public String getDepositId() {
    	return m_depositId;
    }

    public void setPackaging(String packaging) {
        m_packaging = packaging;
    }

    public String getPackaging() {
        return m_packaging;
    }

    public void setFile(InputStream file) {
        m_file = file;
    }

    public void setFile(File file) throws SWORDException {
        try {
			m_file = (new FileInputStream(file));
		} catch (FileNotFoundException e) {
			throw new SWORDException(SWORDException.IO_ERROR, e);
		}
    }

    public InputStream getFile() {
        return m_file;
    }

    public void setContentDisposition(String contentDisposition) {
        m_contentDisposition = contentDisposition;
        if (contentDisposition != null) {
            Matcher m = FILENAME.matcher(contentDisposition);
            if (m.matches() && m.groupCount() > 2) {
                m_fileName = m.group(1);
            }
        }
    }

    public String getContentDisposition() {
        return m_contentDisposition;
    }

    public void setMD5(String md5) {
        m_md5 = md5;
    }

    public String getMD5() {
        return m_md5;
    }

    public void setNoOp(boolean noOp) {
        m_noOp = noOp;
    }

    public boolean isNoOp() {
        return m_noOp;
    }

    public void setSlug(String slug) {
        m_slug = slug;
    }

    public String getSlug() {
        return m_slug;
    }

    public void setVerbose(boolean verbose) {
        m_verbose = verbose;
    }

    public boolean isVerbose() {
        return m_verbose;
    }

    public String getFileName() {
        return m_fileName;
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
