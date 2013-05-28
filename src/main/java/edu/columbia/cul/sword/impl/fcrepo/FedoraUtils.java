package edu.columbia.cul.sword.impl.fcrepo;

import org.fcrepo.server.Context;
import org.fcrepo.server.errors.ServerException;
import org.fcrepo.server.storage.DOManager;
import org.fcrepo.server.storage.DOReader;
import org.fcrepo.server.storage.types.RelationshipTuple;
import org.fcrepo.server.utilities.DCFields;

import edu.columbia.cul.sword.SWORDException;
import edu.columbia.cul.sword.SwordConstants;

public abstract class FedoraUtils {

	public static DCFields getDCFields(Context context, DOManager manager, String pid) 
	    throws SWORDException {
		try {
		if (!manager.objectExists(pid)) {
			throw new SWORDException(SWORDException.FEDORA_NO_OBJECT);
		}
		DOReader reader = manager.getReader(false, context, pid);
		DCFields dcf = new DCFields(reader.getDatastream("DC", null).getContentStream());
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
}
