package edu.columbia.cul.sword;

import org.fcrepo.server.Context;

import edu.columbia.cul.sword.xml.service.ServiceDocument;

public interface ServiceDocumentService {

	public ServiceDocument getServiceDocument(String id, Context context) throws SWORDException;
	
}
