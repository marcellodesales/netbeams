package org.netbeams.dsp.message;

import org.netbeams.dsp.ComponentIdentifier;
import org.netbeams.dsp.data.MessageContent;

public class DeleteMessage extends Message {

	public DeleteMessage(ComponentIdentifier producer, MessageContent content) {
		super(producer, content);
	}

}
