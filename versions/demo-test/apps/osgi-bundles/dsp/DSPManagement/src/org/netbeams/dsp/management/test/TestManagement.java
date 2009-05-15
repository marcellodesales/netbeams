package org.netbeams.dsp.management.test;

import java.io.StringWriter;
import java.util.Properties;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.netbeams.dsp.DSPException;
import org.netbeams.dsp.data.property.DSProperties;
import org.netbeams.dsp.data.property.DSProperty;
import org.netbeams.dsp.management.Manager;
import org.netbeams.dsp.message.MessageContent;
import org.netbeams.dsp.util.NetworkUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class TestManagement extends Thread{

	private static final Logger log = Logger.getLogger(Manager.class);
	
	private Manager manager;
	boolean shouldStop;
	
	public TestManagement(Manager manager){
		shouldStop = false;
		this.manager = manager;
	}
	
	public void run(){
		log.info("Executing tests");
		
		while(!shouldStop && !manager.isActive()){
			log.info("Manager is not active.");
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {}
		}
		testQueryConfiguration();
	}
	
	public void shouldStop(){
		shouldStop = true;
	}
	
	public void testQueryConfiguration(){
		
		log.info("Executing testQueryConfiguration");
		
		String producerCompNodeId = "DSPStockProducer";
		String nodeAddress = NetworkUtil.getCurrentEnvironmentNetworkIp();
		
    	DSProperties properties = new DSProperties();
    	DSProperty property = new DSProperty();
    	property.setName("sampling_frequency");
    	properties.getProperty().add(property);
    	
    	String interactionId = null;
    	
    	try {
    		interactionId = manager.query(producerCompNodeId, "org.netbeams.dsp.demo.stocks.producer", nodeAddress, properties);
		} catch (DSPException e) {
			log.info("Error", e);
			return;
		}
		
		while(!shouldStop){
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {}
			
			MessageContent reply = manager.retrieveInteractionReply(interactionId);
			if(reply == null){
				log.info("No reply yet for interactionId");
				continue;
			}
			log.info("Reply: " + serializeDoc(reply));
			break;
		}
 	}

	public void testUpdateConfiguration(){
		
		log.info("Executing testQueryConfiguration");
		
		String producerCompNodeId = "DSPStockProducer";
		String nodeAddress = NetworkUtil.getCurrentEnvironmentNetworkIp();
		
    	DSProperties properties = new DSProperties();
    	DSProperty property = new DSProperty();
    	property.setName("sampling_frequency");
    	property.setValue("10");
    	properties.getProperty().add(property);
    	
    	String interactionId = null;
    	
    	try {
    		interactionId = manager.query(producerCompNodeId, "org.netbeams.dsp.demo.stocks.producer", nodeAddress, properties);
		} catch (DSPException e) {
			log.info("Error", e);
			return;
		}
		
		while(!shouldStop){
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {}
			
			MessageContent reply = manager.retrieveInteractionReply(interactionId);
			if(reply == null){
				log.info("No reply yet for interactionId");
				continue;
			}
			log.info("Reply: " + serializeDoc(reply));
			break;
		}
 	}
	
	public String serializeDoc(MessageContent content){
		return null;
	}
	
	public String serializeDoc(Node doc){
		StringWriter outText = new StringWriter();
		StreamResult sr = new StreamResult(outText);
		Properties oprops = new Properties();
//		oprops.put(OutputKeys.METHOD, "html");
//		oprops.put("indent-amount", "4");
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer t = null;
		try{
			t = tf.newTransformer();
			t.setOutputProperties(oprops);
			t.transform(new DOMSource(doc),sr);
		} catch(Exception e){
			log.warn("Could not serialize reply", e);
		}
		
		return outText.toString();
	}
}
