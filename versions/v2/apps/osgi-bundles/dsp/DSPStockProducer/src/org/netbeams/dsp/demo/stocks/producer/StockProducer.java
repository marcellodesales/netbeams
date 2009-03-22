package org.netbeams.dsp.demo.stocks.producer;



import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;
import org.netbeams.dsp.ComponentDescriptor;
import org.netbeams.dsp.DSPComponent;
import org.netbeams.dsp.DSPContext;
import org.netbeams.dsp.DSPException;
import org.netbeams.dsp.MessageBrokerAccessor;
import org.netbeams.dsp.MessageCategory;
import org.netbeams.dsp.MessageFactory;
import org.netbeams.dsp.data.property.DSProperties;
import org.netbeams.dsp.data.property.DSProperty;
import org.netbeams.dsp.demo.stock.StockTick;
import org.netbeams.dsp.demo.stock.StockTicks;
import org.netbeams.dsp.message.AcknowledgementMessage;
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


public class StockProducer implements DSPComponent{
	
	private static final Logger log = Logger.getLogger(StockProducer.class);
	
	private static final ComponentDescriptor componentDescriptor;
	
	// Component Type
	public final static String COMPONENT_TYPE = "org.netbeams.dsp.demo.stocks.producer";
	
	// Message Content Type
	public final static String MSG_CONTENT_TYPE_STOCK_TICK = "stock.tick";
	
	private DSPContext context;
	
	private String componentNodeId;
	
	// Last measurement. This is used for pull requests
	private List<StockTick> lastMeasurements;
	
	// Main thread
	private Engine engine;
	
		
	/////////////////////////////////////////////
	////////// DSP Component Interface //////////
	/////////////////////////////////////////////
	
