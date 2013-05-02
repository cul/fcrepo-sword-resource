package edu.columbia.libraries.fcrepo;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

import java.util.HashMap;

import javax.ws.rs.core.UriInfo;

import org.fcrepo.server.Context;
import org.fcrepo.server.ReadOnlyContext;
import org.fcrepo.server.resourceIndex.ResourceIndex;
import org.fcrepo.server.storage.DOManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.columbia.libraries.sword.DepositHandler;
import edu.columbia.libraries.sword.SWORDException;
import edu.columbia.libraries.sword.impl.DepositRequest;
import edu.columbia.libraries.sword.impl.ServiceDocumentRequest;
import edu.columbia.libraries.sword.impl.fcrepo.FedoraService;
import edu.columbia.libraries.sword.xml.entry.Entry;
import edu.columbia.libraries.sword.xml.service.ServiceDocument;

public class FedoraServerTests {
	
	private FedoraService test;
	
    @Before
    public void setUp() throws SWORDException {
    	Entry entry = mock(Entry.class);
    	DepositHandler handler = mock(DepositHandler.class);
    	when(handler.ingestDeposit(any(DepositRequest.class), any(Context.class))).thenReturn(entry);
    	HashMap<String, DepositHandler> handlers = new HashMap<String, DepositHandler>(1);
    	handlers.put("", handler);
    	DOManager mgmt = mock(DOManager.class);
    	ResourceIndex ri = mock(ResourceIndex.class);
    	UriInfo uriInfo = mock(UriInfo.class);
    	test = new FedoraService(mgmt, ri, uriInfo);
    	test.setDepositHandlers(handlers);
    }
    
    @After
    public void tearDown() {
    	
    }
    
    @Test
    public void testDoServiceDocument() throws SWORDException {
    	Context authzContext = ReadOnlyContext.EMPTY;
        ServiceDocument actual = test.getDefaultServiceDocument(authzContext);
        assertNotNull(actual);
    }
}
