package edu.columbia.cul.sword.utils;

import java.io.OutputStream;
import java.lang.reflect.Field;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import edu.columbia.cul.sword.holder.InfoStructure;

public class LogResutUtils {

	/*
	 * This method is relatively slow because it is using reflection, 
	 * so is intended to be used only in debug mode
	 */
    public static String printablePublicValues(Object obj){
    	return printablePublicValues(obj, "");
    }

	/*
	 * This method is relatively slow because it is using reflection, 
	 * so is intended to be used only in debug mode
	 */
    public static String printablePublicValues(Object obj, String offset){
    	
    	StringBuilder sb = new StringBuilder();
    	
		try {
			for(Field field : obj.getClass().getFields()){

				if(field.getAnnotation(InfoStructure.class) != null) {	
					sb.append("\nsub " + field.getName() + ":"  + printablePublicValues(field.get(obj), "    "));
				}else{				
				
					sb.append("\n" + offset + field.getName() 
							+ ", <" + field.getType().getCanonicalName() 
							+ ">, " + field.get(obj));
				}

			} 
		} catch (Exception e) {
			sb.append("Not able to process public values for class: " + obj.getClass().getCanonicalName() + "\n");
			sb.append(e.getMessage());
		}

		return sb.toString();
	}    	
	

    public static void print(Object entity, Class classObj, OutputStream out){
    	try {
			JAXBContext jaxbContext = JAXBContext.newInstance(classObj);
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			jaxbMarshaller.marshal(entity, out);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
} // ====================================================== //
