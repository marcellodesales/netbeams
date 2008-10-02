package org.netbeams.dsp.core.stockproducer;


import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.jdom.Element;
import org.netbeams.dsp.ComponentDescriptor;
import org.netbeams.dsp.ComponentLocator;
import org.netbeams.dsp.DMPComponent;
import org.netbeams.dsp.DMPContext;
import org.netbeams.dsp.DMPException;
import org.netbeams.dsp.Message;
import org.netbeams.dsp.MessageFactory;
import org.netbeams.dsp.MessageProducer;
import org.netbeams.dsp.MessageType;
import org.netbeams.dsp.MessageTypes;
import org.netbeams.dsp.broker.MessageBrokerAccessor;
import org.netbeams.dsp.util.Log;


public class StockProducer implements DMPComponent, MessageProducer{

	public final static String COMPONENT_TYPE = "org.csc899.stock.producer";
	
	public final static String MEASUREMENT_TYPE_STOCK_TICK = "stock_tick";
	public final static String PRODUCED_DATA_TYPE_CONFIGURATION = "any";
	public final static String PRODUCED_DATA_TYPE_ACTION = "TDB";
	public final static String PRODUCED_DATA_TYPE_STATE = "TBD";

	// TODO: I think we can use annotations to describe the message types
	private static MessageTypes producedMessageTypes;
	static{
		producedMessageTypes = new MessageTypes();
		MessageType msgType = new MessageType(Message.Category.MEASUREMENT, MEASUREMENT_TYPE_STOCK_TICK);
		producedMessageTypes.addType(msgType);
	}

	private DMPContext context;
	
	private ComponentLocator locator;
	private ComponentDescriptor descriptor;
	
	// Last measurement. This is used for pull requests
	private List<StockTicker> lastMeasuments;
	// JDOM Helper
	private StockJDOMHelper jdomHelper;
	
	// Main thread
	private Engine engine;
	
	@Override
	public ComponentLocator getLocator() {
		return locator;
	}
	
	@Override
	public String getType() {
		return COMPONENT_TYPE;
	}

	@Override
	public ComponentDescriptor getDescriptor() {
		return descriptor;
	}

	@Override
	public void initComponent(ComponentLocator locator, DMPContext context) throws DMPException {
		Log.log("StockProducer.initComponent()");
		
		this.locator = locator;
		this.context = context;
		createDescriptor();
		
		jdomHelper = new StockJDOMHelper();
	}


	@Override
	public void startComponent() {
		Log.log("StockProducer.startComponent()");
		engine = new Engine("[" + getLocator().getUUID() + "] " + COMPONENT_TYPE);
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


	@Override
	public Message messageDelivered(Message request) throws DMPException {
		// TODO Auto-generated method stub
		return null;
	}
	
	private void push(Element element) throws DMPException{
		//Create the message
		Message message = MessageFactory.newMessage(this, Message.Category.MEASUREMENT, MEASUREMENT_TYPE_STOCK_TICK);
		message.setBodyContent(element);
		// TODO: Test only
		Log.log("StockProducer.push()");
		Log.log(message.getBodyContent());
		
		// Always check if there is a broker available
		MessageBrokerAccessor messageBroker = context.getDataBroker();
		if(messageBroker != null){
			messageBroker.push(message);
		}else{
			Log.log("StockProducer.push: Message Broker not available");
		}
	}
	
	private void createDescriptor() {
		descriptor = new ComponentDescriptor();
		descriptor.setType(COMPONENT_TYPE);
		descriptor.setMsgTypesProduced(producedMessageTypes);	
	}

	///////////////////
	/// Inner Class ///
	///////////////////
	
	
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
			Element tickers =  
				StockProducer.this.jdomHelper.generateStockTicks(StockProducer.this.lastMeasuments);
			// TODO: Test Only
			Log.log(tickers);
			try {
				push(tickers);
			} catch (DMPException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
			
		}

		private void gatherStockTickers() {
			
			Log.log("StockProducer.gatherStockTickers()");
			
			// Google
			StockTicker google = new StockTicker();
			google.name = "Google";
			google.symbol = "GOOG";
			google.value = 400 +  100 * random.nextDouble(); 	
			// Google
			StockTicker ms = new StockTicker();
			ms.name = "Micrisoft";
			ms.symbol = "MSFT";
			ms.value = 25 +  10 * random.nextDouble(); 			
			// Google
			StockTicker y = new StockTicker();
			y.name = "Yahoo";
			y.symbol = "YHOO";
			y.value = 10 +  10 * random.nextDouble(); 	
			
		    List<StockTicker> tickers = new ArrayList<StockTicker>();
		    tickers.add(google);
		    tickers.add(ms);
		    tickers.add(y);
		    			
			StockProducer.this.lastMeasuments = tickers;

		}

	}

	@Override
	public Message messageDeliveredWithReply(Message message)
			throws DMPException {
		// TODO Auto-generated method stub
		return null;
	}

}
