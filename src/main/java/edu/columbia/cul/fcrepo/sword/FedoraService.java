package edu.columbia.cul.fcrepo.sword;

import java.net.URI;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.fcrepo.common.Constants;
import org.fcrepo.server.Context;
import org.fcrepo.server.Server;
import org.fcrepo.server.resourceIndex.ResourceIndex;
import org.fcrepo.server.security.Authorization;
import org.fcrepo.server.storage.DOManager;
import org.fcrepo.server.utilities.DCFields;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.columbia.cul.fcrepo.sword.fileHandlers.DepositHandler;
import edu.columbia.cul.fcrepo.sword.fileHandlers.FileHandlerManager;
import edu.columbia.cul.sword.RepositoryService;
import edu.columbia.cul.sword.exceptions.SWORDException;
import edu.columbia.cul.sword.holder.SwordSessionStructure;
import edu.columbia.cul.sword.impl.DepositRequest;
import edu.columbia.cul.sword.xml.entry.Entry;
import edu.columbia.cul.sword.xml.entry.Feed;
import edu.columbia.cul.sword.xml.service.Collection;
import edu.columbia.cul.sword.xml.service.ServiceDocument;
import edu.columbia.cul.sword.xml.service.Workspace;

//public class FedoraService implements ServiceDocumentService, EntryService, Constants {
public class FedoraService implements RepositoryService, Constants {	
	
	private static final Logger LOGGER = LoggerFactory.getLogger(FedoraService.class.getName());	
	private Authorization m_authz;	
	private ResourceIndex m_resourceIndex; 	
	private Set<String>collectionIds;
	private DOManager doManager;	
	private Set<String> m_rels; 	
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
	
	
	public void setWorkspaceTitle(String title) {
		m_workspace_title = title;
	}
	
	
	public ServiceDocument getDefaultServiceDocument(Context context) throws SWORDException {
		ServiceDocument result = new ServiceDocument();
		result.workspace = new Workspace();
		result.workspace.title = m_workspace_title;
		for (String collectionId: collectionIds) {
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

		if (!collectionIds.contains(collectionId)) {
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
	
	public Entry getEntry(DepositRequest deposit, Context context) throws SWORDException {
		
		LOGGER.debug("getEntry() started");

//		try {
//			if (!doManager.objectExists(deposit.getDepositId())) {
//				throw new SWORDException(SWORDException.FEDORA_NO_OBJECT);
//			}
//
//			return SwordHelper.makeEntry(deposit, doManager, context);
//			
//		} catch (ServerException e) {
//			e.printStackTrace();
//			throw new SWORDException(SWORDException.FEDORA_NO_OBJECT);
//		}
		
		return null;
        
	}
	

	public Entry createEntry(SwordSessionStructure swordSession)
			throws SWORDException {

        try {
        	DepositHandler handler = fileHandlerManager.getHandler(swordSession.httpHeader.contentType, 
        			                                               swordSession.httpHeader.packaging);
        	handler.setRels(m_rels);
            return handler.ingestDeposit(swordSession, doManager);
        } catch (Exception e) {
        	throw new SWORDException(SWORDException.FEDORA_ERROR, e);
        }

	}	

	
	public Feed getEntryFeed(String collectionId, Date startDate,
			Context context) throws SWORDException {
		// TODO Auto-generated method stub
		return null;
	}

	
	public Entry getEntry(SwordSessionStructure swordSession) throws SWORDException {

		return  FedoraUtils.makeEntry(swordSession, doManager);
	}
	
    // -------------- set methods set from sword-jaxrs.xml ------------------ //
    
    public void setMembershipPredicate(String predicate) {
    	setMembershipRel(predicate);
    }

	public void setFileHandlerManager(FileHandlerManager fileHandlerManager) {
		this.fileHandlerManager = fileHandlerManager;
		LOGGER.debug("FileHandlerManagerImpl was set - " + (fileHandlerManager != null));
	}

    public void setCollectionIds(Set<String>collectionIds) {
    	
    	this.collectionIds = collectionIds;
    	
		int count = 0;
		LOGGER.info("collectionIds size: " + collectionIds.size());
		for (String collection : collectionIds) {
			LOGGER.info("collection-{} {}", ++count, collection);
		}

	}	

    
} // ========================================= //
