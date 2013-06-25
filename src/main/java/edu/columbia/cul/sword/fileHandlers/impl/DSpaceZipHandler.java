package edu.columbia.cul.sword.fileHandlers.impl;

import java.util.Set;

import org.fcrepo.server.Context;
import org.fcrepo.server.errors.ServerException;
import org.fcrepo.server.storage.DOManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.columbia.cul.sword.SWORDException;
import edu.columbia.cul.sword.fileHandlers.DepositHandler;
import edu.columbia.cul.sword.impl.DepositRequest;
import edu.columbia.cul.sword.xml.entry.Entry;

public class DSpaceZipHandler extends DefaultDepositHandler implements DepositHandler {
	
	private static final Logger log = LoggerFactory.getLogger(DSpaceZipHandler.class.getName());

	public DSpaceZipHandler() {
		m_contentType = "application/zip";
		m_packaging = "http://purl.org/net/sword-types/mets/dspace";
    }	
	
//	public DSpaceZipHandler(DOManager mgmt, Set<String> collectionIds) throws ServerException {
//		super(mgmt, collectionIds);
//		m_contentType = "application/zip";
//		m_packaging = "http://purl.org/net/sword-types/mets/dspace";
//	}
	
	@Override
	public boolean handles(String contentType, String packaging) {
		return m_contentType.equals(contentType) && m_packaging.equals(packaging);
	}

	@Override
	public Entry ingestDeposit(DepositRequest deposit, Context context, DOManager doManager)
			throws SWORDException {
		
		log.info("start ingestDeposit");
		
		return super.ingestDeposit(deposit, context, doManager);
	}
	
	

} // ========================================================= //
