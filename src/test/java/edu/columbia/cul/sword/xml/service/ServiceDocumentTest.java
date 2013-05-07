package edu.columbia.cul.sword.xml.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.columbia.cul.sword.xml.service.AcceptPackaging;
import edu.columbia.cul.sword.xml.service.Collection;
import edu.columbia.cul.sword.xml.service.Workspace;

public class ServiceDocumentTest {

	@Before
	public void setUp(){
		
	}
	
	@After
	public void tearDown(){
		
	}
	
	@Test
	public void testUnmarshalling() throws JAXBException {
		JAXBContext jc = JAXBContext.newInstance(
				"edu.columbia.cul.sword.xml.service");
		Unmarshaller um = jc.createUnmarshaller();
		ServiceDocument actual = (ServiceDocument) um.unmarshal(
				ServiceDocumentTest.class.getResourceAsStream(
						"/edu/columbia/libraries/sword/xml/serviceDoc.xml"));
		assertEquals("1.3", actual.version);
		Workspace ws = actual.workspace;
		assertNotNull(ws);
		assertEquals("Main Site", ws.title);
		Collection c = ws.getCollection("http://www.myrepository.ac.uk/atom/geography-collection");
		assertEquals("My Repository : Geography Collection", c.title);
		Set<String> accepts = c.getAcceptableMimeTypes();
		assertEquals(1, accepts.size());
		assertEquals("application/zip", accepts.iterator().next());
		List<AcceptPackaging> pkgs = c.getAcceptablePackagings();
		assertEquals(2, pkgs.size());
		assertEquals("http://purl.org/net/sword-types/bagit", pkgs.get(1).packaging.toString());
		assertEquals("0.8", pkgs.get(1).getQ());
	}
	
	@Test
	public void testMarshalling() {
		
	}
}
