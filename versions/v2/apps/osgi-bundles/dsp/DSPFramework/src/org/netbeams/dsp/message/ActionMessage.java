package org.netbeams.dsp.message;

import org.netbeams.dsp.ComponentIdentifier;
import org.netbeams.dsp.data.MessageContent;

public class ActionMessage extends Message {
	
	public ActionMessage(ComponentIdentifier producer, MessageContent content) {
		super(producer, content);
	}

}
