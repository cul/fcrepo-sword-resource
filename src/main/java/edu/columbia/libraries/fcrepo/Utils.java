package edu.columbia.libraries.fcrepo;

import java.util.Set;

import org.fcrepo.server.errors.ServerException;
import org.fcrepo.server.storage.DOReader;
import org.fcrepo.server.storage.DOWriter;
import org.fcrepo.server.storage.types.Datastream;
import org.fcrepo.server.storage.types.RelationshipTuple;
import org.fcrepo.server.utilities.DCFields;

import edu.columbia.libraries.sword.SwordConstants;

public abstract class Utils {
    public static DCFields getDCFields(DOWriter writer) throws ServerException {
		return getDCFields(writer.GetDatastream("DC", null));
    }

    public static DCFields getDCFields(DOReader reader) throws ServerException {
		return getDCFields(reader.GetDatastream("DC", null));
    }
    
    private static DCFields getDCFields(Datastream dcDatastream) throws ServerException {
    	return new DCFields(dcDatastream.getContentStream());
    }
    
    public static String getSwordContentType(DOReader reader) throws ServerException {
    	return getSwordContentType(reader.getRelationships());
    }
    
    public static String getSwordContentType(DOWriter writer) throws ServerException {
    	return getSwordContentType(writer.getRelationships());
    }
    
    private static String getSwordContentType(Set<RelationshipTuple> rels) {
		return getRel(SwordConstants.SWORD_CONTENT_TYPE_PREDICATE, rels);
    }

    public static String getSwordPackaging(DOReader reader) throws ServerException {
    	return getSwordPackaging(reader.getRelationships());
    }
    
    public static String getSwordPackaging(DOWriter writer) throws ServerException {
    	return getSwordPackaging(writer.getRelationships());
    }

    private static String getSwordPackaging(Set<RelationshipTuple> rels) {
		return getRel(SwordConstants.SWORD_PACKAGING_PREDICATE, rels);
    }

    public static String getSlug(DOReader reader) throws ServerException {
    	return getSlug(reader.getRelationships());
    }
    
    public static String getSlug(DOWriter writer) throws ServerException {
    	return getSlug(writer.getRelationships());
    }

    private static String getSlug(Set<RelationshipTuple> rels) {
		return getRel(SwordConstants.SWORD_SLUG_PREDICATE, rels);
    }

    private static String getRel(String predicate, Set<RelationshipTuple> rels) {
		for (RelationshipTuple rel: rels) {
			if (rel.predicate.equals(predicate)){
				return rel.object;
			}
		}
		return null;
    }
}
