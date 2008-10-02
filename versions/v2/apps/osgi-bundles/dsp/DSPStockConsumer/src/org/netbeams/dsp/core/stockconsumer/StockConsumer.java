package org.netbeams.dsp.core.stockconsumer;


import org.jdom.Element;
import org.jdom.Namespace;
import org.netbeams.dsp.ComponentDescriptor;
import org.netbeams.dsp.ComponentLocator;
import org.netbeams.dsp.DMPComponent;
import org.netbeams.dsp.DMPContext;
import org.netbeams.dsp.DMPException;
import org.netbeams.dsp.Message;
import org.netbeams.dsp.MessageConsumer;
import org.netbeams.dsp.MessageType;
import org.netbeams.dsp.MessageTypes;
import org.netbeams.dsp.util.Log;

public class StockConsumer implements DMPComponent, MessageConsumer{
	
	public final static String COMPONENT_TYPE = "org.csc899.stock.consumer";

	// TODO: I think we can use annotations to describe the message types
	private static MessageTypes consumedMessageTypes;
	static{
		consumedMessageTypes = new MessageTypes();
		MessageType msgType = new MessageType(Message.Category.MEASUREMENT, StockConstants.MEASUREMENT_TYPE_STOCK_TICK);
		consumedMessageTypes.addType(msgType);
	}
	
	private DMPContext context;
	private ComponentLocator locator;
	private ComponentDescriptor descriptor;


	@Override 
	public String getType() {
		return COMPONENT_TYPE;
	}

	@Override
	public void initComponent(ComponentLocator locator, DMPContext context) throws DMPException {
		Log.log("StockConsumer.initComponent()");
		this.locator = locator;
		this.context = context;
		createDescriptor();
	}

	@Override
	public ComponentDescriptor getDescriptor() {
		return descriptor;
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
	public ComponentLocator getLocator() {
		return locator;
	}


	@Override
	public void push(Message data) throws DMPException {
		Log.log("StockConsumer.push()");
		
		Element stockTicks = data.getBodyContent();
		Namespace ns = stockTicks.getNamespace();
		for(Object o: stockTicks.getChildren()){
			Element tick = (Element)o;
			Element name = tick.getChild(XMLConstants.ELEMENT_NAME, ns);
			Log.log("StockConsumer.push(): " + name.getTextTrim());
		}		
	}

	@Override
	public Message messageDelivered(Message request) throws DMPException {
		// TODO Auto-generated method stub
		return null;
	}
	
	private void createDescriptor() {
		descriptor = new ComponentDescriptor();
		descriptor.setType(COMPONENT_TYPE);
		descriptor.setMsgTypesConsummed(consumedMessageTypes);	
	}

	@Override
	public Message messageDeliveredWithReply(Message message)
			throws DMPException {
		// TODO Auto-generated method stub
		return null;
	}



}
