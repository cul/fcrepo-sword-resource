package edu.columbia.cul.fcrepo.sword;

import java.util.Date;
import java.util.Set;

import org.fcrepo.server.Context;
import org.fcrepo.server.errors.ServerException;
import org.fcrepo.server.storage.DOManager;
import org.fcrepo.server.storage.DOReader;
import org.fcrepo.server.storage.types.RelationshipTuple;
import org.fcrepo.server.utilities.DCFields;

import edu.columbia.cul.fcrepo.Utils;
import edu.columbia.cul.sword.SwordConstants;
import edu.columbia.cul.sword.exceptions.SWORDException;
import edu.columbia.cul.sword.holder.SwordSessionStructure;
import edu.columbia.cul.sword.utils.SwordUrlUtils;
import edu.columbia.cul.sword.xml.entry.Entry;

public abstract class FedoraUtils {
	
	public static String DEFAULT_LABEL = "Object created via SWORD Deposit";

	public static DCFields getDCFields(Context context, DOManager manager, String pid) 
	    throws SWORDException {
		try {
		if (!manager.objectExists(pid)) {
			throw new SWORDException(SWORDException.FEDORA_NO_OBJECT);
		}
		DOReader reader = manager.getReader(false, context, pid);

		DCFields dcf = new DCFields(reader.GetDatastream("DC", new Date()).getContentStream());
        return dcf;
		} catch (ServerException e) {
			throw new SWORDException(SWORDException.FEDORA_ERROR, e);
		}
	}
	
	public static boolean isMediated(Context context, DOManager manager, String pid) 
		    throws SWORDException {
		try {
			if (!manager.objectExists(pid)) {
				throw new SWORDException(SWORDException.FEDORA_NO_OBJECT);
			}
			boolean result = false;
			DOReader reader = manager.getReader(false, context, pid);
			for (RelationshipTuple rel: reader.getRelationships(SwordConstants.SWORD.MEDIATION, null)) {
				result = Boolean.valueOf(rel.object);
			}
			return result;
		} catch (ServerException e) {
			throw new SWORDException(SWORDException.FEDORA_ERROR, e);
		}
	}

	public static String getRelationship(Set<RelationshipTuple> relationships){

        for (RelationshipTuple rel: relationships) {
        	return rel.object;
        }
		
		return null;
	}	
	
	public static Entry makeEntry(SwordSessionStructure swordSession, DOManager doManager) throws SWORDException {

		//m_generator = new Generator(info.repositoryBaseURL, info.repositoryVersion);
		
		try {

			if (!doManager.objectExists(swordSession.depositId)) {
				throw new SWORDException(SWORDException.FEDORA_NO_OBJECT);
			}
	
			DOReader reader = doManager.getReader(false, swordSession.fedoraContext, swordSession.depositId);
			
			String packaging = getRelationship(reader.getRelationships(SwordConstants.SWORD.PACKAGING, null));
			String contentType = getRelationship(reader.getRelationships(SwordConstants.SWORD.CONTENT_TYPE, null));

			return makeEntry(swordSession, Utils.getDCFields(reader), contentType, packaging);
			
		} catch (Exception e) {
			throw new SWORDException(SWORDException.FEDORA_ERROR, e);
		}
		
	}	
	
	public static Entry makeEntry(SwordSessionStructure swordSession, DCFields dcf, String contentType, String packaging) {
		
        if(swordSession.httpHeader.onBehalfOf == null){
        	swordSession.httpHeader.onBehalfOf = swordSession.userName;
        }
		
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
	
} // ================================================= //
