package org.netbeams.dsp.management;

import org.netbeams.dsp.DSPException;
import org.netbeams.dsp.management.ui.Buffer;
import org.netbeams.dsp.message.MessageContent;

public interface Manager {

	public String query(String nodeComponentId, String componenetType, String nodeAddress, MessageContent content) throws DSPException;

	public String update(String nodeComponentId, String componenetType, String nodeAddress, MessageContent content) throws DSPException;
	
	/**
	 * 
	 * @param interactionId
	 * @return
	 */
	public  MessageContent retrieveInteractionReply(String interactionId);
	
	public boolean isActive();
	
	public Buffer getBuffer();
	
	public String[][] getRegisteredDspComponents();
}
