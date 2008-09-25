/*
 * Created on Jun 9, 2005
 *
 */
package edu.sfsu.netbeams.web;


import java.util.*;
import java.text.*;


public class Util {
	
	public static final String NRSS_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS'Z'z";
	public static final String CSV_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSSz";
	public static final String DATE_FORMAT_1 = "M/d/yy hh:mm:ss a z";
	public static final String DATE_FORMAT_2 = "M/d/yy HH:mm:ss z";
	public static final String DATE_FORMAT_3 = "MMMM dd, yyyy HH:mm:ss";
	

	private static final String TIME_ZONE = "GMT";
	
	private Util () {}
	
	public static Date stringToDate (String date, String format) throws Exception {
		SimpleDateFormat dateFormat = new SimpleDateFormat (format);
		if (! date.endsWith(TIME_ZONE)) {
			date = date + TIME_ZONE;
		}
		return dateFormat.parse(date);
	}
	
	
	public static String dateToString (Date date, String format) throws Exception {
		SimpleDateFormat dateFormat = new SimpleDateFormat (format);
		return dateFormat.format(date);
	}
	
}


