package edu.columbia.cul.sword.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import edu.columbia.cul.sword.holder.InfoStructure;

public class LogResutUtils {

	
//    public static String printableGetMethodsReturnValues(Object obj){
//    	
//    	StringBuilder sb = new StringBuilder();
//    	
//		try {
//			for(Method method : obj.getClass().getMethods()){
//				if(method.getName().startsWith("get")){
//					sb.append("\nmethod name:  " + method.getName() 
//							  + ", \nreturn type:  " + method.getReturnType().getCanonicalName() 
//				              + ", \nreturn value: " + method.invoke(obj) + "\n");
//					
//					
//				}
//			}
//		} catch (Exception e) {
//			sb.append("Not able to process methods for class: " + obj.getClass() + "\n");
//			sb.append(e.getMessage());
//		}
//
//		return sb.toString();
//	}
	
	
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
	
} // ====================================================== //
