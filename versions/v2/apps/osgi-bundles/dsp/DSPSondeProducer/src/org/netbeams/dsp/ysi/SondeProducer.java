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
	
	public static final String SERIAL_PORT = "/dev/ttyS0";
	private DSPContext context;
	private SondeHandler sondeHandler;
	private SerialHandler serialHandler;
	private String cmdBuffer;
	private String rcvBuffer;
	
	public SondeProducer (DSPContext context) {
		this.context = context;
		serialHandler = new SerialHandler();
		initSerialConnection();
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
	
	private void initSerialConnection() {
		try {
			serialHandler.connect(SERIAL_PORT);
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	private void configureSonde() {
		serialHandler.sendSerial("\n");		// ----> [CR]
		//serialHandler.recvSerial(rcvBuffer);			// <---- "#"
		//sendSerial(cmdBuffer);		// ----> "menu"
		//recvSerial(responseBuffer); // <---- (Main Menu)
		//sendSerial(cmdBuffer);		// ----> "2"[CR]
		//recvSerial(responseBuffer); // <---- (Unattended Sample Menu)
		//sendSerial(cmdBuffer);		// ----> "1"[CR]
		//recvSerial(responseBuffer); // <---- "00:00:00"
		//sendSerial(cmdBuffer);		// ----> "00:15:00"
	}
	
	
	private void send(SondeDataContainer data) throws DSPException{
		
		String localIPAddress = NetworkUtil.getCurrentEnvironmentNetworkIp();
		
		log.debug("Sonde Data to be sent from " + localIPAddress);
        log.debug("Number of Container Elements: " + data.getSondeData().size());
		
        ComponentIdentifier producer = DSPMessagesFactory.INSTANCE.makeDSPComponentIdentifier(
                "SondeProducer", localIPAddress, data.getContentContextForJAXB());
		
        ComponentIdentifier consumer = DSPMessagesFactory.INSTANCE.makeDSPComponentIdentifier(
        		"DSPWireTransportClient", System.getProperty("WIRE_TRANSPORT_SERVER_IP"), "org.netbeams.dsp.wiretransport.client");
        
        log.debug("Sonde Data to be sent to " + consumer.getComponentLocator().getNodeAddress().getValue());
        Header header = DSPMessagesFactory.INSTANCE.makeDSPMessageHeader(null, producer, consumer);
        
        
        try {
        	Message message = DSPMessagesFactory.INSTANCE.makeDSPMeasureMessage(header, data, org.netbeams.dsp.ysi.ObjectFactory.class);
		
        	// Always check if there is a broker available
        	MessageBrokerAccessor messageBroker = context.getDataBroker();
        	if(messageBroker != null){
        		messageBroker.send(message);
        	}else{
        		log.debug("Message broker not available");
        	}
        } catch (JAXBException e) {
        	log.error("JAXBException");
        	log.error(e.getMessage(), e);
        } catch (ParserConfigurationException e) {
        	log.error("ParseConfigurationException");
        	log.error(e.getMessage(), e);        	
        } catch (DSPException e) {
        	log.error("DSPException");
        	log.error(e.getMessage(), e);
        }		
	}


	private class SondeHandler extends Thread {

		private boolean running = true;
		private SondeDataContainer sondeDataContainer;
		private SondeTestData sondeTestData;
		private List<SondeDataType> sondeData;
		
		public SondeHandler () {
			sondeTestData = new SondeTestData();  // Get the test data
			sondeDataContainer = sondeTestData.getSondeTestData();
		};
		
		
		public void run() {			
			// Configure the sonde
			configureSonde();
			
			//while (running) {
				
				// Communication with a consumer
				/*
				try {
					
					send(sondeDataContainer);
					
				} catch (InterruptedException e) {
					System.err.println("ERROR: " + e.getMessage());
				} catch (DSPException e) {
					System.err.println("ERROR: " + e.getMessage());
				}
				*/
				
			//}
		}
		
		public void stopHandler() {
			this.running = false;
		}	
	
	}

	
}
