package org.netbeams.dsp.message;

/**
 * Message category is a tuple represention a message type and message content type. DSP components 
 * use the message type to express what messages/content it produce or consume. For instance a 
 * StockTick producer could state it produces:
 * 
 * (org.dsp.message.MeasurementMessage, "stock:tick")
 * 
 * #10/11/2008 - Kleber Sales - Creation
 *
 */

public class MessageCategory {

	private String messageType;
	private String messageContentType;
	
	public MessageCategory(String messageType, String messageContentType) {
		this.messageType = messageType;
		this.messageContentType = messageContentType;
	}
	
	public String getMessageType() {
		return messageType;
	}
	public String getMessageContentType() {
		return messageContentType;
	}

}
