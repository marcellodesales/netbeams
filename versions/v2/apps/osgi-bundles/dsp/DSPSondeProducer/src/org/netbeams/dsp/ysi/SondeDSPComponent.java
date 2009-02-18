package org.netbeams.dsp.ysi;

import java.util.ArrayList;
import java.util.Collection;
import org.apache.log4j.Logger;
import org.netbeams.dsp.ComponentDescriptor;
import org.netbeams.dsp.DSPComponent;
import org.netbeams.dsp.DSPContext;
import org.netbeams.dsp.DSPException;
import org.netbeams.dsp.message.Message;
import org.netbeams.dsp.MessageCategory;

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
    
    
 // Implemented methods of the DSPComponent interface.
    
    
    public void initComponent(String componentNodeId, DSPContext context) throws DSPException {
    	this.context = context;
		this.componentNodeId = componentNodeId;
		log.info("Initializing Sonde DSP component: " + componentNodeId);
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
		// Not in use
	}
	

	public Message deliverWithReply(Message message) throws DSPException {
		// Not in use
		return null;
	}

	
	public Message deliverWithReply(Message message, long waitTime) throws DSPException {
		// Not in use
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
