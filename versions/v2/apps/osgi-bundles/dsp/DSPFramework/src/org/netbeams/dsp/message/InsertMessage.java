package org.netbeams.dsp.message;

import org.netbeams.dsp.ComponentIdentifier;
import org.netbeams.dsp.data.MessageContent;

public class InsertMessage extends Message {

	public InsertMessage(ComponentIdentifier producer, MessageContent content) {
		super(producer, content);
	}

}
