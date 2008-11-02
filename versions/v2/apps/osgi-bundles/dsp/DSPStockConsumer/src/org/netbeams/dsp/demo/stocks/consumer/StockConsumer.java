package org.netbeams.dsp.demo.stocks.consumer;



import java.util.UUID;

import org.netbeams.dsp.ComponentDescriptor;
import org.netbeams.dsp.ComponentLocator;
import org.netbeams.dsp.DSPComponent;
import org.netbeams.dsp.DSPContext;
import org.netbeams.dsp.DSPException;
import org.netbeams.dsp.demo.stocks.data.StockTick;
import org.netbeams.dsp.demo.stocks.data.StockTickData;
import org.netbeams.dsp.message.MeasurementMessage;
import org.netbeams.dsp.message.Message;
import org.netbeams.dsp.util.Log;

public class StockConsumer implements DSPComponent{
	
	// Component Type
	public final static String COMPONENT_TYPE = "org.netbeams.dsp.demo.stocks.consumer";
	
	private static ComponentDescriptor componentDescriptor;

	
	private UUID uuid;
	
	private DSPContext context;
	private ComponentLocator locator;
	private ComponentDescriptor descriptor;

	/////////////////////////////////////////////
	////////// DSP Component Interface //////////
	/////////////////////////////////////////////


	@Override
	public UUID getUUID() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getComponentType() {
		return COMPONENT_TYPE;
	}

	@Override
	public void initComponent(UUID uuid, DSPContext context) throws DSPException {
		Log.log("StockConsumer.initComponent()");	
		this.context = context;
		this.uuid = uuid;
	}

	@Override
	public ComponentDescriptor getComponentDescriptor() {
		return componentDescriptor;
	}


	@Override
	public void startComponent() {
		Log.log("StockConsumer.startComponent()");
	}
	
	@Override
	public void stopComponent() {
		Log.log("StockConsumer.stopComponent()");
	}

	@Override
	public void deliver(Message message) throws DSPException {
		Log.log("StockConsumer.deliver()");
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
		Log.log("message.class=" + message.getClass().getName());
		if(message instanceof MeasurementMessage){
			StockTickData data = (StockTickData)message.getContent();
			for(StockTick tick: data.getTicks()){
				Log.log("Symble:" + tick.getStockSymbol() + "  " + "Value:" + tick.getValue());
			}
		}
		
	}

}
