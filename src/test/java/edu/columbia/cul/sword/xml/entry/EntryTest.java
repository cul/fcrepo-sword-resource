package edu.columbia.libraries.sword.xml.entry;

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

public class EntryTest {

	@Before
	public void setUp(){
		
	}
	
	@After
	public void tearDown(){
		
	}

	@Test
	public void testUnmarshall() throws JAXBException {
		JAXBContext jc = JAXBContext.newInstance(
				"edu.columbia.libraries.sword.xml.entry");
		Unmarshaller um = jc.createUnmarshaller();
		Entry actual = (Entry) um.unmarshal(
				EntryTest.class.getResourceAsStream(
						"/edu/columbia/libraries/sword/xml/entry.xml"));
		assertEquals("info:something:1", actual.id);
		String test = actual.source.generator.getUri().toString();
		assertEquals("http://www.myrepository.ac.uk/engine", test);
	}
}
