package edu.columbia.libraries.sword.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;

import org.purl.sword.base.Deposit;
import org.purl.sword.base.ErrorCodes;
import org.purl.sword.base.SWORDErrorException;

import edu.columbia.libraries.sword.SWORDRequest;


public class DepositRequest extends SWORDRequest {

    public static Pattern FILENAME = Pattern.compile(".*filename=(.*?)((; *.*)|( +)){0,1}");
    private Deposit m_deposit;
    private String m_fileName;

    public DepositRequest(HttpServletRequest request) throws SWORDErrorException {
        super(request);
        m_deposit = new Deposit();
        m_deposit.setContentDisposition(request.getHeader(org.purl.sword.base.HttpHeaders.CONTENT_DISPOSITION.toString()));
        m_deposit.setMd5(request.getHeader(org.purl.sword.base.HttpHeaders.CONTENT_MD5.toString()));
        m_deposit.setContentType(request.getContentType());
        String len = request.getHeader(HttpHeaders.CONTENT_LENGTH);
        if (len != null && !"".equals(len)) m_deposit.setContentLength(Integer.parseInt(len));
        m_deposit.setPackaging(request.getHeader(org.purl.sword.base.HttpHeaders.X_PACKAGING));
        m_deposit.setNoOp(booleanValue(request.getHeader(org.purl.sword.base.HttpHeaders.X_NO_OP), "noOp"));
        m_deposit.setVerbose(booleanValue(request.getHeader(org.purl.sword.base.HttpHeaders.X_VERBOSE), "verbose"));
        m_deposit.setSlug(request.getHeader(org.purl.sword.base.HttpHeaders.SLUG));
        m_deposit.setLocation(super.getLocation());
        m_deposit.setIPAddress(super.getIPAddress());
        m_deposit.setOnBehalfOf(super.getOnBehalfOf());
    }
    
    public Deposit getDeposit() {
    	return m_deposit;
    }

    public void setContentType(String contentType) {
    	m_deposit.setContentType(contentType);
    }

    public String getContentType() {
        return m_deposit.getContentType();
    }

    public void setContentLength(int contentLength) {
        m_deposit.setContentLength(contentLength);
    }

    public int getContentLength(){
        return m_deposit.getContentLength();
    }

    public void setDepositID(String id) {
      m_deposit.setDepositID(id);
    }

    public String getDepositID(){
        return m_deposit.getDepositID();
    }

    public void setPackaging(String packaging) {
        m_deposit.setPackaging(packaging);
    }

    public String getPackaging() {
        return m_deposit.getPackaging();
    }

    public void setFile(InputStream file) {
        m_deposit.setFile(file);
    }

    public void setFile(File file) throws FileNotFoundException {
        m_deposit.setFile(new FileInputStream(file));
    }

    public InputStream getFile() {
        return m_deposit.getFile();
    }

    public void setContentDisposition(String contentDisposition) {
        m_deposit.setContentDisposition(contentDisposition);
        if (contentDisposition != null) {
            Matcher m = FILENAME.matcher(contentDisposition);
            if (m.matches() && m.groupCount() > 2) {
                m_fileName = m.group(1);
            }
        }
    }

    public String getContentDisposition() {
        return m_deposit.getContentDisposition();
    }

    public void setMD5(String md5) {
        m_deposit.setMd5(md5);
    }

    public String getMD5() {
        return m_deposit.getMd5();
    }

    public void setNoOp(boolean noOp) {
        m_deposit.setNoOp(noOp);
    }

    public boolean isNoOp() {
        return m_deposit.isNoOp();
    }

    public void setSlug(String slug) {
        m_deposit.setSlug(slug);
    }

    public String getSlug() {
        return m_deposit.getSlug();
    }

    public void setVerbose(boolean verbose) {
        m_deposit.setVerbose(verbose);
    }

    public boolean isVerbose() {
        return m_deposit.isVerbose();
    }

    public String getFileName() {
        return m_fileName;
    }
    
    private static boolean booleanValue(String input, String field) throws SWORDErrorException {
    	boolean candidate = Boolean.valueOf(input);
    	if (candidate && !"true".equalsIgnoreCase(input)) {
            throw new SWORDErrorException(ErrorCodes.ERROR_BAD_REQUEST,"Bad " + field + " value: " + input);
    	}
    	return candidate;
    }

}
