package org.netbeams.dsp.demo.stocks.producer;



import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;
import org.netbeams.dsp.ComponentDescriptor;
import org.netbeams.dsp.DSPComponent;
import org.netbeams.dsp.DSPContext;
import org.netbeams.dsp.DSPException;
import org.netbeams.dsp.MessageBrokerAccessor;
import org.netbeams.dsp.MessageCategory;
import org.netbeams.dsp.MessageFactory;
import org.netbeams.dsp.demo.stock.StockTick;
import org.netbeams.dsp.demo.stock.StockTicks;
import org.netbeams.dsp.message.MeasureMessage;
import org.netbeams.dsp.message.Message;


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
	public void deliver(Message request) throws DSPException {
		log.debug("Delivering message.");
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
		Message message = MessageFactory.newMessage(MeasureMessage.class, data, this);
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
					Thread.sleep(15 * 1000);
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
