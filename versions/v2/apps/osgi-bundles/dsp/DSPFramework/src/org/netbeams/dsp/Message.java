package org.netbeams.dsp;

import java.io.Writer;
import java.util.Date;
import java.util.List;

import org.jdom.Element;
import org.netbeams.dsp.broker.MessageBrokerAccessor;

public interface Message {
	
	static public enum Category { 
		ACTION, CONFIGURATION, STATE, MEASUREMENT;	
	}
	
	public String getMessageId();
	
	public Date getCreationTime();
	
	public String getCorrelationId();
	public void setCorrelationId();
	
	public String getDeliveryMode();
	public void setDeliveryMode(String deliveryMode);
	
	public int getPriority();
	public void setPriority();
	
	
	/**
	 * 
	 * @return
	 */
	public MessageType getType() throws DMPException;
	
	
	/**
	 * Component that produce the data.
	 * 
	 * @return
	 */
	public ComponentIdentifier getProducer() throws DMPException;
	
	/**
	 * Optional list of consumers. The Platform will only deliver to these consumers even if more cosumers are registered 
	 * to consume this message.
	 * 
	 * When a message is used as request to {@link MessageBrokerAccessor#pull(Message)} it MUST have one, and only one, consumer.
	 * 
	 * @return
	 */
	public List<ComponentIdentifier> getConsumers() throws DMPException;
	
	/**
	 * Optionally informs which consumers this message can be delivered. This method MUST be called
	 * before the message is sent to the message broker
	 * 
	 * @param consumers Targets for this message
	 * @throws DMPException If the message already has consumers.
	 */
	public void setConsumers(List<ComponentLocator> consumers) throws DMPException;
		
	/**
	 * 
	 * @return
	 */
	public Object getBody() throws DMPException;
	
	/**
	 * 
	 * @param content
	 */
	public void setBody(Object body) throws DMPException;
	
	
	
	
	/**
	 * 
	 * @param writer
	 */
	public void output(Writer writer) throws DMPException;
	
	public Object getNativeRepresentation();

	public Element getBodyContent();

	public void setBodyContent(Element element);


}
