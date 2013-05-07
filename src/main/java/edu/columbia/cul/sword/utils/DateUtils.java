package edu.columbia.libraries.sword.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class DateUtils {
	public static String FCREPO_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
	public static String ATOM_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
	
	public static String getFCRepoDate(Date date) {
		DateFormat df = new SimpleDateFormat(FCREPO_DATE_FORMAT);
		return df.format(date);
	}
	
	public static String getAtomDate(Date date) {
		DateFormat df = new SimpleDateFormat(ATOM_DATE_FORMAT);
		return df.format(date);
	}
	
	public static String getCurrentDate(boolean millis){
		
		return (millis) ? getFCRepoDate(new Date())
		                         : getAtomDate(new Date());
	}

}
