package edu.columbia.libraries.sword.impl;

import javax.ws.rs.core.UriInfo;

import org.fcrepo.server.errors.ServerException;
import org.fcrepo.server.storage.DOManager;

public class DSpaceZipHandler extends DefaultDepositHandler {

	public DSpaceZipHandler(DOManager mgmt, UriInfo uriInfo) throws ServerException {
		super(mgmt, uriInfo);
		m_contentType = "application/zip";
		m_packaging = "http://purl.org/net/sword-types/mets/dspace";
	}
	
	@Override
	public boolean handles(String contentType, String packaging) {
		return m_contentType.equals(contentType) && m_packaging.equals(packaging);
	}

}
