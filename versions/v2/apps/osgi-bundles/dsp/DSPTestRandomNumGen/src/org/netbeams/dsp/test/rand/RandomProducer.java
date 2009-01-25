package org.netbeams.dsp.test.rand;

import java.security.SecureRandom;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.netbeams.dsp.ComponentDescriptor;
import org.netbeams.dsp.message.ComponentLocator;
import org.netbeams.dsp.DSPComponent;
import org.netbeams.dsp.DSPContext;
import org.netbeams.dsp.DSPException;
import org.netbeams.dsp.MessageBrokerAccessor;
import org.netbeams.dsp.MessageCategory;
import org.netbeams.dsp.MessageFactory;
import org.netbeams.dsp.message.MeasureMessage;
import org.netbeams.dsp.message.Message;


public class RandomProducer implements DSPComponent {
	
	private static final Logger log = Logger.getLogger(RandomProducer.class);	
	
	private static ComponentDescriptor componentDescriptor;
	private DSPContext context;
    private String componentNodeId;
    private ComponentLocator locator;
    private ComponentDescriptor descriptor;
    private List <RandomNumber> dataList;
    private RandomNumberGenerator rng;
    
    public static final String COMPONENT_TYPE = "org.netbeams.dsp.test.rand";
	public static final String MSG_CONTENT_TYPE_RANDOM_FLOAT = "random.float";
    
    public RandomProducer() {};
    
        
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
		rng = new RandomNumberGenerator();
		rng.start();
	}

	public void stopComponent() throws DSPException {
		log.info("Stopping component");
		
		if (rng != null) {
			rng.stopGenerator();
			rng = null;
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
		
		Message message = MessageFactory.newMessage(MeasureMessage.class, rNums, this);
		log.debug("Send random number");
				
		if(rNums == null){
			log.debug("No random number generated");
		} else {
			if(rNums.getRandomNumber() == null){
				log.debug("No random number generated");
			} else {
				log.debug("Size of Random number set is: " + rNums.getRandomNumber().size());
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
