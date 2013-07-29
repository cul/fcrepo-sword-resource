package edu.columbia.cul.fcrepo.sword.fileHandlers;

import java.util.List;

import edu.columbia.cul.sword.exceptions.SWORDException;

public interface FileHandlerManager {

	public DepositHandler getHandler(String contentType, String packaging) throws SWORDException;	
	public DepositHandler getHandler(HandlerKey handlerKey) throws SWORDException;
	public List<DepositHandler>getHandlers();
	
}
