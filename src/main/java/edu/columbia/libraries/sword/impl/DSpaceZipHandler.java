package edu.columbia.libraries.sword.impl;

import org.fcrepo.server.errors.ServerException;
import org.fcrepo.server.storage.DOManager;

public class DSpaceZipHandler extends DefaultDepositHandler {

	public DSpaceZipHandler(DOManager mgmt) throws ServerException {
		super(mgmt);
		m_contentType = "application/zip";
		m_packaging = "http://purl.org/net/sword-types/mets/dspace";
	}
	
	@Override
	public boolean handles(String contentType, String packaging) {
		return m_contentType.equals(contentType) && m_packaging.equals(packaging);
	}

}
