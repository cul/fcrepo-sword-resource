package edu.columbia.cul.sword.fileHandlers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FileHandlerManager {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(FileHandlerManager.class.getName());
	private Map<HandlerKey,DepositHandler>handlers;
	
	public FileHandlerManager(List<String>handlerNames) throws Exception {
		init(handlerNames);
	}
	
	
	private void init(List<String>handlerNames) throws Exception {
		
		LOGGER.info("FileHandlerManager init started.");
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

		LOGGER.info("FileHandlerManager init finished.");
	}
	
	public DepositHandler getHandler(String contentType, String packaging){
		
		return getHandler(new HandlerKey(contentType, packaging));
	}
	
	public DepositHandler getHandler(HandlerKey handlerKey){
		
		if(handlers.containsKey(handlerKey)){
			return handlers.get(handlerKey);
		}else{
			return handlers.get(new HandlerKey(null, null));
		}

	}	

} // ====================== //
