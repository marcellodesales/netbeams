package org.netbeams.dsp;

import org.netbeams.dsp.Message.Category;


public class MessageType {

	public static final char DELIMITER = ':';
	
	private Message.Category category;
	private String hierarchy;
	private String type;
	
	private int hashCode;
	
	/**
	 * 
	 * @param category Not null
	 * @param hierarchy Not null and non-empty
	 */
	public MessageType(Message.Category category, String hierarchy){
		this.category = category;
		this.hierarchy = hierarchy;
		init();
	}
	
	/**
	 * Create a MessageType from a message type String
	 * @param type
	 */
	public MessageType(String type) throws DMPException{
		parseType(type);
		init();
	}
	
	public String getType(){
		return type;
	}
	
	public Message.Category getCategory() {
		return category;
	}

	public String getHierarchy() {
		return hierarchy;
	}
	
	@Override
	public boolean equals(Object o){
		if(o instanceof MessageType) {
			MessageType msgType = (MessageType)o;
			return (category.equals(msgType.category) && hierarchy.equals(msgType.hierarchy));
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return hashCode;
	}
	
	@Override
	public String toString(){
		return type;
	}
	
	/////////////////////////////////////
	////////// Private Section //////////
	/////////////////////////////////////
	
	private void generateHashCode() {
		hashCode = type.hashCode();
	}

	private void init(){
		type = category.name() + DELIMITER + hierarchy;
		generateHashCode();		
	}
	
	private void parseType(String type) throws DMPException{
		if(type == null){
			throw new DMPException("Message type cannot be null");
		}
		boolean invalid = false;
		String errorMsg = null;
		
		int ind = type.indexOf(DELIMITER);
		if(ind <= 0){
			invalid = true;
			errorMsg = "Message Type has invalid format: [" + type + "]";
		}
		String catStr = type.substring(0, ind);
		String hierStr = type.substring(ind);
		
		Category cat = Category.valueOf(catStr);
		if(cat == null){
			invalid = true;
			errorMsg = "Message Type has invalid category: [" + type + "]";
			
		}
		if(hierStr == null || hierStr.length() == 0){
			invalid = true;
			errorMsg = "Message Type has invalid hierarquy: [" + type + "]";
		}
		if(!invalid){
			category = cat;
			hierarchy = hierStr;
		}else{
			throw new DMPException(errorMsg);
		}
	}
	
}
