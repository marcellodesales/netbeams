package edu.sfsu.netbeams.web;

import java.util.logging.Logger;


public class NetBeamsLogger {
	
	public static void logErrorMessages (Exception e) {
		Logger logger = Logger.getAnonymousLogger();
		String message = e.getMessage();
		for (StackTraceElement st : e.getStackTrace()) {
			message = message + "\n" + st.toString();
		}
		message = message + "\n-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------";
		logger.severe(message);		
		System.out.println(message);
	}
	
	
	public static void logInfoMessage (String message) {
		Logger logger = Logger.getAnonymousLogger();
		logger.info(message);
		System.out.println(message);
	}
	
}
