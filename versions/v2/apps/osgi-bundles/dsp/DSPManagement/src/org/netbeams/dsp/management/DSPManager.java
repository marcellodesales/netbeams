
package org.netbeams.dsp.management;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.netbeams.dsp.ComponentDescriptor;
import org.netbeams.dsp.data.property.DSProperties;
import org.netbeams.dsp.data.property.DSProperty;
import org.netbeams.dsp.management.ui.Buffer;
import org.netbeams.dsp.management.ui.PropertyUI;
import org.netbeams.dsp.message.AbstractMessage;
import org.netbeams.dsp.message.AcknowledgementMessage;
import org.netbeams.dsp.message.Body;
import org.netbeams.dsp.message.ComponentIdentifier;
import org.netbeams.dsp.message.ComponentLocator;
import org.netbeams.dsp.message.CreateMessage;
import org.netbeams.dsp.message.DSPMessagesFactory;
import org.netbeams.dsp.message.Header;
import org.netbeams.dsp.message.MeasureMessage;
import org.netbeams.dsp.message.MessageContent;
import org.netbeams.dsp.message.MessagesContainer;
import org.netbeams.dsp.message.NodeAddress;
import org.netbeams.dsp.message.QueryMessage;
import org.netbeams.dsp.message.UpdateMessage;
import org.netbeams.dsp.DSPComponent;
import org.netbeams.dsp.DSPContext;
import org.netbeams.dsp.DSPException;
import org.netbeams.dsp.MessageBrokerAccessor;
import org.netbeams.dsp.MessageFactory;
import org.netbeams.dsp.message.Message;
import org.netbeams.dsp.util.DSPUtils;
import org.netbeams.dsp.util.NetworkUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.sun.corba.se.pept.broker.Broker;


public class DSPManager implements Manager, DSPComponent
{
	private static final Logger log = Logger.getLogger(DSPManager.class);
			
    // Component Type
    public final static String COMPONENT_TYPE = "org.netbeams.dsp.management";

    private static ComponentDescriptor componentDescriptor;
    
    private boolean isActive;

    private String componentNodeId;

    private DSPContext context;
       
    private Map<String, Boolean> pendingReply;
    private Map<String, Message> repliedMsgs;
    
    private Buffer buffer;
    
    private Map<String, String> regDspComps;
    
    public DSPManager(){
    	isActive = false;
    	pendingReply = new HashMap<String, Boolean>();
    	repliedMsgs = new HashMap<String, Message>();
    	buffer = new Buffer();
    	regDspComps = new HashMap<String, String>();
    }

    /////////////////////////////////////////////
    ////////// DSP Component Interface //////////
    /////////////////////////////////////////////

    /**
     * @Override 
     */
    public String getComponentNodeId()
    {
        return componentNodeId;
    }

    /**
     * @Override 
     */
    public String getComponentType()
    {
        return COMPONENT_TYPE;
    }

