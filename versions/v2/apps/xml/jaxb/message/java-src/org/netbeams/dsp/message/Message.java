package org.netbeams.dsp.message;


public class Message extends AbstractMessage {

	public static boolean isPojo(Object content){
		return (content instanceof MessageContent);
	}

}