	/**
	 * @Override
	 */
	public String getComponentType() {
		return COMPONENT_TYPE;
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
	public void initComponent(String componentNodeId, DSPContext context) throws DSPException {
		log.info("Initializing component: " + componentNodeId);
		
		this.context = context;
		this.componentNodeId = componentNodeId;
	}


	/**
	 * @Override
	 */
	public String getComponentNodeId() {
		return componentNodeId;
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
	public Message deliverWithReply(Message message) throws DSPException {
		log.debug("Delivering message with reply.");
		return null;
	}
	
	/**
	 * @Override
	 */
	public Message deliverWithReply(Message message, long waitTime) throws DSPException {
		log.debug("Delivering message with reply w/wait.");
		return null;
	}


	/**
	 * @Override
	 */
	public void startComponent() {
		log.info("Starting component");
//		engine = new Engine("[" + componentNodeId + "] " + COMPONENT_TYPE);
//		engine.start();
		
	}
	
	/**
	 * @Override
	 */
	public void stopComponent() {
		log.info("Stopping component");
		
		if(engine != null){
			engine.stopEngine();
			engine = null;
		}	
	}

	
	private void send(StockTicks data) throws DSPException{
		//Create the message
    	    DSProperties properties = new DSProperties();
    	    for(StockTick tk : data.getStockTick()) {
    	        DSProperty p = new DSProperty();
    	        p.setName(tk.getSymbol());
    	        p.setValue(tk.getValue()+"");
    	        properties.getProperty().add(p);
    	    }
	    
		Message message = MessageFactory.newMessage(MeasureMessage.class, properties, this);
		log.debug("Send stock ticks");
				
		if(data == null){
			log.debug("No stock ticks generated");
		}else{
			if(data.getStockTick() == null){
				log.debug("No stock ticks generated");
			}else{
				log.debug("Tick Collection size is " + data.getStockTick().size());
			}
		}
		// Always check if there is a broker available
		MessageBrokerAccessor messageBroker = context.getDataBroker();
		if(messageBroker != null){
			messageBroker.send(message);
		}else{
			log.debug("Message broker not available");
		}
	}
	
	/////////////////////////////////////
	////////// Private Section //////////
	/////////////////////////////////////
	
	// TODO: I think we can use annotations to describe the message types
	static{
		componentDescriptor = new ComponentDescriptor();
		
		Collection<MessageCategory> producedMessageCategories = new ArrayList<MessageCategory>();
		Collection<MessageCategory> consumedMessageCategories = new ArrayList<MessageCategory>();
		// StockTick
		MessageCategory messageCategory = 
			new MessageCategory(StockProducer.class.getName(), MSG_CONTENT_TYPE_STOCK_TICK);
		
		producedMessageCategories.add(messageCategory);
		componentDescriptor.setMsgCategoryProduced(producedMessageCategories);
		componentDescriptor.setMsgCategoryConsumed(consumedMessageCategories);
		
	}
	
	private void processMessage(Message message) {
		log.debug("message class=" + message.getClass().getName());
		
		if(message instanceof UpdateMessage){		
			processUpdate(message);
		}else if(message instanceof QueryMessage){	
			processQuery(message);			
		}
	}
	
	/**
	 * JAXBImplementation
	 * 
	private void processQuery(Message message) {
		DSProperties dspProperties = null;
		try{
			Object content = message.getBody().getAny();
			log.debug("Content class " + content.getClass().getName());
			
			JAXBContext jc = JAXBContext.newInstance("org.netbeams.dsp.data.property",
					org.netbeams.dsp.demo.stock.StockTicks.class.getClassLoader());
			Unmarshaller unmarshaller = jc.createUnmarshaller();
			dspProperties = (DSProperties)unmarshaller.unmarshal((Node)content);
		}catch(JAXBException e){
			log.error("Error unmarhalling the message", e);
			return;
		}
		log.debug("Got query configuration");
		
		sendReply(message);		
	}
	*/

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
		freq.setName("samplingFrequency");
		freq.setValue("10");
		props.getProperty().add(freq);
		
		// Obtain original producer
		ComponentIdentifier origProducer = message.getHeader().getProducer();
		String originalMessageId = message.getMessageID();
		// Create reply  message
		
		String localIPAddress = NetworkUtil.getCurrentEnvironmentNetworkIp();
        ComponentIdentifier producer = DSPMessagesFactory.INSTANCE.makeDSPComponentIdentifier(
        		getComponentNodeId(), localIPAddress, props.getContentContextForJAXB());
		       
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

	/////////////////////////////////
	////////// Inner Class //////////
	/////////////////////////////////
	
	
	private class Engine extends Thread{
		
	    private Random random; 
	    // Flag used to stop the Thread running the Engine.
	    boolean shouldStop;
	    
	    private Engine(String name){
	    	super(name);
	    	shouldStop = false;
	    	random = new Random(); 
	    }

		@Override
		public void run() {
			while(true){
				if(isInterrupted()){
					break;
				}
				if(shouldStop){
					break;
				}
				gatherStockTickers();
				pushMeasurements();
				
				try {
					Thread.sleep(15000 * 1000);
				} catch (InterruptedException e) {
					break;
				}
			}	
			log.debug("Producer is stopping...");
		}
		
		private void stopEngine(){
			shouldStop = true;
			interrupt();
		}

		private void pushMeasurements() {
			StockTicks data = generateStockTickData(StockProducer.this.lastMeasurements);
			// TODO: Test Only
			try {
				send(data);
			} catch (DSPException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
			
		}

		private StockTicks generateStockTickData(List<StockTick> lastMeasurements) {
			StockTicks result = new StockTicks();
			for(StockTick tick: lastMeasurements){
				result.getStockTick().add(tick);
			}
			return result;
		}

		private void gatherStockTickers() {
			
			log.debug("Gathering stock ticks...");
			
			// Google
			StockTick google = new StockTick();
//			google.name = "Google";
			google.setSymbol("GOOG");
			google.setValue(400 +  100 * random.nextFloat()); 	
			// Google
			StockTick ms = new StockTick();
//			ms.name = "Micrisoft";
			ms.setSymbol("MSFT");
			ms.setValue(25 +  10 * random.nextFloat()); 	

			// Yahoo
			StockTick y = new StockTick();
//			y.name = "Yahoo";
			y.setSymbol("YHOO");
			y.setValue(10 +  10 * random.nextFloat()); 	
			
		    List<StockTick> tickers = new ArrayList<StockTick>();
		    tickers.add(google);
		    tickers.add(ms);
		    tickers.add(y);
		    			
		    StockProducer.this.lastMeasurements = tickers;

		}

	}

}
