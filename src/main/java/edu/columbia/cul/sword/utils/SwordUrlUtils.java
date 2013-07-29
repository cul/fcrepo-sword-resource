package edu.columbia.cul.sword.utils;

import edu.columbia.cul.sword.holder.SwordSessionStructure;

public class SwordUrlUtils {
	
	private static String descriptionUrlPattern = "%s/get/%s";
	private static String contentUrlPattern = "%s/get/%s/content";
	
	public static String removePathFromUrl(String requestPath, String pathPart){

        requestPath = requestPath.substring(0, requestPath.indexOf("/" + pathPart));
    	return requestPath;
	}

	public static String getFedoraBasePath(String requestPath, String collection){

        requestPath = requestPath.substring(0, requestPath.indexOf("/" + collection));
        requestPath = requestPath .substring(0, requestPath.lastIndexOf("/"));

    	return requestPath;
	}
	
	public static String makeDescriptionUrl(SwordSessionStructure swordSession) {

		String path = swordSession.uriInfo.getAbsolutePath().toString();
		String collectionId = swordSession.collectionId;
		String pid = swordSession.depositId;
    	return  String.format(descriptionUrlPattern, getFedoraBasePath(path, collectionId), pid);
    }
    
    public static String makeContentUrl(SwordSessionStructure swordSession) {
    	
		String path = swordSession.uriInfo.getAbsolutePath().toString();
		String collectionId = swordSession.collectionId;
		String pid = swordSession.depositId;
    	return String.format(contentUrlPattern, getFedoraBasePath(path, collectionId), pid);
    }
	
} // ======================================================================================== //