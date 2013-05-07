package edu.columbia.cul.utils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.SecureRandom;

public abstract class TestUtils {
    public static InputStream getRandomData(int length) {
    	byte [] bytes = SecureRandom.getSeed(length);
    	return new ByteArrayInputStream(bytes);
    }
}
