package edu.columbia.libraries.sword;

import org.fcrepo.server.Context;

import edu.columbia.libraries.sword.xml.service.ServiceDocument;

public interface ServiceDocumentService {

	public ServiceDocument getServiceDocument(String id, Context context) throws SWORDException;
}
