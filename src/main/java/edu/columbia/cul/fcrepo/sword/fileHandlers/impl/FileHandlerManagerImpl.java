package edu.columbia.cul.fcrepo.sword.fileHandlers.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.columbia.cul.fcrepo.sword.fileHandlers.DepositHandler;
import edu.columbia.cul.fcrepo.sword.fileHandlers.FileHandlerManager;
import edu.columbia.cul.fcrepo.sword.fileHandlers.HandlerKey;
import edu.columbia.cul.sword.exceptions.SWORDException;


public class FileHandlerManagerImpl implements FileHandlerManager {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(FileHandlerManagerImpl.class.getName());
	private Map<HandlerKey,DepositHandler>handlers;
	
	public FileHandlerManagerImpl(List<String>handlerNames) throws Exception {
		init(handlerNames);
	}
	
	
	private void init(List<String>handlerNames) throws Exception {
		
		LOGGER.info("FileHandlerManagerImpl init started.");
		handlers = new HashMap<HandlerKey,DepositHandler>();
		
		for(String className : handlerNames){
			try {
				DepositHandler handler = (DepositHandler)Class.forName(className).newInstance();
				handlers.put(new HandlerKey(handler.getContentType(), handler.getPackaging()), handler);
				LOGGER.info(className + " added to the FileHandlers list.");
			} catch (InstantiationException e) {
				String msg = "Couldn't instanciate: " + className;
				LOGGER.error(msg);
				e.printStackTrace();
				throw new Exception(msg, e);
			} catch (IllegalAccessException e) {
				String msg = "Illegal Access Exception for class: " + className + " in CLASSPATH";
				LOGGER.error(msg);
				throw new Exception(msg, e);
			} catch (ClassNotFoundException e) {
				String msg = "Couldn't find class: " + className + " in CLASSPATH";
				LOGGER.error(msg);
				throw new Exception(msg, e);
			}
		}

		LOGGER.info("FileHandlerManagerImpl init finished.");
	}
	
	public DepositHandler getHandler(String contentType, String packaging) throws SWORDException{
		
		return getHandler(new HandlerKey(contentType, packaging));
	}
	
	public DepositHandler getHandler(HandlerKey handlerKey) throws SWORDException {

		if(handlers.containsKey(handlerKey)){
			return handlers.get(handlerKey);
		}else{
			throw new SWORDException(SWORDException.UNSUPPORTED_MEDIA_TYPE);
		}
	}	
	
	public List<DepositHandler>getHandlers(){
		return new ArrayList<DepositHandler>(handlers.values());
	}

} // ====================== //
