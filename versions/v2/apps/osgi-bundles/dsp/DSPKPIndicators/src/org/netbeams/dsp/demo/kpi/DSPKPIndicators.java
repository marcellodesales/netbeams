package org.netbeams.dsp.demo.kpi;


import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;
import org.netbeams.dsp.ComponentDescriptor;
import org.netbeams.dsp.message.ComponentLocator;
import org.netbeams.dsp.DSPComponent;
import org.netbeams.dsp.DSPContext;
import org.netbeams.dsp.DSPException;

import org.netbeams.dsp.message.MeasureMessage;
import org.netbeams.dsp.message.Message;

import org.netbeams.dsp.weblogger.Buffer;


import org.w3c.dom.Node;
import org.netbeams.dsp.demo.stock.StockTick;
import org.netbeams.dsp.demo.stock.StockTicks;
import org.netbeams.dsp.demo.mouseactions.MouseAction;
import org.netbeams.dsp.demo.mouseactions.MouseActionsContainer;


public class DSPKPIndicators implements DSPComponent{
	
	
	private static final Logger log = Logger.getLogger(DSPKPIndicators.class);
	
	// Component Type
	public final static String COMPONENT_TYPE = "org.netbeams.dsp.demo.kpi";
	
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
		log.debug("deliver(Message)");
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


	private void processMessage(Message dspMsg) {
        try {  
        	StockTicks ticks = null;
        	log.debug("ticks"+ticks);
        	MouseActionsContainer mouseacts = null;
        	log.debug("mouseacts"+mouseacts);
            Object obj = dspMsg.getBody().getAny();
            log.debug("obj"+ obj);
            
           if (dspMsg.getContentType().equals("org.netbeams.dsp.demo.stock")) {
        	   JAXBContext jc = JAXBContext.newInstance("org.netbeams.dsp.demo.stock",
						StockTicks.class.getClassLoader());
				Unmarshaller unmarshaller = jc.createUnmarshaller();
				ticks = (StockTicks)unmarshaller.unmarshal((Node)obj);
   
                for (StockTick sttk : ticks.getStockTick()) {
                	if (sttk.getValue()>30){
                	log.info("Value:" + sttk.getValue()+ "Great!");
                	}
                }
            } else 
            if (dspMsg.getContentType().equals("org.netbeams.dsp.demo.mouseactions")) {
            	
         	   JAXBContext jc = JAXBContext.newInstance("org.netbeams.dsp.demo.mouseactions",
         			  MouseActionsContainer.class.getClassLoader());
				Unmarshaller unmarshaller = jc.createUnmarshaller();
				mouseacts = (MouseActionsContainer)unmarshaller.unmarshal((Node)obj);
            	
                for (MouseAction sttk : mouseacts.getMouseAction()) {
                	if (sttk.getX()>15){
                	log.info("Value:" + sttk.getX()+"Great!");
                	}
                }
            }                
 
        
    } catch (JAXBException e) {
		log.error("Error unmarhalling the message", e);
		return;
    }
	}

}
