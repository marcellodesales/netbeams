
package org.netbeams.dsp.management;



import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
import org.netbeams.dsp.message.AbstractMessage;
import org.netbeams.dsp.message.AcknowledgementMessage;
import org.netbeams.dsp.message.Body;
import org.netbeams.dsp.message.ComponentIdentifier;
import org.netbeams.dsp.message.ComponentLocator;
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
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.sun.corba.se.pept.broker.Broker;


public class DSPManager implements Manager, DSPComponent
{
	private static final Logger log = Logger.getLogger(DSPManager.class);
			
    // Component Type
    public final static String         COMPONENT_TYPE = "org.netbeams.dsp.management";

    private static ComponentDescriptor componentDescriptor;
    
    private boolean isActive;

    private String                     componentNodeId;

    private DSPContext                 context;
    private ComponentLocator           locator;
    private ComponentDescriptor        descriptor;
    
    private MessageBrokerAccessor accessor;
    
    private Map<String, Boolean> pendingReply;
    private Map<String, Message> repliedMsgs;
    
    public DSPManager(){
    	isActive = false;
    	pendingReply = new HashMap<String, Boolean>();
    	repliedMsgs = new HashMap<String, Message>();
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
        log.info("Initing component...");
        accessor = context.getDataBroker();
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

		Message msg = MessageFactory.newMessage2(QueryMessage.class, content, this, nodeComponentId, componentType, nodeAddress);
		
		registerPendingMessage(msg.getMessageID());
		accessor.send(msg);

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
		accessor.send(msg);

		return msg.getMessageID();
	}

	/**
     *  
     * @Override
     */	
	public  Document retrieveInteractionReply(String interactionId){
		Message msg = repliedMsgs.get(interactionId);
		if(msg !=  null){
			return (Document)msg.getBody().getAny();
		}
		return null;
	}
    
  
    /////////////////////////////////////
    ////////// Private Section //////////
    /////////////////////////////////////
    
	
    private void registerPendingMessage(String messageId) {
    	pendingReply.put(messageId, Boolean.TRUE);
    }
	
	private String messageSummary(Message message) {
		StringBuilder b = new StringBuilder();
		ComponentIdentifier ciConsumer =  message.getHeader().getConsumer();
		b.append("producer ").append(ciConsumer.getComponentLocator().getComponentNodeId());
		b.append(" node ").append(ciConsumer.getComponentLocator().getNodeAddress().getValue());
		b.append(" data type ").append(message.getContentType());
		return b.toString();
	}

    private void processMessage(Message message) {
		// Obtain correlation id
		Header header = message.getHeader();
		String corrId = (String)header.getCorrelationID();
		if(corrId != null){
			Boolean b = pendingReply.remove(corrId);
			
			log.debug("Replied message " + message.getMessageID());
			
			if(b != null){
				repliedMsgs.put(corrId, message);
			}
		}else{
			log.debug("No correlation id. Message content class:" + message.getContentType());
		}
	}
}
