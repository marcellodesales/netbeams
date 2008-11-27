package org.netbeams.dsp;

import java.util.Collection;



public class ComponentDescriptor {
	
	private String type;
	private Collection<MessageCategory> msgCategoryProduced;
	private Collection<MessageCategory> msgCategoryConsumed;
	
	public ComponentDescriptor(){
		type = null;
		msgCategoryProduced = null;
		msgCategoryConsumed = null;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Collection<MessageCategory> getMsgCategoryProduced() {
		return msgCategoryProduced;
	}

	public void setMsgCategoryProduced(
			Collection<MessageCategory> msgCategoryProduced) {
		this.msgCategoryProduced = msgCategoryProduced;
	}

	public Collection<MessageCategory> getMsgCategoryConsumed() {
		return msgCategoryConsumed;
	}

	public void setMsgCategoryConsumed(	Collection<MessageCategory> msgCategoryConsummed) {
		this.msgCategoryConsumed = msgCategoryConsummed;
	}

	
}
