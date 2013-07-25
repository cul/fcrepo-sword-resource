package edu.columbia.cul.sword.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.httpclient.HttpStatus;
import org.fcrepo.common.Constants;
import org.fcrepo.server.Context;
import org.fcrepo.server.ReadOnlyContext;
import org.fcrepo.server.storage.DOManager;
import org.fcrepo.server.storage.DOReader;
import org.fcrepo.server.storage.types.RelationshipTuple;
import org.fcrepo.server.utilities.DCFields;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.columbia.cul.fcrepo.Utils;
import edu.columbia.cul.sword.SWORDResource;
import edu.columbia.cul.sword.SwordConstants;
import edu.columbia.cul.sword.exceptions.SWORDException;
import edu.columbia.cul.sword.holder.SwordSessionStructure;
import edu.columbia.cul.sword.impl.DepositRequest;
import edu.columbia.cul.sword.xml.SwordError;
import edu.columbia.cul.sword.xml.entry.Entry;
import edu.columbia.cul.sword.xml.entry.Generator;
import edu.columbia.cul.sword.xml.entry.Link;

public class ServiceHelper implements SwordConstants {

	private static final Logger LOGGER = LoggerFactory.getLogger(ServiceHelper.class.getName());
	public static String DEFAULT_LABEL = "Object created via SWORD Deposit";
	
	public static Response makeResutResponce(Entry entry) {

		String location = null;

        for (Link link: entry.getLinks()){
        	if (link.isDescription()) location = link.getHref().toString();
        }
        
        System.out.println("=============== location: " + location);

        ResponseBuilder responseBuilder  = Response.status(HttpStatus.SC_CREATED);
        
        if (location != null) { 
        	responseBuilder.header(HttpHeaders.LOCATION, location); }
            responseBuilder.header(HttpHeaders.CONTENT_TYPE, ATOM_CONTENT_TYPE);
            responseBuilder.entity(entry);

        
        Response response =  responseBuilder.build();
        return response;
	}
	
	
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
			                       HttpServletRequest m_servletRequest, 
			                       AtomicInteger counter, 
			                       int m_maxUpload) throws SWORDException{
		
        File tempFile = new File(m_tempDir, "SWORD_" + counter.addAndGet(1));  

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
	
	public static String getRelationship(Set<RelationshipTuple> relationships){

        for (RelationshipTuple rel: relationships) {
        	return rel.object;
        }
		
		return null;
	}	

	public static Entry makeEntry(SwordSessionStructure swordSession, DCFields dcf, String contentType, String packaging) {
		
		Entry resultEntry = new Entry(swordSession.depositId);
		resultEntry.treatment = DEFAULT_LABEL;
		resultEntry.setDCFields(dcf);
		resultEntry.setPackaging(packaging);
		
		String descUri = SwordUrlUtils.makeDescriptionUrl(swordSession);
		String contentUri = SwordUrlUtils.makeContentUrl(swordSession);

		resultEntry.addEditLink(descUri.toString());
		//result.addEditMediaLink(mediaUri.toString());
		resultEntry.setContent(contentUri.toString(), contentType);
		return resultEntry;
	}


	public static Entry makeEntry(SwordSessionStructure swordSession) throws SWORDException {

		//m_generator = new Generator(info.repositoryBaseURL, info.repositoryVersion);
		
		try {

			if (!swordSession.getDoManager().objectExists(swordSession.depositId)) {
				throw new SWORDException(SWORDException.FEDORA_NO_OBJECT);
			}
	
			DOReader reader = swordSession.getDoManager().getReader(false, swordSession.fedoraContext, swordSession.depositId);
			
			String packaging = getRelationship(reader.getRelationships(SwordConstants.SWORD.PACKAGING, null));
			String contentType = getRelationship(reader.getRelationships(SwordConstants.SWORD.CONTENT_TYPE, null));

			return makeEntry(swordSession, Utils.getDCFields(reader), contentType, packaging);
			
		} catch (Exception e) {
			throw new SWORDException(SWORDException.FEDORA_ERROR, e);
		}
		
	}	
	
	
//	public static Entry makeEntry(DepositRequest deposit, DOManager doManager, org.fcrepo.server.Context context) throws SWORDException {
//
//		try {
//
//			if (!doManager.objectExists(deposit.getDepositId())) {
//				throw new SWORDException(SWORDException.FEDORA_NO_OBJECT);
//			}
//	
//			DOReader reader = doManager.getReader(false, context, deposit.getDepositId());
//			
//			String packaging = getRelationship(reader.getRelationships(SwordConstants.SWORD.PACKAGING, null));
//			String contentType = getRelationship(reader.getRelationships(SwordConstants.SWORD.CONTENT_TYPE, null));
//
//			return makeEntry(deposit, Utils.getDCFields(reader), contentType, packaging);
//			
//		} catch (Exception e) {
//			throw new SWORDException(SWORDException.FEDORA_ERROR, e);
//		}
//		
//	}	
	

	public static Context getContext(boolean isProxied, String onBehalfOf, HttpServletRequest servletRequest, Logger logger) throws SWORDException {
		Context authzContext = null;
		if (isProxied){
		    // do some authZ to see if this user is allowed to proxy
		    try {
				authzContext = ReadOnlyContext.getContext(servletRequest.getProtocol(), onBehalfOf, null, true);
				logger.debug("authzContext is proxied");
			} catch (Exception e) {
				throw new SWORDException(SWORDException.FEDORA_ERROR, e);
			}
		} else {
			authzContext = ReadOnlyContext.getContext(Constants.HTTP_REQUEST.REST.uri, servletRequest);
			logger.debug("authzContext is not proxied");
		}
		
		return authzContext;
	}	
	
	public static String verifyTempDirectoryName(String tempDirStr) {
		
    	if (tempDirStr == null || tempDirStr.isEmpty()) {
    		tempDirStr = System.getProperty("java.io.tmpdir");
    	}
    	
    	if (!tempDirStr.endsWith(System.getProperty("file.separator"))){
    		tempDirStr += System.getProperty("file.separator");
    	}
    	
    	return tempDirStr;
	}
	
	public static File createTempDirectory(String tempDirStr) {

    	File tempDir = new File(tempDirStr);
    	
    	if (!tempDir.exists()) {
    		if (!tempDir.mkdirs()) {
    			String errMsg = "Upload directory did not exist and I can't create it. " + tempDir.getPath();
        		LOGGER.error(errMsg);
    			throw new IllegalArgumentException(errMsg);
    		}
    	}
    	
    	if (!tempDir.isDirectory()) {
    		String errMsg = "Upload temporary directory is not a directory: " + tempDir.getPath();
    		LOGGER.error(errMsg);
    		throw new IllegalArgumentException(errMsg);
    	}
    	
    	if (!tempDir.canWrite()) {
    		String errMsg = "Upload temporary directory cannot be written to: " + tempDir.getPath();
    		LOGGER.error(errMsg);
    		throw new IllegalArgumentException(errMsg);
    	}
    	
    	return tempDir;
	}
	
	
	public static int makeMaxUploadSize(String maxUploadSize) {

		int max = -1;

		if (maxUploadSize == null || maxUploadSize.isEmpty() || maxUploadSize.equals("-1")) {
			LOGGER.warn("No maxUploadSize set, so setting max file upload size to unlimited.");
		} else {
			try {
				max = Integer.parseInt(maxUploadSize);
				LOGGER.info("Setting max file upload size to " + max);
			} catch (NumberFormatException nfe) {
				LOGGER.warn("maxUploadSize not a number, so setting max file upload size to unlimited.");
			}
		}

		return max;
	}

} // ================================================= //
