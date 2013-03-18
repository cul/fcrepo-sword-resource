package edu.columbia.libraries.sword;

import java.util.Map;

import org.fcrepo.server.Context;
import org.purl.sword.base.AtomDocumentResponse;
import org.purl.sword.base.Deposit;
import org.purl.sword.base.DepositResponse;
import org.purl.sword.base.SWORDAuthenticationException;
import org.purl.sword.base.SWORDErrorException;
import org.purl.sword.base.SWORDException;
import org.purl.sword.base.ServiceDocument;

import edu.columbia.libraries.sword.impl.AtomEntryRequest;
import edu.columbia.libraries.sword.impl.DepositRequest;
import edu.columbia.libraries.sword.impl.ServiceDocumentRequest;


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
        throws SWORDAuthenticationException, SWORDErrorException, SWORDException;

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
     */
    public DepositResponse doDeposit(Deposit deposit, Context authzContext)
        throws SWORDAuthenticationException, SWORDErrorException, SWORDException;

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
        throws SWORDAuthenticationException, SWORDErrorException, SWORDException;

    /**
     * Set the deposit handlers
     * @param handlers
     */
    public void setDepositHandlers(Map<String, DepositHandler> handlers);
}

