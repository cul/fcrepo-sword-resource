package edu.columbia.cul.sword;

import org.fcrepo.server.Context;

import edu.columbia.cul.sword.impl.DepositRequest;
import edu.columbia.cul.sword.xml.entry.Entry;


public interface DepositHandler {
		
    public static String DEPOSIT_DSID = "content";

    /**
     * Report on whether this handler can handle the given contentType and packaging
     * @param contentType
     * @param packaging
     * @return
     */
	public boolean handles(String contentType, String packaging);
    
    /**
     * Get the content type this handler is designed to acommodate
     * @return MIME type associated with the content as a String
     */
	public String getContentType();
    
	/**
	 * Get the specific packaging this handler expects
	 * @return packaging URI as a String
	 */
    public String getPackaging();

    /**
     * Create a new object and return an SWORD ATOM Entry describing it
     * @param deposit
     * @param context
     * @return
     * @throws SWORDException
     */
    public Entry ingestDeposit(DepositRequest deposit, Context context) throws SWORDException;
    
    /**
     * Get a SWORD ATOM Entry describing an existing item
     * @param deposit
     * @param context
     * @return
     * @throws SWORDException
     */
    public Entry getEntry(DepositRequest deposit, Context context) throws SWORDException;

}
