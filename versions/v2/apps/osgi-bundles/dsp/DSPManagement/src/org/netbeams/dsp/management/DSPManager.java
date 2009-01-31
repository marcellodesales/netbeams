
package org.netbeams.dsp.management;



import java.util.Collection;
import java.util.Map;

import org.apache.log4j.Logger;
import org.netbeams.dsp.ComponentDescriptor;
import org.netbeams.dsp.data.property.DSProperties;
import org.netbeams.dsp.message.AcknowledgementMessage;
import org.netbeams.dsp.message.ComponentIdentifier;
import org.netbeams.dsp.message.ComponentLocator;
import org.netbeams.dsp.message.Header;
import org.netbeams.dsp.message.NodeAddress;
import org.netbeams.dsp.message.UpdateMessage;
import org.netbeams.dsp.DSPComponent;
import org.netbeams.dsp.DSPContext;
import org.netbeams.dsp.DSPException;
import org.netbeams.dsp.MessageBrokerAccessor;
import org.netbeams.dsp.MessageFactory;
import org.netbeams.dsp.message.Message;
import org.netbeams.dsp.util.DSPUtils;


public class DSPManager implements DSPComponent
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
    
    private Map<String, Message> pendingAck;
    
    public DSPManager(){
    	isActive = false;
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
        log.debug("deliver message.");
        processMessage(message);
    }

	//@Override
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

    public boolean isActive(){
    	return isActive;
    }

    public void updateProperties(NodeAddress nodeAddress, String compNodeId, DSProperties dsProps) 
		throws DSPException{
		ComponentIdentifier compIden = DSPUtils.obtainIdentifier(null, compNodeId, nodeAddress);
		Message message = MessageFactory.newMessage(UpdateMessage.class, dsProps, this);
		message.setComponentIdentifier(compIden);
		Header h = null;
		accessor.send(message);
		
		pendingAck.put(message.getMessageID(), message);
	}    
    
    public Collection<Message> getPendingAck(){
    	return pendingAck.values();
    }
    
    /////////////////////////////////////
    ////////// Private Section //////////
    /////////////////////////////////////
    


    private void processMessage(Message message) {
		if(message instanceof AcknowledgementMessage){
			processAcknowledge(message);
		}	
	}

	private void processAcknowledge(Message message) {
		// Obtain correlation id
		Header header = message.getHeader();
		String corrId = (String)header.getCorrelationID();
		Message msg = pendingAck.remove(corrId);
		if(msg != null){
			log.warn("No pending message for acknowledge " + corrId);
		}	
	}

}
