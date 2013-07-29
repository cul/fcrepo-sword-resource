package edu.columbia.cul.sword;

import java.util.Date;

import org.fcrepo.server.Context;
import org.fcrepo.server.Server;

import edu.columbia.cul.sword.exceptions.SWORDException;
import edu.columbia.cul.sword.holder.SwordSessionStructure;
import edu.columbia.cul.sword.xml.entry.Entry;
import edu.columbia.cul.sword.xml.entry.Feed;
import edu.columbia.cul.sword.xml.service.ServiceDocument;

public interface RepositoryService {

	public void init(Server server);
	
	public ServiceDocument getServiceDocument(String id, SwordSessionStructure swordSession) throws SWORDException;   
    public Feed getEntryFeed(String collectionId, Date startDate, Context context) throws SWORDException;
    public ServiceDocument getDefaultServiceDocument(SwordSessionStructure swordSession) throws SWORDException;
    
    public Entry createEntry(SwordSessionStructure swordSession)  throws SWORDException;  
    public Entry getEntry(SwordSessionStructure swordSession) throws SWORDException; 
    
    public boolean isContentSupported(SwordSessionStructure swordSession) throws SWORDException;
	
} // ================================================= //
