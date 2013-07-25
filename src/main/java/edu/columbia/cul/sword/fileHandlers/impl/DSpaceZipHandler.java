package edu.columbia.cul.sword.fileHandlers.impl;

import org.fcrepo.server.Context;
import org.fcrepo.server.storage.DOManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.columbia.cul.sword.exceptions.SWORDException;
import edu.columbia.cul.sword.fileHandlers.DepositHandler;
import edu.columbia.cul.sword.holder.SwordSessionStructure;
import edu.columbia.cul.sword.impl.DepositRequest;
import edu.columbia.cul.sword.xml.entry.Entry;

public class DSpaceZipHandler extends DefaultZipHandler implements DepositHandler {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DSpaceZipHandler.class.getName());

	public DSpaceZipHandler() {
		m_contentType = "application/zip";
		m_packaging = "http://purl.org/net/sword-types/mets/dspace";
    }	
	

	@Override
	public Entry ingestDeposit(SwordSessionStructure swordSession)
			throws SWORDException {
		
		LOGGER.info("start ingestDeposit");
		
		return super.ingestDeposit(swordSession);
	}
	
	

} // ========================================================= //
