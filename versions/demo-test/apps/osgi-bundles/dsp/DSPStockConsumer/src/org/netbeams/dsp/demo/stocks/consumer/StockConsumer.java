package org.netbeams.dsp.demo.stocks.consumer;


import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;
import org.netbeams.dsp.ComponentDescriptor;
import org.netbeams.dsp.message.ComponentLocator;
import org.netbeams.dsp.DSPComponent;
import org.netbeams.dsp.DSPContext;
import org.netbeams.dsp.DSPException;
import org.netbeams.dsp.MessageBrokerAccessor;
import org.netbeams.dsp.data.property.DSProperties;
import org.netbeams.dsp.data.property.DSProperty;
import org.netbeams.dsp.demo.stock.StockTick;
import org.netbeams.dsp.demo.stock.StockTicks;
import org.netbeams.dsp.message.ComponentIdentifier;
import org.netbeams.dsp.message.DSPMessagesFactory;
import org.netbeams.dsp.message.Header;
import org.netbeams.dsp.message.MeasureMessage;
import org.netbeams.dsp.message.Message;
import org.netbeams.dsp.message.MessageContent;
import org.netbeams.dsp.message.QueryMessage;
import org.netbeams.dsp.message.UpdateMessage;
import org.netbeams.dsp.util.NetworkUtil;
import org.w3c.dom.Node;

public class StockConsumer implements DSPComponent{
	
	
	private static final Logger log = Logger.getLogger(StockConsumer.class);
	
	// Component Type
	public final static String COMPONENT_TYPE = "org.netbeams.dsp.demo.stocks.consumer";
	
	private static ComponentDescriptor componentDescriptor;

	
	private String componentNodeId;
	
	private DSPContext context;
	private ComponentLocator locator;
	private ComponentDescriptor descriptor;

	/////////////////////////////////////////////
	////////// DSP Component Interface //////////
	/////////////////////////////////////////////


	/**
	 * @Override
	 */
	public String getComponentNodeId() {
		return componentNodeId;
	}

	/**
	 * @Override
	 */
	public String getComponentType() {
		return COMPONENT_TYPE;
	}

	/**
	 * @Override
	 */
	public void initComponent(String componentNodeId, DSPContext context) throws DSPException {
		log.info("Initializing...");
		
		this.context = context;
		this.componentNodeId = componentNodeId;
	}

	/**
	 * @Override
	 */
	public ComponentDescriptor getComponentDescriptor() {
		return componentDescriptor;
	}


	/**
	 * @Override
	 */
	public void startComponent() {
		log.info("startComponent()");
	}
	
	/**
	 * @Override
	 */
	public void stopComponent() {
		log.info("stopComponent()");
	}

	/**
	 * @Override
	 */
	public void deliver(Message message) throws DSPException {
		processMessage(message);
	}

	/**
	 * @Override
	 */
	public Message deliverWithReply(Message request)
			throws DSPException {
		// TODO How we should handle an invokation to this method when the component is not a consumer?
		return null;
	}
	
	/**
	 * @Override
	 */
	public Message deliverWithReply(Message message, long waitTime)
			throws DSPException {
		// TODO Auto-generated method stub
		return null;
	}
	
	/////////////////////////////////////
	////////// Privage Section //////////
	/////////////////////////////////////


	private void processMessage(Message message) {
		log.debug("message class=" + message.getClass().getName());
		
		if(message instanceof UpdateMessage){		
			processUpdate(message);
		}else if(message instanceof QueryMessage){	
			processQuery(message);			
		}else if(message instanceof MeasureMessage){
			StockTicks stockTicks = null;
			Object content = message.getBody().getAny();
			if(Message.isPojo(content)){
				log.debug("StockConsumer.processMessage(): Message Received....");
				return;
			}else{
				try{
					JAXBContext jc = JAXBContext.newInstance("org.netbeams.dsp.demo.stock",
							StockTicks.class.getClassLoader());
					Unmarshaller unmarshaller = jc.createUnmarshaller();
					stockTicks = (StockTicks)unmarshaller.unmarshal((Node)content);
				}catch(JAXBException e){
					log.error("Error unmarhalling the message", e);
					return;
				}

			}
			
			for(StockTick tick: stockTicks.getStockTick()){
				log.debug("Symble:" + tick.getSymbol() + "  " + "Value:" + tick.getValue());
			}
		}
	}
	
