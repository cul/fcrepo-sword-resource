package edu.columbia.cul.sword;

import java.util.Date;

import org.fcrepo.server.Context;

import edu.columbia.cul.sword.impl.DepositRequest;
import edu.columbia.cul.sword.xml.entry.Entry;
import edu.columbia.cul.sword.xml.entry.Feed;

public interface EntryService {
    public Entry getEntry(DepositRequest deposit, Context context)  throws SWORDException;
    
    public Entry createEntry(DepositRequest deposit, Context context) throws SWORDException;
    
    public Feed getEntryFeed(String collectionId, Date startDate, Context context) throws SWORDException;
}
