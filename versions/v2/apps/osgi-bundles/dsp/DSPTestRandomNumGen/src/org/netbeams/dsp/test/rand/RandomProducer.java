package org.netbeams.dsp.test.rand;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.netbeams.dsp.ComponentDescriptor;
import org.netbeams.dsp.DSPComponent;
import org.netbeams.dsp.DSPContext;
import org.netbeams.dsp.DSPException;
import org.netbeams.dsp.MessageFactory;
import org.netbeams.dsp.data.property.DSProperties;
import org.netbeams.dsp.data.property.DSProperty;
import org.netbeams.dsp.demo.mouseactions.model.dsp.MouseActionDSPComponent;
import org.netbeams.dsp.message.ComponentIdentifier;
import org.netbeams.dsp.message.ComponentLocator;
import org.netbeams.dsp.message.DSPMessagesFactory;
import org.netbeams.dsp.message.Header;
import org.netbeams.dsp.message.MeasureMessage;
import org.netbeams.dsp.message.Message;
import org.netbeams.dsp.message.content.ObjectFactory;
import org.netbeams.dsp.messagesdirectory.controller.DSPMessagesDirectory;
import org.netbeams.dsp.wiretransport.controller.DSPWireTransportHttpClient;


public class RandomProducer implements DSPComponent {
	
	private static final Logger log = Logger.getLogger(RandomProducer.class);	
	
	private static ComponentDescriptor componentDescriptor;
	private DSPContext context;
    private String componentNodeId;
    private ComponentLocator locator;
    private ComponentDescriptor descriptor;
    private List <RandomNumber> dataList;
    private RandomNumberGenerator rng;

	private DSPMessagesDirectory messagesQueueService;
    
    public static final String COMPONENT_TYPE = "org.netbeams.dsp.test.rand";
	public static final String MSG_CONTENT_TYPE_RANDOM_FLOAT = "random.float";
    
    public RandomProducer(DSPMessagesDirectory messQueueService) {
    	this.messagesQueueService = messQueueService;
    };
    
        
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
		
		String localIPAddress = DSPWireTransportHttpClient.getCurrentEnvironmentNetworkIp();
        
        log.debug("Packaging Random Numbers to be sent from " + localIPAddress);
        log.debug("Total number of Mouse Actions: " + rNums.getRandomNumber().size());
        
        DSProperties randomNumbersData = new DSProperties();
        for (RandomNumber r : rNums.getRandomNumber())
        {
        	DSProperty b = new DSProperty();
        	b.setValue(String.valueOf(r.getValue()));
        	randomNumbersData.getProperty().add(b);
        }
        ComponentIdentifier producer = DSPMessagesFactory.INSTANCE.makeDSPComponentIdentifier(
                MouseActionDSPComponent.class.getName(), localIPAddress, randomNumbersData.getContentContextForJAXB());
        ComponentIdentifier consumer = DSPMessagesFactory.INSTANCE.makeDSPComponentIdentifier("DSPWebLogger",
                "130.212.214.119", null);
        // Note that the consumer has the Wire Transport default DEMO IP as the destination.
        
        log.debug("Packaging Random Numbers to be sent to "
                + consumer.getComponentLocator().getNodeAddress().getValue());
        Header header = DSPMessagesFactory.INSTANCE.makeDSPMessageHeader(null, producer, consumer);
        
        try {
            Message message = DSPMessagesFactory.INSTANCE.makeDSPMeasureMessage(header, randomNumbersData, org.netbeams.dsp.data.property.ObjectFactory.class);
            messagesQueueService.addMessageToOutboundQueue(message);

    		// Always check if there is a broker available
//    		MessageBrokerAccessor messageBroker = context.getDataBroker();
//    		if(messageBroker != null){
//    			messageBroker.send(message);
//    		}else{
//    			log.debug("Message broker not available");
//    		}
            
        } catch (JAXBException e) {
            log.error(e.getMessage(), e);
        } catch (ParserConfigurationException e) {
            log.error(e.getMessage(), e);
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
