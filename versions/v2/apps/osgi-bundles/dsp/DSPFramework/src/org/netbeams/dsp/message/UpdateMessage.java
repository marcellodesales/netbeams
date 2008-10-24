package org.netbeams.dsp.message;

import org.netbeams.dsp.ComponentIdentifier;
import org.netbeams.dsp.data.MessageContent;


public class UpdateMessage extends Message {

	public UpdateMessage(ComponentIdentifier producer, MessageContent content) {
		super(producer, content);
	}

}
