package edu.columbia.libraries.sword;

import org.fcrepo.server.Context;

import edu.columbia.libraries.fcrepo.FedoraDeposit;
import edu.columbia.libraries.sword.xml.entry.Entry;
import edu.columbia.libraries.sword.xml.service.ServiceDocument;


public interface DepositHandler {
    public boolean handles(String contentType, String packaging);
    
    public String getContentType();
    
    public String getPackaging();

    public Entry ingestDeposit(FedoraDeposit deposit, ServiceDocument serviceDocument, Context context) throws SWORDException;

}
