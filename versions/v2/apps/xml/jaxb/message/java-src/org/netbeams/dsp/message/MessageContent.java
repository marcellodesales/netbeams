package org.netbeams.dsp.message;


public class MessageContent extends AbstractMessageContent {

	// TODO: Find way to turn this into an abstract class
	public String getContentType(){
		return getClass().getName();
	}

}
