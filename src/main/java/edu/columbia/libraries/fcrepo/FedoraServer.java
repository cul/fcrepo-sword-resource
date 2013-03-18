package edu.columbia.libraries.fcrepo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import nu.xom.Document;
import nu.xom.Serializer;

import org.fcrepo.server.Context;
import org.fcrepo.server.ReadOnlyContext;
import org.fcrepo.server.management.Management;
import org.purl.sword.atom.Link;
import org.purl.sword.base.AtomDocumentResponse;
import org.purl.sword.base.Deposit;
import org.purl.sword.base.DepositResponse;
import org.purl.sword.base.SWORDAuthenticationException;
import org.purl.sword.base.SWORDEntry;
import org.purl.sword.base.SWORDErrorException;
import org.purl.sword.base.SWORDException;
import org.purl.sword.base.ServiceDocument;

import edu.columbia.libraries.sword.DepositHandler;
import edu.columbia.libraries.sword.SWORDServer;
import edu.columbia.libraries.sword.impl.AtomEntryRequest;
import edu.columbia.libraries.sword.impl.DefaultDepositHandler;
import edu.columbia.libraries.sword.impl.DepositRequest;
import edu.columbia.libraries.sword.impl.ServiceDocumentRequest;


public class FedoraServer implements SWORDServer {

    private String defaultServiceDocument;

    private Map<String, String> serviceDocuments;

    private Map<String, DepositHandler> m_handlers;
    
    private Management m_management;

    public FedoraServer(Management manager, Map<String, DepositHandler> handlers) {
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

    public ServiceDocument doServiceDocument(ServiceDocumentRequest sdr, Context authzContext)
            throws SWORDAuthenticationException, SWORDErrorException,
            SWORDException {
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

    public DepositResponse doDeposit(Deposit deposit, Context context)
            throws SWORDAuthenticationException, SWORDErrorException,
            SWORDException {
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
        if (handler == null) handler = new DefaultDepositHandler(null);
        SWORDEntry entry = null;
        entry = handler.ingestDeposit(new FedoraDeposit(deposit, collection, context), serviceDoc, context);
        DepositResponse response = new DepositResponse(Deposit.CREATED);
        response.setEntry(entry);
        Iterator<Link> links = entry.getLinks();
        while(links.hasNext()) {
            Link _link = links.next();
            if ("edit".equals(_link.getRel())){
                response.setLocation(_link.getHref());
                break;
            }
        }
        // cache response
        File collectionDir = new File(getEntryStoreLocation(serviceDoc), collection.replaceAll(":", "_"));
        if (!collectionDir.exists()) collectionDir.mkdirs();
        try {
        FileOutputStream cacheOut = new FileOutputStream(new File(collectionDir, entry.getId().replaceAll(":", "_") + ".xml"));
        Serializer ser = new Serializer(cacheOut, "UTF-8");
        ser.setIndent(3);
        Document doc = new Document(entry.marshall());
        ser.write(doc);
        } catch (IOException ioe) {
            throw new SWORDException(ioe.getMessage(), ioe);
        }
        return response;
    }

    private String getEntryStoreLocation(ServiceDocument serviceDoc) {
        return null;
    }

    public AtomDocumentResponse doAtomDocument(AtomEntryRequest adr, Context authzContext)
            throws SWORDAuthenticationException, SWORDErrorException,
            SWORDException {
        return null;
    }

    private ServiceDocument getServiceDocument(String onBehalfOf) {
        return getServiceDocument(onBehalfOf, null);
    }

    private ServiceDocument getServiceDocument(String onBehalfOf, String location) {
        return null;
    }

}
