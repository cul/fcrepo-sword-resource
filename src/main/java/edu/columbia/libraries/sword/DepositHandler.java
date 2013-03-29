package edu.columbia.libraries.sword;

import org.fcrepo.server.Context;
import org.purl.sword.base.Deposit;

import edu.columbia.libraries.fcrepo.FedoraDeposit;
import edu.columbia.libraries.sword.impl.DepositRequest;
import edu.columbia.libraries.sword.xml.entry.Entry;
import edu.columbia.libraries.sword.xml.service.ServiceDocument;


public interface DepositHandler {
    public static String DEPOSIT_DSID = "content";

	public boolean handles(String contentType, String packaging);
    
    public String getContentType();
    
    public String getPackaging();

    public Entry ingestDeposit(DepositRequest deposit, Context context) throws SWORDException;

}
