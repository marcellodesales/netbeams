package org.netbeams.dsp.message;


public class Message extends AbstractMessage {

	public static boolean isPojo(Object content){
		return (content instanceof MessageContent);
	}

	public void setComponentIdentifier(ComponentIdentifier compIdent){
		Header header = getHeader();
		if(header == null){
			header = new Header();
			setHeader(header);
		}
		header.setConsumer(compIdent);
	}
}