    /**
     * @Override 
     */
    public void initComponent(String componentNodeId, DSPContext context) throws DSPException {
        log.info("Initing component " + componentNodeId);
        this.componentNodeId = componentNodeId;
        this.context = context;
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
    public void startComponent(){
        log.info("Starting component...");  
        startUI();
        isActive = true;
    }

	/**
     * @Override 
     */
    public void stopComponent(){
        log.info("stopping component.");  
        isActive = false;
    }

    /**
     * @Override 
     */
    public void deliver(Message message) throws DSPException {
        log.debug("Message delivered " + messageSummary(message));
        sendToBuffer(message);
        processMessage(message);
    }
    /**
	 * @Override
	 */
    public Message deliverWithReply(Message request)
        throws DSPException
    {
        // TODO How we should handle an invokation to this method when the
        // component is not a consumer?
        return null;
    }

    //@Override
    public Message deliverWithReply(Message message, long waitTime)
        throws DSPException
    {
        // TODO Auto-generated method stub
        return null;
    }

    //////////////////////////////////////
    ////////// Public Interface //////////
    //////////////////////////////////////

    /**
     * @Override
     */	
    public boolean isActive(){
    	return isActive;
    }
    
    /**
     * @throws DSPException 
     * @Override
     */
	public String query(String nodeComponentId, String componentType, String nodeAddress, MessageContent content) 
		throws DSPException 
	{
		log.debug("Query message for component=" + nodeComponentId + " type=" + componentType + " node=" + nodeAddress + " content type= " + content.getContentType());

//		Message msg = MessageFactory.newMessage2(QueryMessage.class, content, this, nodeComponentId, componentType, nodeAddress);

		Message msg = createMessage(nodeComponentId, componentType, nodeAddress, content);
		
		registerPendingMessage(msg.getMessageID());
		sendMessage(msg);

		return msg.getMessageID();
	}
	

	/**
     * @throws DSPException 
     * @Override
     */
	public String update(String nodeComponentId, String componentType, String nodeAddress, MessageContent content) 
		throws DSPException 
	{
		log.debug("Update message for component=" + nodeComponentId + " type=" + componentType + " node=" + nodeAddress + " content type= " + content.getContentType());

		Message msg = MessageFactory.newMessage2(UpdateMessage.class, content, this, nodeComponentId, componentType, nodeAddress);
		
		registerPendingMessage(msg.getMessageID());
		sendMessage(msg);

		return msg.getMessageID();
	}

	/**
     * @Override
     */	
	public  MessageContent retrieveInteractionReply(String interactionId){
		Message msg = repliedMsgs.get(interactionId);
		if(msg !=  null){
			return (MessageContent)msg.getBody().getAny();
		}
		return null;
	}
    
	/**
     * @Override
     */	
	public Buffer getBuffer()
	{
		return buffer;
	}

	/**
     * @Override
     */	
	public String[][] getRegisteredDspComponents()
	{
		String[][] result = new String[regDspComps.size()][2];
		int x = 0;
		for(Map.Entry<String, String> entry: regDspComps.entrySet()){
			result[x][0] = entry.getKey();
			result[x][1] = entry.getValue();
			++x;
		}
		return result;
	}
  
    /////////////////////////////////////
    ////////// Private Section //////////
    /////////////////////////////////////
	
     private void startUI() {
    	// Create component list
    	List<String> compNodeIds = new ArrayList<String>();
    	compNodeIds.add("DSPStockProducer");
		PropertyUI.setComponents(compNodeIds);
	}
    
	
    private void registerPendingMessage(String messageId) {
    	pendingReply.put(messageId, Boolean.TRUE);
    }
	
	private String messageSummary(Message message) {
		StringBuilder b = new StringBuilder();
		b.append("msg_type=").append(message.getClass().getName());
		ComponentIdentifier producer =  message.getHeader().getProducer();
		b.append(" producer=").append(producer.getComponentLocator().getComponentNodeId());
		b.append(" node=").append(producer.getComponentLocator().getNodeAddress().getValue());
		b.append(" data type=").append(message.getContentType());
		return b.toString();
	}
	
    private String messageSummaryToBuffer(Message message) {
        StringBuilder buff = new StringBuilder();
        buff.append("[Producer]\n");
        buff.append("Address=").append(message.getHeader().getProducer().getComponentLocator().getNodeAddress().getValue()).append("; ");       
        buff.append("Type=").append(message.getHeader().getProducer().getComponentType()).append("; ");
        buff.append("[Content]\n");
        buff.append("Type=").append(message.getContentType()).append("; ").append("\n");
        return buff.toString();
    }	

    private void processMessage(Message message) {
    	// Is it information about DSP Component
    	if(message instanceof CreateMessage){
    		processCreateMessage(message);
    		return;
    	}
		// Obtain correlation id
		Header header = message.getHeader();
		String corrId = (String)header.getCorrelationID();
		if(corrId != null){
			log.debug("Correlation id " + corrId);
			
			Boolean b = pendingReply.remove(corrId);			
			if(b != null){
				repliedMsgs.put(corrId, message);
			}else{
				log.warn("No message associated with correlatio id" +  corrId);
			}
		}else{
			log.debug("No correlation found in message " + message.getMessageID());
		}
	}
    
	private void sendToBuffer(Message message){
    	buffer.add(messageSummaryToBuffer(message));
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
	
    private Message  createMessage(String nodeComponentId, String componentType,
			String nodeAddress, MessageContent content) 
    {
    	// Create Producer
    	// TODO: Simplify
		String localIPAddress = NetworkUtil.getCurrentEnvironmentNetworkIp();
        ComponentIdentifier producer = DSPMessagesFactory.INSTANCE.makeDSPComponentIdentifier(
                getComponentNodeId(), localIPAddress, getComponentType());
        // Consumer
        String nodeAddStr = getNodeAddressAsString(nodeComponentId, componentType);
        ComponentIdentifier consumer = DSPMessagesFactory.INSTANCE.makeDSPComponentIdentifier(
        		nodeComponentId, nodeAddStr, componentType);     
        
        Header header = DSPMessagesFactory.INSTANCE.makeDSPMessageHeader(null, producer, consumer);
            
    	Message message = DSPMessagesFactory.INSTANCE.makeDSPQueryMessage(header, content);
    	return message;
	}
	
	private String getNodeAddressAsString(String nodeComponentId, String componentType) 
	{
		String ip = regDspComps.get(nodeComponentId);
		return (ip != null)? ip : "LOCAL";
	}

	private void processCreateMessage(Message message) {
		DSProperties props = (DSProperties)message.getBody().getAny();
		String dspComp = null;
		String ip = null;
		for(DSProperty prop: props.getProperty()){
			if(prop.getName().equals("DSPCOMPONENT")){
				dspComp = prop.getValue();
			}else if(prop.getName().equals("IP")){
				ip = prop.getValue();
			}
		}
		regDspComps.put(dspComp, ip);
	}
	
}
