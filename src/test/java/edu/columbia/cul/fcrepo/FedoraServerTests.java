package edu.columbia.cul.fcrepo;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

import java.util.HashMap;

import javax.ws.rs.core.UriInfo;

import org.fcrepo.server.Context;
import org.fcrepo.server.ReadOnlyContext;
import org.fcrepo.server.resourceIndex.ResourceIndex;
import org.fcrepo.server.security.Authorization;
import org.fcrepo.server.storage.DOManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.columbia.cul.sword.DepositHandler;
import edu.columbia.cul.sword.SWORDException;
import edu.columbia.cul.sword.impl.DepositRequest;
import edu.columbia.cul.sword.impl.ServiceDocumentRequest;
import edu.columbia.cul.sword.impl.fcrepo.FedoraService;
import edu.columbia.cul.sword.xml.entry.Entry;
import edu.columbia.cul.sword.xml.service.ServiceDocument;

public class FedoraServerTests {
	
	private FedoraService test;
	
    @Before
    public void setUp() throws SWORDException {
    	Entry entry = mock(Entry.class);
    	DepositHandler handler = mock(DepositHandler.class);
    	when(handler.ingestDeposit(any(DepositRequest.class), any(Context.class))).thenReturn(entry);
    	HashMap<String, DepositHandler> handlers = new HashMap<String, DepositHandler>(1);
    	handlers.put("", handler);
    	Authorization authz = mock(Authorization.class);
    	DOManager mgmt = mock(DOManager.class);
    	ResourceIndex ri = mock(ResourceIndex.class);
    	test = new FedoraService(authz, mgmt, ri);
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
