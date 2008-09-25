/*
 * Created on Jul 1, 2005
 *
 */
package edu.sfsu.netbeams.web;

import java.io.*;
import java.util.*;


public class NetBEAMSConstants {


	public static final String SERVER_PATH = (new File("")).getAbsolutePath();
	public static final String WEB_ROOT = SERVER_PATH + "/../" + "webapps/ROOT/";


	public static final String MAIL_SERVER = "mailrl.sfsu.edu";
	public static final String SENDER_NAME = "dev@netbeams.dev.java.net";
	public static final String SENDER_ID = "bhuynh";
	public static final String SENDER_PASSWORD = "sbcsc848";

	public static final String DMP_CONFIGURATION = WEB_ROOT + "/admin/dmp_configuration.xml";
	public static final String SENSORS_CONFIGURATIONS = WEB_ROOT + "/admin/sensors.xml";
	public static final String KPIS_CONFIGURATIONS = WEB_ROOT + "/admin/kpi/kpis.xml";
	
	public static final String DATA_LOCATION = "/servlet/data/";
	public static final String DATA_DDS = WEB_ROOT + "/data/dds/";
	public static final String DATA_DAS = WEB_ROOT + "/data/das/";


}




