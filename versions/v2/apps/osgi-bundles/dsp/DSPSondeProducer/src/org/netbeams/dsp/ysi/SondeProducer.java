package org.netbeams.dsp.ysi;

import java.util.List;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.log4j.Logger;
import org.netbeams.dsp.DSPContext;
import org.netbeams.dsp.DSPException;
import org.netbeams.dsp.MessageBrokerAccessor;
import org.netbeams.dsp.message.ComponentIdentifier;
import org.netbeams.dsp.message.DSPMessagesFactory;
import org.netbeams.dsp.message.Header;
import org.netbeams.dsp.message.Message;
import org.netbeams.dsp.util.NetworkUtil;

/**
 * 
 * @author Teresa L. Johnson <gamma.particle@gmail.com>
 *
 */
public class SondeProducer {
	
	private static final Logger log = Logger.getLogger(SondeProducer.class);
	
	private DSPContext context;
	private SondeHandler sondeHandler;
	
	public SondeProducer (DSPContext context) {
		this.context = context;
        log.trace("Sonde Producer initialized");
	};
	
	
	public void startProducer() {
		sondeHandler = new SondeHandler();
		sondeHandler.start();
	}
	
	
	public void stopProducer() {
		if (sondeHandler != null) {
			sondeHandler.stopHandler();
			sondeHandler = null;
		}
	}
	
	
	private void send(SondeDataContainer data) throws DSPException{
		
		String localIPAddress = NetworkUtil.getCurrentEnvironmentNetworkIp();
		
		log.debug("Sonde Data to be sent from " + localIPAddress);
        log.debug("Number of Container Elements: " + data.getSondeData().size());
		
        ComponentIdentifier producer = DSPMessagesFactory.INSTANCE.makeDSPComponentIdentifier(
                "SondeProducer", localIPAddress, data.getContentContextForJAXB());
		
        //ComponentIdentifier consumer = DSPMessagesFactory.INSTANCE.makeDSPComponentIdentifier(
        //		"DSPWireTransportClient", System.getProperty("WIRE_TRANSPORT_SERVER_IP"), "org.netbeams.dsp.wiretransport.client");
        ComponentIdentifier consumer = null;
        
        //log.debug("Sonde Data to be sent to " + consumer.getComponentLocator().getNodeAddress().getValue());
        Header header = DSPMessagesFactory.INSTANCE.makeDSPMessageHeader(null, producer, consumer);
        
        
        try {
        	Message message = DSPMessagesFactory.INSTANCE.makeDSPMeasureMessage(header, data);
		
        	
        	// Always check if there is a broker available
        	MessageBrokerAccessor messageBroker = context.getDataBroker();
        	if(messageBroker != null){
        		messageBroker.send(message);
        	}else{
        		log.debug("Message broker not available");
        	}     	
        } catch (DSPException e) {
        	log.error("DSPException");
        	log.error(e.getMessage(), e);
        }		
	}


	private class SondeHandler extends Thread {

		private static final String SERIAL_PORT = "/dev/ttyS0";
		private boolean running = true;
		private SerialHandler serialHandler;
		private SondeDataContainer sondeDataContainer;
		private SondeTestData sondeTestData;    // Testing for the producer side only.
		private List<SondeDataType> sondeData;  // Testing for the producer side only.
		
		public SondeHandler () {
			serialHandler = new SerialHandler();
			sondeTestData = new SondeTestData();  // Get producer test data.
			sondeDataContainer = sondeTestData.getSondeTestData();
		};
		
		
		public void run() {			
			while (running) {
				/*
				try { 
					log.debug("Serial connection established from bundle...");
					serialHandler.connect(SERIAL_PORT);
				} catch (Exception e) {
					System.out.println("ERROR: " + e.getMessage());
					e.printStackTrace();
				}*/
				// Communication with the consumer
				
				
				
				try {
					send(sondeDataContainer);
				} catch (DSPException e) {
					System.err.println("ERROR: " + e.getMessage());
				}
				try {
				Thread.sleep(10000);
				} catch (Exception e) {
					System.out.println("Error: " + e.getMessage());
				}
			}
		}
		
		public void stopHandler() {
			this.running = false;
		}	
	
	}

	
}
