package org.netbeams.dsp.message.matcher;

import org.netbeams.dsp.ComponentIdentifier;
import org.netbeams.dsp.MessageType;

public class MatchRequest {
	
	private ComponentIdentifier producer;
	private MessageType messageType;
	
	public ComponentIdentifier getProducer() {
		return producer;
	}
	public void setProducer(ComponentIdentifier producer) {
		this.producer = producer;
	}
	public MessageType getMessageType() {
		return messageType;
	}
	public void setMessageType(MessageType messageType) {
		this.messageType = messageType;
	}

}
