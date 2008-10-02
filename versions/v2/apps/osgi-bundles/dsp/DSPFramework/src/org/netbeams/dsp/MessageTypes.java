package org.netbeams.dsp;

/**
 *	Contains a set of message types (namespace hierachy) organized by message category. 
 * 
 *  @author kleber
 */

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MessageTypes {
	
	private Collection<MessageType> types;
	// Fast access to types beloging to a category
	private Map<Message.Category, List<MessageType>> catTypes;
	
	public MessageTypes(){
		types = new ArrayList<MessageType>();
		catTypes = new HashMap<Message.Category, List<MessageType>>();
	}
	
	public Collection<MessageType> getType(Message.Category category){
		return catTypes.get(category);
	}
	
	public Collection<MessageType> getTypes(){
		return types;
	}
	
	public void addType(MessageType type){
		types.add(type);
		addToCategory(type);
	}

	private void addToCategory(MessageType type) {
		// TODO Implement
		
	}
}
