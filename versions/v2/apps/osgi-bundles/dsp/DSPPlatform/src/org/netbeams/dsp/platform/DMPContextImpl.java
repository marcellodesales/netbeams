package org.netbeams.dsp.platform;

import org.netbeams.dsp.DMPContext;
import org.netbeams.dsp.DMPException;
import org.netbeams.dsp.broker.MessageBrokerAccessor;

class DMPContextImpl implements DMPContext {
	
	private MessageBrokerAccessor _msgBrokerAccessor;

	public DMPContextImpl() {
		_msgBrokerAccessor = null;
	}
	
	public synchronized void setMessageBrokerAccessor(MessageBrokerAccessor msgBrokerAccessor){
		_msgBrokerAccessor = msgBrokerAccessor;
	}
	
	@Override
	public synchronized MessageBrokerAccessor getDataBroker() throws DMPException {
		return _msgBrokerAccessor;
	}

}
