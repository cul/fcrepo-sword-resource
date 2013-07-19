package edu.columbia.cul.sword.fileHandlers.impl;

import org.fcrepo.server.Context;
import org.fcrepo.server.storage.DOManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.columbia.cul.sword.exceptions.SWORDException;
import edu.columbia.cul.sword.fileHandlers.DepositHandler;
import edu.columbia.cul.sword.impl.DepositRequest;
import edu.columbia.cul.sword.xml.entry.Entry;

public class DefaultZipHandler extends DefaultDepositHandler implements DepositHandler {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultZipHandler.class.getName());

	public DefaultZipHandler() {
		m_contentType = "application/zip";
		m_packaging = null;
    }	
	
	@Override
	public Entry ingestDeposit(DepositRequest deposit, Context context, DOManager doManager)
			throws SWORDException {
		
		LOGGER.info("start ingestDeposit");
		
		return super.ingestDeposit(deposit, context, doManager);
	}
		
	

} // ========================================================= //
