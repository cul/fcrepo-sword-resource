package edu.columbia.libraries.fcrepo;

import static org.mockito.Mockito.*;

import org.fcrepo.server.Server;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SwordResourceTests {
	
	SwordResource test;
	
	@Before
	public void setUp(){
		Server server = mock(Server.class);
		test = new SwordResource(server);
	}
	
	@After
	public void tearDown(){
		test = null;
	}

	@Test
	public void testStuff() {
		
	}
}
