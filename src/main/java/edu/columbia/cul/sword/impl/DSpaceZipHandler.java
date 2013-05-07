package edu.columbia.libraries.sword.impl;

import java.util.Set;

import org.fcrepo.server.errors.ServerException;
import org.fcrepo.server.storage.DOManager;

public class DSpaceZipHandler extends DefaultDepositHandler {

	public DSpaceZipHandler(DOManager mgmt, Set<String> collectionIds) throws ServerException {
		super(mgmt, collectionIds);
		m_contentType = "application/zip";
		m_packaging = "http://purl.org/net/sword-types/mets/dspace";
	}
	
	@Override
	public boolean handles(String contentType, String packaging) {
		return m_contentType.equals(contentType) && m_packaging.equals(packaging);
	}

}
