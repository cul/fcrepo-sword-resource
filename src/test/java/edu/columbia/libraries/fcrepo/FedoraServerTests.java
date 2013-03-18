package edu.columbia.libraries.fcrepo;

import static org.mockito.Mockito.*;

import java.util.HashMap;

import org.fcrepo.server.Context;
import org.fcrepo.server.management.Management;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.purl.sword.base.SWORDEntry;
import org.purl.sword.base.SWORDException;
import org.purl.sword.base.ServiceDocument;

import edu.columbia.libraries.sword.DepositHandler;

public class FedoraServerTests {
	
	private FedoraServer test;
	
    @Before
    public void setUp() throws SWORDException {
    	SWORDEntry entry = mock(SWORDEntry.class);
    	DepositHandler handler = mock(DepositHandler.class);
    	when(handler.ingestDeposit(any(FedoraDeposit.class), any(ServiceDocument.class), any(Context.class))).thenReturn(entry);
    	HashMap<String, DepositHandler> handlers = new HashMap<String, DepositHandler>(1);
    	handlers.put("", handler);
    	Management mgmt = mock(Management.class);
    	test = new FedoraServer(mgmt, handlers);
    }
    
    @After
    public void tearDown() {
    	
    }
    
    @Test
    public void testStuff() {
    	
    }
}
