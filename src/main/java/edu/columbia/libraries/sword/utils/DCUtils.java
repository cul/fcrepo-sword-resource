package edu.columbia.libraries.sword.utils;

import java.util.ArrayList;
import java.util.List;

import org.fcrepo.server.utilities.DCField;

public abstract class DCUtils {
    public static List<String> toStrings(List<DCField> vals) {
    	ArrayList<String> result = new ArrayList<String>(vals.size());
    	for (DCField val: vals) {
    		result.add(val.getValue());
    	}
    	return result;
    }
}
