package org.netbeams.dsp.demo.stocks.producer;



import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.netbeams.dsp.ComponentDescriptor;
import org.netbeams.dsp.DSPComponent;
import org.netbeams.dsp.DSPContext;
import org.netbeams.dsp.DSPException;
import org.netbeams.dsp.MessageBrokerAccessor;
import org.netbeams.dsp.demo.stocks.data.StockCT;
import org.netbeams.dsp.demo.stocks.data.StockTick;
import org.netbeams.dsp.demo.stocks.data.StockTickData;
import org.netbeams.dsp.message.MeasurementMessage;
import org.netbeams.dsp.message.Message;
import org.netbeams.dsp.message.MessageCategory;
import org.netbeams.dsp.message.MessageFactory;
import org.netbeams.dsp.util.Log;



public class StockProducer implements DSPComponent{
	
	private static final ComponentDescriptor componentDescriptor;
	
	// Component Type
	public final static String COMPONENT_TYPE = "org.dsp.test.stockproducer";
	
	private DSPContext context;
	
	private UUID uuid;
	
	// Last measurement. This is used for pull requests
	private List<StockTick> lastMeasurements;
	
	// Main thread
	private Engine engine;
	
		
	/////////////////////////////////////////////
	////////// DSP Component Interface //////////
	/////////////////////////////////////////////
	
	@Override
	public String getComponentType() {
		return COMPONENT_TYPE;
	}

	@Override
	public ComponentDescriptor getComponentDescriptor() {
		return componentDescriptor;
	}

	@Override
	public void initComponent(UUID uuid, DSPContext context) throws DSPException {
		Log.log("StockProducer.initComponent()");
		
		this.context = context;
		this.uuid = uuid;
	}


	@Override
	public UUID getUUID() {
		return uuid;
	}

	@Override
	public void deliver(Message request) throws DSPException {
		// TODO How we should handle an invokation to this method when the component is not a consumer?
	}

	@Override
	public Message deliverWithReply(Message message)
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


	@Override
	public void startComponent() {
		Log.log("StockProducer.startComponent()");
		engine = new Engine("[" + uuid + "] " + COMPONENT_TYPE);
		engine.start();
		
	}
	
	@Override
	public void stopComponent() {
		Log.log("StockProducer.stopComponent()");
		
		if(engine != null){
			engine.stopEngine();
			engine = null;
		}	
	}

	
	private void send(StockTickData data) throws DSPException{
		//Create the message
		Message message = MessageFactory.newMessage(MeasurementMessage.class, data, this);
		// TODO: Test only
		Log.log("StockProducer.send()");
		StockTickData d = (StockTickData)message.getContent();
		if(d == null){
			Log.log("StockTickData is null");
		}else{
			if(d.getTicks() == null){
				Log.log("Tick Collection is null");
			}else{
				Log.log("Tick Collection size is " + d.getTicks().size());
			}
		}
		
		// Always check if there is a broker available
		MessageBrokerAccessor messageBroker = context.getDataBroker();
		if(messageBroker != null){
			messageBroker.send(message);
		}else{
			Log.log("StockProducer.push: Message Broker not available");
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
			new MessageCategory(StockProducer.class.getName(), StockCT.STOCK_TICK);
		
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
			Log.log("Stock Producer is stopping...");
		}
		
		private void stopEngine(){
			shouldStop = true;
			interrupt();
		}

		private void pushMeasurements() {
			StockTickData data = generateStockTickData(StockProducer.this.lastMeasurements);
			// TODO: Test Only
			try {
				send(data);
			} catch (DSPException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
			
		}

		private StockTickData generateStockTickData(List<StockTick> lastMeasurements) {
			StockTickData result = new StockTickData();
			result.setTicks(lastMeasurements);
			return result;
		}

		private void gatherStockTickers() {
			
			Log.log("StockProducer.gatherStockTickers()");
			
			// Google
			StockTick google = new StockTick();
//			google.name = "Google";
			google.setStockSymbol("GOOG");
			google.setValue(400 +  100 * random.nextFloat()); 	
			// Google
			StockTick ms = new StockTick();
//			ms.name = "Micrisoft";
			ms.setStockSymbol("MSFT");
			ms.setValue(25 +  10 * random.nextFloat()); 	

			// Yahoo
			StockTick y = new StockTick();
//			y.name = "Yahoo";
			y.setStockSymbol("YHOO");
			y.setValue(10 +  10 * random.nextFloat()); 	
			
		    List<StockTick> tickers = new ArrayList<StockTick>();
		    tickers.add(google);
		    tickers.add(ms);
		    tickers.add(y);
		    			
		    StockProducer.this.lastMeasurements = tickers;

		}

	}



}
