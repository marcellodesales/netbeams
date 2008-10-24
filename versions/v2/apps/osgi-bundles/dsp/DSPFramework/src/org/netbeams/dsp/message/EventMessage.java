package org.netbeams.dsp.message;

import org.netbeams.dsp.ComponentIdentifier;
import org.netbeams.dsp.data.MessageContent;

public class EventMessage extends Message {

	public EventMessage(ComponentIdentifier producer, MessageContent content) {
		super(producer, content);
	}

}
