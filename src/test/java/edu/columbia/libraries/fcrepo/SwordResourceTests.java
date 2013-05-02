package edu.columbia.libraries.fcrepo;

import static org.mockito.Mockito.*;

import javax.xml.bind.JAXBException;

import org.fcrepo.server.Server;
import org.fcrepo.server.errors.ServerException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.BeansException;

import edu.columbia.libraries.sword.SWORDResource;

public class SwordResourceTests {
	
	SWORDResource test;
	
	@Before
	public void setUp() throws JAXBException, BeansException, ServerException{
		Server server = mock(Server.class);
		test = new SWORDResource(server);
	}
	
	@After
	public void tearDown(){
		test = null;
	}

	@Test
	public void testStuff() {
		
	}
}
