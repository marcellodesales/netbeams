package org.netbeams.dsp.platform;

import org.netbeams.dsp.DSPContext;
import org.netbeams.dsp.DSPException;
import org.netbeams.dsp.MessageBrokerAccessor;

class DSPContextImpl implements DSPContext {
	
	private MessageBrokerAccessor _msgBrokerAccessor;

	public DSPContextImpl() {
		_msgBrokerAccessor = null;
	}
	
	public synchronized void setMessageBrokerAccessor(MessageBrokerAccessor msgBrokerAccessor){
		_msgBrokerAccessor = msgBrokerAccessor;
	}
	
	@Override
	public synchronized MessageBrokerAccessor getDataBroker() throws DSPException {
		return _msgBrokerAccessor;
	}

}
