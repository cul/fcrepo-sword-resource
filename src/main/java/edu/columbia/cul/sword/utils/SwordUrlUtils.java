package edu.columbia.cul.sword.utils;

public class SwordUrlUtils {
	
	private static String basePath;
	private static String descriptionUrlPattern = "%s/get/%s";
	private static String contentUrlPattern = "%s/get/%s/content";

	public static String getBasePath(String requestPath, String collection){
    	
		if(basePath == null){
        	requestPath = requestPath.substring(0, requestPath.indexOf("/" + collection));
        	requestPath = requestPath .substring(0, requestPath.lastIndexOf("/"));
        	basePath = requestPath;
    	}
    	return basePath;
	}
	
	public static String makeDescriptionUrl(String requestPath, String collection, String pid) {

    	return  String.format(descriptionUrlPattern, getBasePath(requestPath, collection), pid);
    }
    
    public static String makeContentUrl(String requestPath, String collection, String pid) {
    	
    	return String.format(contentUrlPattern, getBasePath(requestPath, collection), pid);
    }
	
} // ======================================================================================== //