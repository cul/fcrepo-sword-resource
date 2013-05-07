package edu.columbia.libraries.sword.utils;

import java.util.Arrays;
import java.util.List;

public abstract class StringUtils {
    public static List<String> asList(String value) {
    	return Arrays.asList(new String[] {value});
    }
}
