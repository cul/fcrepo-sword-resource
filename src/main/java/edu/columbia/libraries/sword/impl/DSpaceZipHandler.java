package edu.columbia.libraries.sword.impl;

import org.fcrepo.server.storage.DOManager;

public class DSpaceZipHandler extends DefaultDepositHandler {

	public DSpaceZipHandler(DOManager mgmt) {
		super(mgmt);
		m_contentType = "application/zip";
		m_packaging = "http://purl.org/net/sword-types/mets/dspace";
	}

}
