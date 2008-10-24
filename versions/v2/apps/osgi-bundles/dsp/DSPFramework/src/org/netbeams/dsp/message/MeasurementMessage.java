package org.netbeams.dsp.message;

import org.netbeams.dsp.ComponentIdentifier;
import org.netbeams.dsp.data.MessageContent;

public class MeasurementMessage extends Message {

	public MeasurementMessage(ComponentIdentifier producer, MessageContent content) {
		super(producer, content);
	}

}
