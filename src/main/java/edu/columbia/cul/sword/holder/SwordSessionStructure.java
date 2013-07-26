package edu.columbia.cul.sword.holder;

import java.io.File;

import javax.ws.rs.core.UriInfo;

import org.fcrepo.server.Context;
import org.fcrepo.server.access.RepositoryInfo;
import org.fcrepo.server.storage.DOManager;

import edu.columbia.cul.sword.xml.entry.Generator;

public class SwordSessionStructure {

	/* those values go from from http request */
	@InfoStructure
	public HttpHeaderStructure httpHeader;
	
	/* those values go from web.xml */	
	@InfoStructure
	public WebContextStructure webContext;	
	
	/* this is Fedora repository info */
	@InfoStructure
	public RepositoryInfo repositoryInfo;
	
	/* this is Fedora repository context */
	@InfoStructure
	public Context fedoraContext;
	
	/* those values go from sword-jars.xml*/
	
	public String depositId;
	public String collectionId;

	public Integer contentLength;
	public boolean verbose;
	public boolean noOp;
	
    public String userName;
    public String password;
    public boolean authenticated; 
    public boolean proxied;   
    public UriInfo baseUri;  
    public Generator generator;  

	public String ipAddress;
	//public String location;
    
    public String fileName;
    public File tempFile;
    public String tempDir;
    public int maxUploadSizeInt = -1;
    
//    private DOManager doManager;
//
//	public DOManager getDoManager() {
//		return doManager;
//	}
//
//	public void setDoManager(DOManager doManager) {
//		this.doManager = doManager;
//	}

} // ====================================================== //
