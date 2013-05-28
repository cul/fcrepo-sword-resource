package edu.columbia.cul.utils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.SecureRandom;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import edu.columbia.cul.sword.xml.SwordError;
import edu.columbia.cul.sword.xml.entry.Entry;
import edu.columbia.cul.sword.xml.service.ServiceDocument;

public abstract class TestUtils {
    public static InputStream getRandomData(int length) {
    	byte [] bytes = SecureRandom.getSeed(length);
    	return new ByteArrayInputStream(bytes);
    }
    
    public static JAXBContext getJAXBContext() throws JAXBException {
    	return JAXBContext.newInstance( ServiceDocument.class, Entry.class, SwordError.class );
    }
}
