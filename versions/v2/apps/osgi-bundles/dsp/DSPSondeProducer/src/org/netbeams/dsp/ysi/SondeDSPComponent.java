package org.netbeams.dsp.ysi;

import java.util.ArrayList;
import java.util.Collection;
import org.apache.log4j.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.netbeams.dsp.ysi.SondeDataContainer;
import org.netbeams.dsp.data.property.DSProperties;
import org.netbeams.dsp.data.property.DSProperty;
import org.netbeams.dsp.ComponentDescriptor;
import org.netbeams.dsp.DSPComponent;
import org.netbeams.dsp.DSPContext;
import org.netbeams.dsp.DSPException;
import org.netbeams.dsp.MessageBrokerAccessor;
import org.netbeams.dsp.MessageCategory;
import org.netbeams.dsp.MessageFactory;
import org.netbeams.dsp.message.Message;
import org.netbeams.dsp.message.AcknowledgementMessage;
import org.netbeams.dsp.message.ComponentIdentifier;
import org.netbeams.dsp.message.MeasureMessage;
import org.netbeams.dsp.message.QueryMessage;
import org.netbeams.dsp.message.UpdateMessage;
import org.w3c.dom.Node;


/**
 * 
 * @author Teresa L. Johnson <gamma.particle@gmail.com>
 *
 */
public class SondeDSPComponent implements DSPComponent {
	
	private static final Logger log = Logger.getLogger(SondeDSPComponent.class);	
	private static final ComponentDescriptor componentDescriptor;
	public static final String COMPONENT_TYPE = "org.netbeams.dsp.ysi";
	public static final String MSG_CONTENT_TYPE_SONDE_DATA = "sonde.data";
    
	private DSPContext context;
    private String componentNodeId;
    private SondeProducer producer;
    
    static {
        componentDescriptor = new ComponentDescriptor();

        Collection<MessageCategory> producedMessageCategories = new ArrayList<MessageCategory>();
        Collection<MessageCategory> consumedMessageCategories = new ArrayList<MessageCategory>();
        MessageCategory messageCategory = new MessageCategory(SondeDSPComponent.class.getName(),
                MSG_CONTENT_TYPE_SONDE_DATA);

        producedMessageCategories.add(messageCategory);
        componentDescriptor.setMsgCategoryProduced(producedMessageCategories);
        componentDescriptor.setMsgCategoryConsumed(consumedMessageCategories);
    }

    
    public SondeDSPComponent() {
    	log.debug("Instantiating the Sonde DSP component");
    };
    
    
    
    private void processMessage(Message message) {
		log.debug("message class = " + message.getClass().getName());
		
		if(message instanceof UpdateMessage){		
			processUpdate(message);
		}else if(message instanceof QueryMessage){	
			processQuery(message);			
		}
	}
    
    
    private void processUpdate(Message message) {
		String contentType = message.getContentType();
		
		log.debug("message content = " + contentType);
		
		if(SondeDataContainer.class.getName().equals(contentType)){
			Object content = message.getBody().getAny();
			try{
				JAXBContext jc = JAXBContext.newInstance("org.netbeams.dsp.data.property", SondeDataContainer.class.getClassLoader());
				Unmarshaller unmarshaller = jc.createUnmarshaller();
				DSProperties dspProperties = (DSProperties)unmarshaller.unmarshal((Node)content);
				for(DSProperty prop: dspProperties.getProperty()){
					log.debug("Update property = " + prop.getName() + " value = " + prop.getValue());
				}
			}catch(JAXBException e){
				log.error("Error unmarshalling the message", e);
				return;
			}
			sendBackAcknowledge(message);
		}
	}
    
    
    private void sendBackAcknowledge(Message message) {
		ComponentIdentifier ciProducer = message.getHeader().getProducer();
		String originalMessageId = message.getMessageID();
		
		Message replyMsg = null;
		try {
			// TODO: Dummy payload. Need to define it properly
			DSProperties props = new DSProperties();
			replyMsg = MessageFactory.newMessage2(AcknowledgementMessage.class, props, this, 
					ciProducer.getComponentLocator().getComponentNodeId(), 
					ciProducer.getComponentType(),
					ciProducer.getComponentLocator().getNodeAddress().getValue());
		} catch (DSPException e) {
			log.warn("Cannot create acknowledgement.", e);
		}		
		// Set correlation
		replyMsg.getHeader().setCorrelationID(originalMessageId);
		
		sendMessage(replyMsg);
	}
    
    
    
