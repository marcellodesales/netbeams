package org.netbeams.dsp;

import java.io.Writer;
import java.util.Date;
import java.util.List;

import org.jdom.Element;

public class MessageBasicImpl implements Message {

	@Override
	public Object getBody() throws DMPException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ComponentIdentifier> getConsumers() throws DMPException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getCorrelationId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Date getCreationTime() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDeliveryMode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getMessageId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getNativeRepresentation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getPriority() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ComponentIdentifier getProducer() throws DMPException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MessageType getType() throws DMPException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void output(Writer writer) throws DMPException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setBody(Object body) throws DMPException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setConsumers(List<ComponentLocator> consumers)
			throws DMPException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setCorrelationId() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDeliveryMode(String deliveryMode) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setPriority() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Element getBodyContent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setBodyContent(Element element) {
		// TODO Auto-generated method stub
		
	}

}