	private void processQuery(Message message) {
		MessageContent content = message.getBody().getAny();
		log.debug("Content class " + content.getClass().getName());
		
		if(content instanceof DSProperties){
			log.debug("Got query configuration");	
			sendReply(message);					
		}
	}

	
	
	private void processUpdate(Message message) {
		Object content = message.getBody().getAny();
		
		log.debug("Content class " + content.getClass().getName());
		
		try{
			JAXBContext jc = JAXBContext.newInstance("org.netbeams.dsp.data.property",
					StockTicks.class.getClassLoader());
			Unmarshaller unmarshaller = jc.createUnmarshaller();
			DSProperties dspProperties=(DSProperties)unmarshaller.unmarshal((Node)content);
			for(DSProperty prop: dspProperties.getProperty()){
				log.debug("Update property=" + prop.getName() + " value=" + prop.getValue());
			}
		}catch(JAXBException e){
			log.error("Error unmarhalling the message", e);
			return;
		}
		sendBackAcknowledge(message);
	}

	private void sendBackAcknowledge(Message message) {
		//TODO: Not sure whether we can send a message with no payload
		DSProperties props = new DSProperties();
		
		// Obtain original producer
		ComponentIdentifier origProducer = message.getHeader().getProducer();
		String originalMessageId = message.getMessageID();
		// Create reply  message
		
		String localIPAddress = NetworkUtil.getCurrentEnvironmentNetworkIp();
        ComponentIdentifier producer = DSPMessagesFactory.INSTANCE.makeDSPComponentIdentifier(
                getComponentNodeId(), localIPAddress, props.getContentContextForJAXB());
		       
        Header header = DSPMessagesFactory.INSTANCE.makeDSPMessageHeader(null, producer, origProducer);
    	header.setCorrelationID(originalMessageId);
            
    	Message replyMsg = DSPMessagesFactory.INSTANCE.makeDSPAcknowledgementMessage(header, props);
    	sendMessage(replyMsg);	
	}

	private void sendReply(Message message)
	{
		DSProperties props = new DSProperties();
		DSProperty freq = new DSProperty();
		freq.setName("#message");
		freq.setValue("15");
		props.getProperty().add(freq);
		
		// Obtain original producer
		ComponentIdentifier origProducer = message.getHeader().getProducer();
		String originalMessageId = message.getMessageID();
		// Create reply  message
		
		String localIPAddress = NetworkUtil.getCurrentEnvironmentNetworkIp();
        ComponentIdentifier producer = DSPMessagesFactory.INSTANCE.makeDSPComponentIdentifier(
        		getComponentNodeId(), localIPAddress, getComponentType());
		       
        Header header = DSPMessagesFactory.INSTANCE.makeDSPMessageHeader(null, producer, origProducer);
    	header.setCorrelationID(originalMessageId);
            
    	Message replyMsg = DSPMessagesFactory.INSTANCE.makeDSPMeasureMessage(header, props);
    	sendMessage(replyMsg);	
	}

	private void sendMessage(Message message) 
	{
		MessageBrokerAccessor messageBroker;
		try {
			messageBroker = context.getDataBroker();
			if(messageBroker != null){
				messageBroker.send(message);
				log.debug("Reply mensage sent back");
			}else{
				log.debug("Message broker not available");
			}
		} catch (DSPException e) {
			log.warn("Cannot send reply", e);
		}
	}
	

}
