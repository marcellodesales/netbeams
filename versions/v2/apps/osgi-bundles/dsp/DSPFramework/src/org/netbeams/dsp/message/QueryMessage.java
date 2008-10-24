package org.netbeams.dsp.message;

import org.netbeams.dsp.ComponentIdentifier;
import org.netbeams.dsp.data.MessageContent;


public class QueryMessage extends Message {

	public QueryMessage(ComponentIdentifier producer, MessageContent content) {
		super(producer, content);
	}

}
