package edu.columbia.cul.sword.fileHandlers;

import java.util.List;

public interface FileHandlerManager {

	public DepositHandler getHandler(String contentType, String packaging);	
	public DepositHandler getHandler(HandlerKey handlerKey);
	public List<DepositHandler>getHandlers();
	
}
