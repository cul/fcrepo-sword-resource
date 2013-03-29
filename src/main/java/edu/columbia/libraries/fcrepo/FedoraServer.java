package edu.columbia.libraries.fcrepo;

import java.io.File;
import java.net.URI;
import java.util.HashSet;
import java.util.Map;

import org.fcrepo.server.Context;
import org.fcrepo.server.errors.ServerException;
import org.fcrepo.server.management.Management;
import org.fcrepo.server.storage.DOManager;
import org.fcrepo.server.storage.DOReader;
import org.fcrepo.server.utilities.DCFields;
import org.purl.sword.base.AtomDocumentResponse;
import org.purl.sword.base.Deposit;

import edu.columbia.libraries.sword.DepositHandler;
import edu.columbia.libraries.sword.SWORDException;
import edu.columbia.libraries.sword.SWORDServer;
import edu.columbia.libraries.sword.impl.AtomEntryRequest;
import edu.columbia.libraries.sword.impl.DefaultDepositHandler;
import edu.columbia.libraries.sword.impl.ServiceDocumentRequest;
import edu.columbia.libraries.sword.xml.entry.Entry;
import edu.columbia.libraries.sword.xml.service.Collection;
import edu.columbia.libraries.sword.xml.service.ServiceDocument;
import edu.columbia.libraries.sword.xml.service.Workspace;


public class FedoraServer implements SWORDServer {

    private String defaultServiceDocument;

    private Map<String, String> serviceDocuments;

    private Map<String, DepositHandler> m_handlers;
    
    private DOManager m_management;

    public FedoraServer(DOManager manager, Map<String, DepositHandler> handlers) {
        m_handlers = handlers;
        m_management = manager;
    }
    
    public void setDepositHandlers(Map<String, DepositHandler> handlers) {
    	m_handlers = handlers;
    }

    public void setDefaultServiceDocument(String serviceDocument) {
        defaultServiceDocument = serviceDocument;
    }

    public void setServiceDocuments(Map<String, String> docs) {
        serviceDocuments = docs;
    }
    
    public ServiceDocument doServiceDocument(String collectionPid, Context authzContext) throws SWORDException {
    	try {
    		if (!m_management.objectExists(collectionPid)) {
    			throw new SWORDException(SWORDException.FEDORA_NO_OBJECT);
    		}
    		DOReader reader = m_management.getReader(false, authzContext, collectionPid);
    		DCFields dcf = new DCFields(reader.getDatastream("DC", null).getContentStream());

            Collection collection = new Collection();
    		for (DepositHandler h: m_handlers.values()) {
    			collection.addAcceptableMimeType(h.getContentType());
    			collection.addAcceptablePackaging(URI.create(h.getPackaging()));
    		}
    		collection.title = (dcf.titles().size() > 0) ? dcf.titles().get(0).getValue() : "FCRepo Collection " + collectionPid;
            ServiceDocument result = new ServiceDocument();
            result.workspace = new Workspace();
            result.workspace.addCollection(collection);

    		
    	} catch (ServerException e) {
        	throw new SWORDException(SWORDException.FEDORA_ERROR, e);
    	}
    	throw new SWORDException(SWORDException.FEDORA_ERROR);
    }

    public ServiceDocument doServiceDocument(ServiceDocumentRequest sdr, Context authzContext)
            throws SWORDException {
        //TODO this requires read authZ on the service doc
        String onBehalfOf = sdr.getOnBehalfOf();
        if (onBehalfOf == null) {
        	onBehalfOf = sdr.getUserName();
        } else {
        	String user = sdr.getUserName();
        	// do some authZ for whether user can mediate
        }
        String [] uriList = sdr.getLocation().split("/");
        String location = uriList[uriList.length - 1];
        if ("servicedocument".equals(location)){
            return getServiceDocument(onBehalfOf);
        } else {
            return getServiceDocument(onBehalfOf, location);
        }
    }

    public Entry doDeposit(Deposit deposit, Context context)
            throws SWORDException {
        //TODO this requires ingest/create authZ for a new resource
        String location = deposit.getLocation();
        if (location.endsWith("/")){ // trim ending slash
            location = location.substring(0, location.length() - 1);
        }
        String[] words = location.split("/");
        final String collection = words[words.length - 1];
        String onBehalfOf = deposit.getOnBehalfOf();
        if (onBehalfOf == null) onBehalfOf = deposit.getUsername();
        ServiceDocument serviceDoc = getServiceDocument(onBehalfOf);
        // do some authZ with the serviceDoc (??)
        // check whether content types is allowed
        // check whether package type is allowed
        // get the first matching deposit handler
        DepositHandler handler = null;
        for (DepositHandler h: m_handlers.values()) {
            if (h.handles(deposit.getContentType(), deposit.getPackaging())) {
                handler = h;
            }
        }
        if (handler == null) handler = new DefaultDepositHandler(m_management);
        Entry entry = null;
        FedoraDeposit fDeposit = new FedoraDeposit(deposit, collection, context);
        entry = handler.ingestDeposit(fDeposit, serviceDoc, context);

        // cache response
        File collectionDir = new File(getEntryStoreLocation(serviceDoc), collection.replaceAll(":", "_"));
        if (!collectionDir.exists()) collectionDir.mkdirs();
        return entry;
    }

    private String getEntryStoreLocation(ServiceDocument serviceDoc) {
        return null;
    }

    public AtomDocumentResponse doAtomDocument(AtomEntryRequest adr, Context authzContext)
            throws SWORDException {
        return null;
    }

    private ServiceDocument getServiceDocument(String onBehalfOf) {
        return getServiceDocument(onBehalfOf, null);
    }

    private ServiceDocument getServiceDocument(String onBehalfOf, String location) {
        return null;
    }

}
