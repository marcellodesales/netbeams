package org.netbeams.dsp;


public class ComponentDescriptor {
	private String type;
	private MessageTypes msgTypesProduced;
	private MessageTypes msgTypesConsummed;
	
	public ComponentDescriptor(){
		type = null;
		msgTypesConsummed = null;
		msgTypesProduced = null;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public MessageTypes getMsgTypesProduced() {
		return msgTypesProduced;
	}
	public void setMsgTypesProduced(MessageTypes msgTypesProduced) {
		this.msgTypesProduced = msgTypesProduced;
	}
	public MessageTypes getMsgTypesConsummed() {
		return msgTypesConsummed;
	}
	public void setMsgTypesConsummed(MessageTypes msgTypesConsummed) {
		this.msgTypesConsummed = msgTypesConsummed;
	}
	
}
