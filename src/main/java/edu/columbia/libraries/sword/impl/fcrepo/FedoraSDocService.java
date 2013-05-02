package edu.columbia.libraries.sword.impl.fcrepo;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.fcrepo.common.Constants;
import org.fcrepo.common.rdf.FedoraRelsExtNamespace;
import org.fcrepo.server.Context;
import org.fcrepo.server.ReadOnlyContext;
import org.fcrepo.server.errors.ServerException;
import org.fcrepo.server.errors.StorageDeviceException;
import org.fcrepo.server.resourceIndex.ResourceIndex;
import org.fcrepo.server.storage.DOManager;
import org.fcrepo.server.storage.DOReader;
import org.fcrepo.server.utilities.DCFields;

import edu.columbia.libraries.sword.DepositHandler;
import edu.columbia.libraries.sword.SWORDException;
import edu.columbia.libraries.sword.ServiceDocumentService;
import edu.columbia.libraries.sword.impl.ServiceDocumentRequest;
import edu.columbia.libraries.sword.utils.DCUtils;
import edu.columbia.libraries.sword.utils.StringUtils;
import edu.columbia.libraries.sword.xml.entry.Entry;
import edu.columbia.libraries.sword.xml.service.Collection;
import edu.columbia.libraries.sword.xml.service.ServiceDocument;
import edu.columbia.libraries.sword.xml.service.Workspace;

public class FedoraSDocService implements ServiceDocumentService, Constants {

	private ResourceIndex m_resourceIndex; 
	
	private DOManager m_manager;
	
	private Set<String> m_rels; 
	
	private Set<String> m_collectionIds;

    private Map<String, DepositHandler> m_handlers;
    
    private String m_workspace_title = "FCRepo SWORD Workspace";

	public FedoraSDocService(DOManager manager, ResourceIndex resourceIndex) {
		m_manager = manager;
		m_resourceIndex = resourceIndex;
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
			collection.title = (dcf.titles().size() > 0) ? DCUtils.toStrings(dcf.titles()) : StringUtils.asList("FCRepo Collection " + collectionId);
			ServiceDocument result = new ServiceDocument();
			result.workspace = new Workspace();
			result.workspace.addCollection(collection);


		} catch (ServerException e) {
			throw new SWORDException(SWORDException.FEDORA_ERROR, e);
		}
		throw new SWORDException(SWORDException.FEDORA_ERROR);
	}
	
	public Entry getEntry(String depositId, Context context) throws SWORDException {
		Entry entry = new Entry();
		try {
			if (!m_manager.objectExists(depositId)) {
				throw new SWORDException(SWORDException.FEDORA_NO_OBJECT);
			}
			return entry;
		} catch (ServerException e) {
			throw new SWORDException(SWORDException.FEDORA_ERROR, e);
		}
	}

}
