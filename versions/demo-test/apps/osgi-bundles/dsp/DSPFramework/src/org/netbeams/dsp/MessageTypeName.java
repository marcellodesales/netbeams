package org.netbeams.dsp;

import static org.netbeams.dsp.util.Constants.*;

public interface MessageTypeName {
	
	//////////////////////////
	///// Required Types /////
	//////////////////////////
	
	public static final String PROPERTIES = DSP_NE + MESSAGE_TYPE_DELIMITER + "property";
	
	///////////////////////////////
	///// Platform  Component /////
	///////////////////////////////

	public static final String MATCHER_MESSAGE_PARAMETERS = 
		DSP_NE + MESSAGE_TYPE_DELIMITER + MATCHER_NE + MESSAGE_TYPE_DELIMITER + "message_parameters";
	
	public static final String MATCHER_MATCH = 
		DSP_NE + MESSAGE_TYPE_DELIMITER + MATCHER_NE + MESSAGE_TYPE_DELIMITER + "match";

	public static final String CHANNEL = 
		DSP_NE + MESSAGE_TYPE_DELIMITER + MATCHER_NE + MESSAGE_TYPE_DELIMITER + "channel";

	
}
