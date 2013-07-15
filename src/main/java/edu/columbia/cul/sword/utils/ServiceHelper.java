package edu.columbia.cul.sword.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.commons.httpclient.HttpStatus;
import org.fcrepo.server.Context;
import org.fcrepo.server.ReadOnlyContext;

import edu.columbia.cul.sword.SwordConstants;
import edu.columbia.cul.sword.exceptions.SWORDException;
import edu.columbia.cul.sword.impl.DepositRequest;
import edu.columbia.cul.sword.xml.SwordError;
import edu.columbia.cul.sword.xml.entry.Entry;
import edu.columbia.cul.sword.xml.entry.Link;

public class ServiceHelper implements SwordConstants {
	
	public static Response makeResutResponce(Entry entry){

		String location = null;

        for (Link link: entry.getLinks()){
        	if (link.isDescription()) location = link.getHref().toString();
        }

        ResponseBuilder responseBuilder  = Response.status(HttpStatus.SC_CREATED);
        
        if (location != null) { 
        	responseBuilder.header(HttpHeaders.LOCATION, location); }
            responseBuilder.header(HttpHeaders.CONTENT_TYPE, ATOM_CONTENT_TYPE);
            responseBuilder.entity(entry);
        
        return responseBuilder.build();
	}
	
	
//	public static Response processException(Exception e){
//		
//		
//		
//	}
	
    public static Response errorResponse(URI errorURI, int status, String summary, HttpServletRequest request) {
        
    	SwordError sed = new SwordError();
        sed.reason = errorURI;
        sed.title = "ERROR";
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat zulu = new SimpleDateFormat(UTC_DATE_FORMAT);
        String serializedDate = zulu.format(calendar.getTime());
        sed.updated = serializedDate;
        sed.summary = summary;
        if (request.getHeader(HttpHeaders.USER_AGENT) != null) {
            sed.setUserAgent(request.getHeader(HttpHeaders.USER_AGENT));
        }
        
        Response response = Response.status(status)
                                    .header(HttpHeaders.CONTENT_TYPE, ATOM_CONTENT_TYPE)
                                    .entity(sed).build();
        return response;
    }	
	
	public static File receiveFile(File m_tempDir, 
			                       DepositRequest deposit, 
			                       HttpServletRequest m_servletRequest, 
			                       AtomicInteger counter, 
			                       int m_maxUpload) throws SWORDException{
		
        File tempFile = new File(m_tempDir, "SWORD-" + deposit.getIPAddress() + "_" + counter.addAndGet(1));  
        
        //LOGGER.debug("Temp file: {}", tempFile.getAbsolutePath());

        InputStream in = null;
        OutputStream out = null;

            try {
                out = new FileOutputStream(tempFile);
                in = m_servletRequest.getInputStream();
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > -1){
                    out.write(buf, 0, len);
                }
            } catch (IOException e) {
            	
                throw new SWORDException(SWORDException.IO_ERROR.error,
                                         HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                                         e.getMessage());
                
            } finally {
            	try {
            		if (out != null) {
            			out.flush();
            			out.close();
            		}
            		if (in != null ) in.close();
            	} catch (IOException e) {
            		e.printStackTrace();
            	}
            }
            
            long fLen = tempFile.length();

            if ((m_maxUpload != -1) && (fLen > m_maxUpload)) {
            	
            	String errMsg = "Maximum upload size (" + m_maxUpload + ") exceeded by input (" + fLen + ")";
            	throw new SWORDException(SWORDException.MAX_UPLOAD_SIZE_EXCEEDED.error,
				                         HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE,
				                         errMsg);
            }
            
		return tempFile;

	}
	

} // ================================================= //
