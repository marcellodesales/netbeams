package org.netbeams.dsp.test.rand;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.SecureRandom;

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
import org.netbeams.dsp.demo.stock.StockTick;
import org.netbeams.dsp.demo.stock.StockTicks;
import org.netbeams.dsp.message.MeasureMessage;
import org.netbeams.dsp.message.Message;
import org.netbeams.dsp.message.UpdateMessage;
import org.w3c.dom.Node;


public class RandomProducer implements DSPComponent {
	
	private static final Logger log = Logger.getLogger(RandomProducer.class);	
	private static final ComponentDescriptor componentDescriptor;
	public static final String COMPONENT_TYPE = "org.netbeams.dsp.test.rand";
	public static final String MSG_CONTENT_TYPE_RANDOM_FLOAT = "random.float";
    
	private DSPContext context;
    private String componentNodeId;
    private List <RandomNumber> dataList;
    private RandomNumberGenerator rngThread;
        
    public RandomProducer() {};
    
    static{
		componentDescriptor = new ComponentDescriptor();
		
		Collection<MessageCategory> producedMessageCategories = new ArrayList<MessageCategory>();
		Collection<MessageCategory> consumedMessageCategories = new ArrayList<MessageCategory>();
		// StockTick
		MessageCategory messageCategory = 
			new MessageCategory(RandomProducer.class.getName(), MSG_CONTENT_TYPE_RANDOM_FLOAT);
		
		producedMessageCategories.add(messageCategory);
		componentDescriptor.setMsgCategoryProduced(producedMessageCategories);
		componentDescriptor.setMsgCategoryConsumed(consumedMessageCategories);
		
	}  
    
    
 // Implemented methods of the DSPComponent interface.
    
    public void deliver(Message message) throws DSPException {
    	log.debug("Delivering message.");
	}

	public Message deliverWithReply(Message message) throws DSPException {	
		log.debug("Delivering message with reply.");
		return null;
	}

	public Message deliverWithReply(Message message, long waitTime) throws DSPException {
		log.debug("Delivering message with reply w/wait.");
		return null;
	}

	public ComponentDescriptor getComponentDescriptor() {		
		return componentDescriptor;	
	}

	public void startComponent() throws DSPException {
		log.info("Starting component");
		rngThread = new RandomNumberGenerator();
		rngThread.start();
	}

	public void stopComponent() throws DSPException {
		log.info("Stopping component");
		
		if (rngThread != null) {
			rngThread.stopGenerator();
			rngThread = null;
		}
	}

	public String getComponentNodeId() {
		return componentNodeId;
	}

	public String getComponentType() {		
		return COMPONENT_TYPE;		
	}

	public void initComponent(String componentNodeId, DSPContext context) throws DSPException {
		log.info("Initializing component: " + componentNodeId);
		
		this.context = context;
		this.componentNodeId = componentNodeId;
        
	}
	
	
	private void send(RandomNumbers rNums) throws DSPException{
		
		//Create the message
		Message message = MessageFactory.newMessage(MeasureMessage.class, rNums, this);
		log.debug("Send random numbers");
				
		if(rNums == null){
			log.debug("No random numbers generated");
		}else{
			if(rNums.getRandomNumber() == null){
				log.debug("No random numbers generated");
			}else{
				log.debug("Random number size is " + rNums.getRandomNumber().size());
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
	
	private class RandomNumberGenerator extends Thread{
		
		private SecureRandom random;
		private boolean running = true;
	    private byte bytes[];
	    private float fData;
		
	    private RandomNumberGenerator() {
	    	random = new SecureRandom();
	    	bytes = new byte[20];
	    }
	    
	    private RandomNumbers generateRandomNumbers(List<RandomNumber> dataList) {
	    	RandomNumbers result = new RandomNumbers();
			for(RandomNumber num: dataList){
				result.getRandomNumber().add(num);
			}
			return result;
		}
	    
	    
	    
	    private void getRandomNumber() {		
			ByteArrayInputStream byte_in = new ByteArrayInputStream (bytes);
			DataInputStream data_in = new DataInputStream (byte_in);
			try {
				fData = data_in.readFloat();
			} catch (Exception e){
				System.err.println("ERROR: " + e.getMessage());
			}
		}
    	
    	public void run() {
    		while (running) {			
    			random.nextBytes(bytes);
    			getRandomNumber();
    			sendData();
    			//System.out.println("Random Number: " + fData );
    			
    			try {
    				Thread.sleep(5000);
    			} catch (InterruptedException e) {
    				System.err.println("ERROR: " + e.getMessage());
    			}
    		}
    	}
    	
    	private void stopGenerator() {
    		this.running = false;
    	}
    	
    	private void sendData() {
			RandomNumbers data = generateRandomNumbers(RandomProducer.this.dataList);
			try {
				send(data);
			} catch (DSPException e) {
				e.printStackTrace();
			}					
		}
	}

}
