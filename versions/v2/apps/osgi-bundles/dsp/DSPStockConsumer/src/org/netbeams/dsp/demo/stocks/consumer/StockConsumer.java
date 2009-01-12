package org.netbeams.dsp.demo.stocks.consumer;


import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.netbeams.dsp.ComponentDescriptor;
import org.netbeams.dsp.message.ComponentLocator;
import org.netbeams.dsp.DSPComponent;
import org.netbeams.dsp.DSPContext;
import org.netbeams.dsp.DSPException;
import org.netbeams.dsp.demo.stock.StockTick;
import org.netbeams.dsp.demo.stock.StockTicks;
import org.netbeams.dsp.message.MeasureMessage;
import org.netbeams.dsp.message.Message;
import org.w3c.dom.Document;
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


	@Override
	public String getComponentNodeId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getComponentType() {
		return COMPONENT_TYPE;
	}

	@Override
	public void initComponent(String componentNodeId, DSPContext context) throws DSPException {
		log.info("initComponent()");
		
		this.context = context;
		this.componentNodeId = componentNodeId;
	}

	@Override
	public ComponentDescriptor getComponentDescriptor() {
		return componentDescriptor;
	}


	@Override
	public void startComponent() {
		log.info("startComponent()");
	}
	
	@Override
	public void stopComponent() {
		log.info("stopComponent()");
	}

	@Override
	public void deliver(Message message) throws DSPException {
		log.debug("deliver(Message)");
		processMessage(message);
	}

	@Override
	public Message deliverWithReply(Message request)
			throws DSPException {
		// TODO How we should handle an invokation to this method when the component is not a consumer?
		return null;
	}
	
	@Override
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
		
		if(message instanceof MeasureMessage){
			
			StockTicks stockTicks = null;
			Object content = message.getBody().getAny();
			if(Message.isPojo(content)){
				log.debug("StockConsumer.processMessage(): Message Received....");
				return;
				
//				if(content instanceof StockTicks){
//					stockTicks = (StockTicks)content;
//				}else{
//					Log.log("StockConsumer.processMessage(): Invalid content type " + content.getClass().getName());
//				}
			}else{
				try{
					JAXBContext jc = JAXBContext.newInstance("org.netbeams.dsp.demo.stock",
							org.netbeams.dsp.demo.stock.ObjectFactory.class.getClassLoader());
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

}
