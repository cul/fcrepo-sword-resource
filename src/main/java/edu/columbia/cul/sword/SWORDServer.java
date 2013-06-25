package edu.columbia.cul.sword;

import java.util.Map;

import org.fcrepo.server.Context;
import org.fcrepo.server.errors.ServerException;
import org.purl.sword.base.AtomDocumentResponse;
import org.purl.sword.base.Deposit;

import edu.columbia.cul.sword.fileHandlers.DepositHandler;
import edu.columbia.cul.sword.impl.AtomEntryRequest;
import edu.columbia.cul.sword.impl.ServiceDocumentRequest;
import edu.columbia.cul.sword.xml.entry.Entry;
import edu.columbia.cul.sword.xml.service.ServiceDocument;

// this one is not used - can be removed?
public interface SWORDServer {

    /**
     * Answer a Service Document request sent on behalf of a user
     *
     * @param sdr The Service Document Request object
     *
     * @exception SWORDAuthenticationException Thrown if the authentication fails. 401
     * @exception SWORDErrorException Thrown if there was an error with the input not matching
     *            the capabilities of the server.
     * @exception SWORDException Thrown if another error occurs.
     *            This will be dealt with by sending a HTTP 500 Server Exception
     *
     * @return The ServiceDocument representing the service document
     */
    public ServiceDocument doServiceDocument(ServiceDocumentRequest sdr, Context authzContext)
        throws SWORDException;

    public ServiceDocument doServiceDocument(String collection, Context authzContext)
            throws SWORDException;
    /**
     * Answer a SWORD deposit
     *
     * @param deposit The Deposit object
     *
     * @exception SWORDAuthenticationException Thrown if the authentication fails
     * @exception SWORDErrorException Thrown if there was an error with the input not matching
     *            the capabilities of the server
     * @exception SWORDException Thrown if an Exception occurs that cannot be handled.
     *            This will be dealt with by sending a HTTP 500 Server Exception
     *
     * @return The response to the deposit
     * @throws ServerException 
     */
    public Entry doDeposit(Deposit deposit, Context authzContext)
        throws SWORDException, ServerException;

    /**
     * Answer a request for an entry document
     *
     * @param adr The Atom Document Request object
     *
     * @exception SWORDAuthenticationException Thrown if the authentication fails
     * @exception SWORDErrorException Thrown if there was an error with the input not matching
     *            the capabilities of the server
     * @exception SWORDException Thrown if an un-handalable Exception occurs.
     *            This will be dealt with by sending a HTTP 500 Server Exception
     *
     * @return The response to the atom document request
     */
    public AtomDocumentResponse doAtomDocument(AtomEntryRequest adr, Context authzContext)
        throws SWORDException;

    /**
     * Set the deposit handlers
     * @param handlers
     */
    public void setDepositHandlers(Map<String, DepositHandler> handlers);
}

