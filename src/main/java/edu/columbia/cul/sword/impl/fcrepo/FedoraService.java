package edu.columbia.cul.sword.impl.fcrepo;

import java.net.URI;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.fcrepo.common.Constants;
import org.fcrepo.server.Context;
import org.fcrepo.server.Server;
import org.fcrepo.server.errors.ServerException;
import org.fcrepo.server.resourceIndex.ResourceIndex;
import org.fcrepo.server.security.Authorization;
import org.fcrepo.server.storage.DOManager;
import org.fcrepo.server.storage.DOReader;
import org.fcrepo.server.storage.types.RelationshipTuple;
import org.fcrepo.server.utilities.DCFields;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.columbia.cul.sword.EntryService;
import edu.columbia.cul.sword.SWORDException;
import edu.columbia.cul.sword.ServiceDocumentService;
import edu.columbia.cul.sword.SwordConstants;
import edu.columbia.cul.sword.fileHandlers.DepositHandler;
import edu.columbia.cul.sword.fileHandlers.FileHandlerManager;
import edu.columbia.cul.sword.fileHandlers.impl.FileHandlerManagerImpl;
import edu.columbia.cul.sword.impl.DepositRequest;
import edu.columbia.cul.sword.xml.entry.Entry;
import edu.columbia.cul.sword.xml.entry.Feed;
import edu.columbia.cul.sword.xml.service.Collection;
import edu.columbia.cul.sword.xml.service.ServiceDocument;
import edu.columbia.cul.sword.xml.service.Workspace;

public class FedoraService implements ServiceDocumentService, EntryService, Constants {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(FedoraService.class.getName());	
	private Authorization m_authz;	
	private ResourceIndex m_resourceIndex; 	
	private DOManager doManager;	
	private Set<String> m_rels; 	
	private Set<String> m_collectionIds;	
    //private Map<String, DepositHandler> m_handlers;   
    private String m_workspace_title = "FCRepo SWORD Workspace";
    private FileHandlerManager fileHandlerManager;
    
    public FedoraService() {
		m_rels = new HashSet<String>(1);
		m_rels.add(RELS_EXT.IS_MEMBER_OF.uri);
    }
    
    public FedoraService(Server server) {
		this();
		init(server);
    }

	public FedoraService(Authorization authz, DOManager manager, ResourceIndex resourceIndex) {
		this();
		m_authz = authz;
		doManager = manager;
		m_resourceIndex = resourceIndex;
	}
	
//    public void setDepositHandlers(Map<String, DepositHandler> handlers) {
//    	m_handlers = handlers;
//    }
    
    public void init(Server server) {
    	m_authz = server.getBean(Authorization.class.getName(), Authorization.class);
    	doManager = server.getBean(DOManager.class.getName(), DOManager.class);
    	m_resourceIndex = server.getBean(ResourceIndex.class.getName(), ResourceIndex.class);
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
	
//	// post-construct initialization
//	public void init() {
//		if (m_handlers == null) {
//			m_handlers = new HashMap<String, DepositHandler>(0);
//		}
//		if (m_collectionIds == null) {
//			m_collectionIds = new HashSet<String>(0);
//		}
//	}
	
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
	
	public Collection getCollection(String collectionId, Context context) throws SWORDException {

		if (!m_collectionIds.contains(collectionId)) {
			throw new SWORDException(SWORDException.ERROR_REQUEST);
		}

		Collection collection = new Collection();
		DCFields dcf = FedoraUtils.getDCFields(context, doManager, collectionId);
		collection.setDCFields(dcf);
		collection.mediation = FedoraUtils.isMediated(context, doManager, collectionId);
		
		for (DepositHandler h: fileHandlerManager.getHandlers()) {
			collection.addAcceptableMimeType(h.getContentType());
			if(h.getPackaging() != null) {
				collection.addAcceptablePackaging(URI.create(h.getPackaging()));
			}
		}

		return collection;
	}
	
//	private DepositHandler getHandler(String contentType, String packaging) throws ServerException {
//        
//		for (DepositHandler handler: m_handlers.values()) {
//        	if (handler.handles(contentType, packaging)) {
//        		return handler;
//        	}
//        }
//		
//        return new DefaultDepositHandler(doManager, m_collectionIds, m_rels);
//	}
	
	public Entry getEntry(DepositRequest deposit, Context context) throws SWORDException {
        String pid = deposit.getDepositId();
        
        try {
			DOReader reader = doManager.getReader(false, context, pid);

	        String contentType = null;
	        String packaging = null;

	        for (RelationshipTuple rel: reader.getRelationships(SwordConstants.SWORD.CONTENT_TYPE, null)) {
        		contentType = rel.object;
	        }

	        for (RelationshipTuple rel: reader.getRelationships(SwordConstants.SWORD.PACKAGING, null)) {
	        	packaging = rel.object;
	        }

	        try {
	        	DepositHandler handler = fileHandlerManager.getHandler(contentType, packaging);
		        return handler.getEntry(deposit, context, doManager);
	        } catch (Exception e) {
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

        try {
        	DepositHandler handler = fileHandlerManager.getHandler(deposit.getContentType(), deposit.getPackaging());
        	handler.setRels(m_rels);
            return handler.ingestDeposit(deposit, context, doManager);
        } catch (Exception e) {
        	throw new SWORDException(SWORDException.FEDORA_ERROR, e);
        }

	}

	public Feed getEntryFeed(String collectionId, Date startDate,
			Context context) throws SWORDException {
		// TODO Auto-generated method stub
		return null;
	}

	public void setFileHandlerManager(FileHandlerManagerImpl fileHandlerManagerImpl) {
		this.fileHandlerManager = fileHandlerManagerImpl;
		LOGGER.debug("FileHandlerManagerImpl was set - " + (fileHandlerManagerImpl != null));
	}

	
} // ============================= //
