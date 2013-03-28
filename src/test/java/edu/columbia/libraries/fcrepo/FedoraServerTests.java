package edu.columbia.libraries.fcrepo;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

import java.util.HashMap;

import org.fcrepo.server.Context;
import org.fcrepo.server.ReadOnlyContext;
import org.fcrepo.server.storage.DOManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.columbia.libraries.sword.DepositHandler;
import edu.columbia.libraries.sword.SWORDException;
import edu.columbia.libraries.sword.impl.ServiceDocumentRequest;
import edu.columbia.libraries.sword.xml.entry.Entry;
import edu.columbia.libraries.sword.xml.service.ServiceDocument;

public class FedoraServerTests {
	
	private FedoraServer test;
	
    @Before
    public void setUp() throws SWORDException {
    	Entry entry = mock(Entry.class);
    	DepositHandler handler = mock(DepositHandler.class);
    	when(handler.ingestDeposit(any(FedoraDeposit.class), any(ServiceDocument.class), any(Context.class))).thenReturn(entry);
    	HashMap<String, DepositHandler> handlers = new HashMap<String, DepositHandler>(1);
    	handlers.put("", handler);
    	DOManager mgmt = mock(DOManager.class);
    	test = new FedoraServer(mgmt, handlers);
    }
    
    @After
    public void tearDown() {
    	
    }
    
    @Test
    public void testDoServiceDocument() throws SWORDException {
    	ServiceDocumentRequest sdr = new ServiceDocumentRequest();
    	Context authzContext = ReadOnlyContext.EMPTY;
        ServiceDocument actual = test.doServiceDocument(sdr, authzContext);
        assertNotNull(actual);
    }
}
