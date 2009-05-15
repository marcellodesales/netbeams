package org.netbeams.dsp;

/**
 * Define reserved DSP component Types.
 * 
 * #10/08/2008 - Kleber Sales - Creation
 *
 **/

import static org.netbeams.dsp.util.Constants.*;

public interface GlobalComponentTypeName {

	///////////////////////////////////////////
	////// DSP Component Type Hierachies //////
	///////////////////////////////////////////
	
	public static final String PLATFORM_HEIRARCHY = "org.dsp.platform";
	public static final String CHANNEL_HEIRARCHY = PLATFORM_HEIRARCHY + COMPONENT_TYPE_DELIMITER+ "channel";
	
	
	/////////////////////////////
	///////// DSP Types /////////
	/////////////////////////////

	public static final String NO_COMPONENT = PLATFORM_HEIRARCHY + COMPONENT_TYPE_DELIMITER + "no_component";
	public static final String MATCHER = PLATFORM_HEIRARCHY + COMPONENT_TYPE_DELIMITER + "matcher";
	public static final String DIRECTORY = PLATFORM_HEIRARCHY + COMPONENT_TYPE_DELIMITER + "directory";
	public static final String BROKER = PLATFORM_HEIRARCHY + COMPONENT_TYPE_DELIMITER + "broker";
	public static final String CHANNEL = PLATFORM_HEIRARCHY + COMPONENT_TYPE_DELIMITER + "channel";
	
}
