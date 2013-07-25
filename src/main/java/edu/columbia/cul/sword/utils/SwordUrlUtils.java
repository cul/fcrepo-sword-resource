package edu.columbia.cul.sword.utils;

import edu.columbia.cul.sword.holder.SwordSessionStructure;

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
	
	public static String makeDescriptionUrl(SwordSessionStructure swordSession) {

		String path = swordSession.baseUri.getAbsolutePath().toString();
		String collectionId = swordSession.collectionId;
		String pid = swordSession.depositId;
    	return  String.format(descriptionUrlPattern, getBasePath(path, collectionId), pid);
    }
    
    public static String makeContentUrl(SwordSessionStructure swordSession) {
    	
		String path = swordSession.baseUri.getAbsolutePath().toString();
		String collectionId = swordSession.collectionId;
		String pid = swordSession.depositId;
    	return String.format(contentUrlPattern, getBasePath(path, collectionId), pid);
    }
	
} // ======================================================================================== //