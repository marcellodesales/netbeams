
package org.netbeams.dsp.management;



import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.netbeams.dsp.ComponentDescriptor;
import org.netbeams.dsp.DSPComponent;
import org.netbeams.dsp.DSPContext;
import org.netbeams.dsp.DSPException;
import org.netbeams.dsp.MessageBrokerAccessor;
import org.netbeams.dsp.data.property.DSProperties;
import org.netbeams.dsp.data.property.DSProperty;
import org.netbeams.dsp.demo.mouseactions.MouseActionsContainer;
import org.netbeams.dsp.management.ui.Buffer;
import org.netbeams.dsp.message.ComponentIdentifier;
import org.netbeams.dsp.message.CreateMessage;
import org.netbeams.dsp.message.DSPMessagesFactory;
import org.netbeams.dsp.message.Header;
import org.netbeams.dsp.message.Message;
import org.netbeams.dsp.message.MessageContent;
import org.netbeams.dsp.message.QueryMessage;
import org.netbeams.dsp.message.UpdateMessage;
import org.netbeams.dsp.util.NetworkUtil;
import org.netbeams.dsp.ysi.SondeDataContainer;
import org.netbeams.dsp.ysi.SondeDataType;


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
    
    // Component ID => {IP,Component Type}
    private Map<String, String[]> regDspComps;
    
    public DSPManager(){
    	isActive = false;
    	pendingReply = new HashMap<String, Boolean>();
    	repliedMsgs = new HashMap<String, Message>();
    	buffer = new Buffer();
    	regDspComps = new HashMap<String, String[]>();
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
		log.info("Query message for component=" + nodeComponentId + " type=" + componentType + " node=" + nodeAddress + " content type= " + content.getContentType());

		Message msg = createMessage(nodeComponentId, componentType, nodeAddress, content, QueryMessage.class);

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
		log.info("Update message for component=" + nodeComponentId + " type=" + componentType + " node=" + nodeAddress + " content type= " + content.getContentType());

		Message msg = createMessage(nodeComponentId, componentType, nodeAddress, content, UpdateMessage.class);
		
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
		String[][] result = new String[regDspComps.size()][3];
		int x = 0;
		for(Map.Entry<String, String[]> entry: regDspComps.entrySet()){
			result[x][0] = entry.getKey();
			result[x][1] = entry.getValue()[0];
			result[x][2] = entry.getValue()[1];
			++x;
		}
		return result;
	}
  
    /////////////////////////////////////
    ////////// Private Section //////////
    /////////////////////////////////////
	
     private void startUI() {
    	// Create component list
//    	List<String> compNodeIds = new ArrayList<String>();
//    	compNodeIds.add("DSPStockProducer");
//		PropertyUI.setComponents(compNodeIds);
	}
    
	
    private void registerPendingMessage(String messageId) {
    	log.info("Registering pending message " + messageId);
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
        buff.append("[Producer]");
        buff.append("Addr=").append(message.getHeader().getProducer().getComponentLocator().getNodeAddress().getValue()).append("; ");       
        buff.append("Type=").append(message.getHeader().getProducer().getComponentType()).append("; $");
        buff.append("[Content]");
        buff.append("Type=").append(message.getContentType()).append("; $");
        MessageContent content = message.getBody().getAny();
        if(content instanceof DSProperties){
            DSProperties props = (DSProperties)content;
            for(DSProperty p: props.getProperty()){
                buff.append(p.getName()).append('=').append(p.getValue()).append(";");
            }
            buff.append("$");
        }else 
        if(content instanceof SondeDataContainer){
            SondeDataContainer sdc = (SondeDataContainer)content;
            for (SondeDataType sdt: sdc.getSondeData()){
                buff.append(sdt.getDateString()).append("    ").append(sdt.getTimeString()).append("    ").append(sdt.getTemp()).append("    ").append(sdt.getSpCond()).append("    ").append(sdt.getCond()).append("    ").append(sdt.getResist()).append("    ").append(sdt.getSal()).append("    ").append(sdt.getPress()).append("    ").append(sdt.getDepth()).append("    ").append(sdt.getPH()).append("    ").append(sdt.getPhmV()).append("    ").append(sdt.getODOSat()).append("    ").append(sdt.getODOConc()).append("    ").append(sdt.getTurbid()).append("    ").append(sdt.getBattery());
            }
            buff.append("$");
        } else
        if (content instanceof MouseActionsContainer) {
            MouseActionsContainer mac = (MouseActionsContainer)content;
            buff.append("Size: "+mac.getMouseAction().size()).append("    ").append("Action 5" + mac.getMouseAction().get(5).getEvent() + " " + mac.getMouseAction().get(5).getButton() + " at " + "["+mac.getMouseAction().get(5).getX()+","+mac.getMouseAction().get(5).getX()+"]");
        }        
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
			String nodeAddress, MessageContent content, Class<?> messageClass) 
    {
    	// Create Producer
    	// TODO: Simplify
		String localIPAddress = NetworkUtil.getCurrentEnvironmentNetworkIp();
        ComponentIdentifier producer = DSPMessagesFactory.INSTANCE.makeDSPComponentIdentifier(
                getComponentNodeId(), localIPAddress, getComponentType());
        // Create Consumer
        
        // IP and type
        String[] compInfo = getNodeAddressAsString(nodeComponentId);
        if(compInfo == null){
        	log.error("No component information for " + nodeComponentId);
        	return null;
        }
        ComponentIdentifier consumer = DSPMessagesFactory.INSTANCE.makeDSPComponentIdentifier(
        		nodeComponentId, compInfo[0], compInfo[1]);     
        
        Header header = DSPMessagesFactory.INSTANCE.makeDSPMessageHeader(null, producer, consumer);
            
    	Message message = null;
    	if(QueryMessage.class.equals(messageClass)){
    		message = DSPMessagesFactory.INSTANCE.makeDSPQueryMessage(header, content);
    	}else if (UpdateMessage.class.equals(messageClass)){
    		message = DSPMessagesFactory.INSTANCE.makeDSPUpdateMessage(header, content);
    	}else{
    		log.warn("Message type unknown: " + messageClass.getName());
    	}
    	return message;
	}
	
	private String[] getNodeAddressAsString(String nodeComponentId) 
	{
		return regDspComps.get(nodeComponentId);
	}

	private void processCreateMessage(Message message) {
		DSProperties props = (DSProperties)message.getBody().getAny();
		String dspComp = null;
		String[] compInfo = new String[2];
		for(DSProperty prop: props.getProperty()){
			if(prop.getName().equals("DSPCOMPONENT")){
				dspComp = prop.getValue();
			}else if(prop.getName().equals("IP")){
				compInfo[0] = prop.getValue();
			}else if(prop.getName().equals("TYPE")){
				compInfo[1] = prop.getValue();
			}
		}
		regDspComps.put(dspComp, compInfo);
	}
	
}
