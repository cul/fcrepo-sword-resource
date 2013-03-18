package edu.columbia.libraries.sword;

import org.fcrepo.server.Context;
import org.purl.sword.base.SWORDEntry;
import org.purl.sword.base.SWORDException;
import org.purl.sword.base.ServiceDocument;

import edu.columbia.libraries.fcrepo.FedoraDeposit;


public interface DepositHandler {
    public boolean handles(String contentType, String packaging);

    public SWORDEntry ingestDeposit(FedoraDeposit deposit, ServiceDocument serviceDocument, Context context) throws SWORDException;

}
