package edu.columbia.libraries.sword.impl.fcrepo;

import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.UriInfo;

import org.fcrepo.common.Constants;
import org.fcrepo.server.Context;
import org.fcrepo.server.errors.ServerException;
import org.fcrepo.server.resourceIndex.ResourceIndex;
import org.fcrepo.server.rest.DatastreamResource;
import org.fcrepo.server.rest.FedoraObjectsResource;
import org.fcrepo.server.storage.DOManager;
import org.fcrepo.server.storage.DOReader;
import org.fcrepo.server.storage.types.RelationshipTuple;
import org.fcrepo.server.utilities.DCFields;

import edu.columbia.libraries.fcrepo.Utils;
import edu.columbia.libraries.sword.DepositHandler;
import edu.columbia.libraries.sword.EntryService;
import edu.columbia.libraries.sword.SWORDErrorInfo;
import edu.columbia.libraries.sword.SWORDException;
import edu.columbia.libraries.sword.SWORDResource;
import edu.columbia.libraries.sword.ServiceDocumentService;
import edu.columbia.libraries.sword.SwordConstants;
import edu.columbia.libraries.sword.impl.DefaultDepositHandler;
import edu.columbia.libraries.sword.impl.DepositRequest;
import edu.columbia.libraries.sword.xml.entry.Entry;
import edu.columbia.libraries.sword.xml.entry.Feed;
import edu.columbia.libraries.sword.xml.service.Collection;
import edu.columbia.libraries.sword.xml.service.ServiceDocument;
import edu.columbia.libraries.sword.xml.service.Workspace;

public class FedoraService implements ServiceDocumentService, EntryService, Constants {
	
	private ResourceIndex m_resourceIndex; 
	
	private DOManager m_manager;
	
	private Set<String> m_rels; 
	
	private Set<String> m_collectionIds;
	
	private UriInfo m_uriInfo;

    private Map<String, DepositHandler> m_handlers;
    
    private String m_workspace_title = "FCRepo SWORD Workspace";

	public FedoraService(DOManager manager, ResourceIndex resourceIndex, UriInfo uriInfo) {
		m_manager = manager;
		m_resourceIndex = resourceIndex;
		m_uriInfo = uriInfo;
		m_rels = new HashSet<String>(1);
		m_rels.add(RELS_EXT.IS_MEMBER_OF.uri);
	}
	
    public void setDepositHandlers(Map<String, DepositHandler> handlers) {
    	m_handlers = handlers;
    }

    /**
	 * Add a local relationship that indicates membership by the form $s <rel> $o,
	 * where $s is the MEMBER and $o is the AGGREGATOR
	 * @param rel
	 */
	public void setMembershipRel(String rel) {
		m_rels.add(rel);
	}
	
	public void setCollections(Set<String> collectionIds) {
		m_collectionIds = collectionIds;
	}
	
	public void setWorkspaceTitle(String title) {
		m_workspace_title = title;
	}
	
	// post-construct initialization
	public void init() {
		if (m_handlers == null) {
			m_handlers = new HashMap<String, DepositHandler>(0);
		}
		if (m_collectionIds == null) {
			m_collectionIds = new HashSet<String>(0);
		}
	}
	
	public ServiceDocument getDefaultServiceDocument(Context context) throws SWORDException {
		ServiceDocument result = new ServiceDocument();
		result.workspace = new Workspace();
		result.workspace.title = m_workspace_title;
		for (String collectionId: m_collectionIds) {
			result.workspace.addCollection(getCollection(collectionId, context));
		}
		return result;
	}
	
	public ServiceDocument getServiceDocument(String collectionId, Context context)
	  throws SWORDException {
		ServiceDocument result = new ServiceDocument();
		result.workspace = new Workspace();
		result.workspace.title = m_workspace_title;
		result.workspace.addCollection(getCollection(collectionId, context));
		return result;
	}
	
	public Collection getCollection(String collectionId,
			Context context) throws SWORDException {
		try {
			if (!m_collectionIds.contains(collectionId)) {
				throw new SWORDException(SWORDException.ERROR_REQUEST);
			}
			if (!m_manager.objectExists(collectionId)) {
				throw new SWORDException(SWORDException.FEDORA_NO_OBJECT);
			}
			DOReader reader = m_manager.getReader(false, context, collectionId);
			DCFields dcf = new DCFields(reader.getDatastream("DC", null).getContentStream());

			Collection collection = new Collection();
			for (DepositHandler h: m_handlers.values()) {
				collection.addAcceptableMimeType(h.getContentType());
				collection.addAcceptablePackaging(URI.create(h.getPackaging()));
			}
			collection.setDCFields(dcf);
			ServiceDocument result = new ServiceDocument();
			result.workspace = new Workspace();
			result.workspace.addCollection(collection);


		} catch (ServerException e) {
			throw new SWORDException(SWORDException.FEDORA_ERROR, e);
		}
		throw new SWORDException(SWORDException.FEDORA_ERROR);
	}
	
	private DepositHandler getHandler(String contentType, String packaging) throws ServerException {
        for (DepositHandler handler: m_handlers.values()) {
        	if (handler.handles(contentType, packaging)) {
        		return handler;
        	}
        }
        return new DefaultDepositHandler(m_manager, m_collectionIds, m_rels);
	}
	
	public Entry getEntry(DepositRequest deposit, Context context) throws SWORDException {
        String pid = deposit.getDepositId();
        
        try {
			DOReader reader = m_manager.getReader(false, context, pid);
	        Set<RelationshipTuple> rels = reader.getRelationships();
	        String contentType = null;
	        String packaging = null;
	        for (RelationshipTuple rel: rels) {
	        	if (SwordConstants.SWORD_CONTENT_TYPE_PREDICATE.equals(rel.predicate)) {
	        		contentType = rel.object;
	        	}
	        	if (SwordConstants.SWORD_PACKAGING_PREDICATE.equals(rel.predicate)) {
	        		packaging = rel.object;
	        	}
	        }
	        DepositHandler handler = null;
	        try {
	        	handler = getHandler(contentType, packaging);
		        return handler.getEntry(deposit, context);
	        } catch (ServerException e) {
	        	throw new SWORDException(SWORDException.FEDORA_ERROR, e);
	        }
		} catch (ServerException e) {
			e.printStackTrace();
			throw new SWORDException(SWORDException.FEDORA_NO_OBJECT);
		}
	}

	public Entry createEntry(DepositRequest deposit, Context context)
			throws SWORDException {
		String collection = deposit.getCollection();
        //TODO this requires ingest/create authZ for a new resource
        String location = deposit.getLocation();
        if (location.endsWith("/")){ // trim ending slash
            location = location.substring(0, location.length() - 1);
        }
        String onBehalfOf = deposit.getOnBehalfOf();
        if (onBehalfOf == null) onBehalfOf = deposit.getUserName();
        ServiceDocument serviceDoc = getServiceDocument(collection, context);
        // do some authZ with the serviceDoc (??)
        // check whether content types is allowed
        // check whether package type is allowed
        // get the first matching deposit handler
        Entry entry = null;
        DepositHandler handler = null;
        try {
        	handler = getHandler(deposit.getContentType(), deposit.getPackaging());
            entry = handler.ingestDeposit(deposit, context);
        } catch (ServerException e) {
        	throw new SWORDException(SWORDException.FEDORA_ERROR, e);
        }
        return entry;
	}

	public Feed getEntryFeed(String collectionId, Date startDate,
			Context context) throws SWORDException {
		// TODO Auto-generated method stub
		return null;
	}

}