    private void processQuery(Message message) {
		DSProperties dspProperties = null;
		try{
			Object content = message.getBody().getAny();
			log.debug("Content class " + content.getClass().getName());
			
			JAXBContext jc = JAXBContext.newInstance("org.netbeams.dsp.data.property", org.netbeams.dsp.ysi.SondeDataContainer.class.getClassLoader());
			Unmarshaller unmarshaller = jc.createUnmarshaller();
			dspProperties = (DSProperties)unmarshaller.unmarshal((Node)content);
		}catch(JAXBException e){
			log.error("Error unmarshalling the message", e);
			return;
		}
		log.debug("Got query configuration for " + dspProperties.getProperty().get(0).getName());
		
		sendReply(message);		
	}
    
    
    private void sendMessage(Message message) {
		MessageBrokerAccessor messageBroker;
		try {
			messageBroker = context.getDataBroker();
			if(messageBroker != null){
				messageBroker.send(message);
				log.debug("Reply message sent back");
			}else{
				log.debug("Message broker not available");
			}
		} catch (DSPException e) {
			log.warn("Cannot send reply", e);
		}
	}

    private void sendReply(Message message) {
		
		DSProperties props = new DSProperties();
		DSProperty freq = new DSProperty();
		freq.setName("sampling_frequency");
		freq.setValue("10");
		
		ComponentIdentifier ciProducer = message.getHeader().getProducer();
		String originalMessageId = message.getMessageID();
		Message replyMsg = null;
		try {
			replyMsg = MessageFactory.newMessage2(MeasureMessage.class, props, this, 
					ciProducer.getComponentLocator().getComponentNodeId(), 
					ciProducer.getComponentType(),
					ciProducer.getComponentLocator().getNodeAddress().getValue());
		} catch (DSPException e) {
			log.warn("Cannot create reply", e);
		}
		
		// Set correlation
		replyMsg.getHeader().setCorrelationID(originalMessageId);
		
		MessageBrokerAccessor messageBroker;
		try {
			messageBroker = context.getDataBroker();
			if(messageBroker != null){
				messageBroker.send(replyMsg);
				log.debug("Reply message sent back");
			}else{
				log.debug("Message broker not available");
			}
		} catch (DSPException e) {
			log.warn("Cannot send reply", e);
		}		
	}
    
    
 // Implemented methods of the DSPComponent interface.
    
    
    public void initComponent(String componentNodeId, DSPContext context) throws DSPException {
    	log.info("Initializing Sonde DSP component: " + componentNodeId);
    	this.context = context;
		this.componentNodeId = componentNodeId;
	}
    
    public String getComponentNodeId() {
    	return componentNodeId;
	}

	public String getComponentType() {
		return COMPONENT_TYPE;
	}
	
	
	public ComponentDescriptor getComponentDescriptor() {
		return componentDescriptor;	
	}

    
	public void deliver(Message message) throws DSPException {
		log.debug("Delivering Message....");
		processMessage(message);
	}
	

	public Message deliverWithReply(Message message) throws DSPException {
		log.debug("Delivering message with reply.");
		return null;
	}

	
	public Message deliverWithReply(Message message, long waitTime) throws DSPException {
		log.debug("Delivering message with reply w/wait.");
		return null;
	}


	public void startComponent() throws DSPException {
		log.info("Starting Sonde DSP component");
		producer = new SondeProducer(context);
		producer.startProducer();
	}
	

	public void stopComponent() throws DSPException {
		log.info("Stopping Sonde DSP component");
		producer.stopProducer();
	}

}
