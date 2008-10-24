package org.netbeams.dsp.message;

import org.netbeams.dsp.ComponentIdentifier;
import org.netbeams.dsp.data.MessageContent;

public class CreateMessage extends Message {

	
	public CreateMessage(ComponentIdentifier producer, MessageContent content) {
		super(producer, content);
	}

}
