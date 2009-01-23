package org.netbeams.dsp.test.rand;

import java.security.SecureRandom;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.ArrayList;
import java.util.List;

import org.netbeams.dsp.ComponentDescriptor;
import org.netbeams.dsp.message.ComponentLocator;
import org.netbeams.dsp.DSPComponent;
import org.netbeams.dsp.DSPContext;
import org.netbeams.dsp.DSPException;
import org.netbeams.dsp.message.Message;
import org.netbeams.dsp.util.Log;

public class RandomNumberGenerator extends Thread implements DSPComponent {
	
	public final static String COMPONENT_TYPE = "org.netbeams.dsp.test.rand";
	private static ComponentDescriptor componentDescriptor;
	private String componentNodeId;
    private DSPContext context;
    private ComponentLocator locator;
    private ComponentDescriptor descriptor;
    private List <String> dataBuffer = new ArrayList<String>();
    private boolean running = true;
    private Message msg;
    SecureRandom random;
    byte bytes[];
    float fData;
    
    
    public RandomNumberGenerator() {
    	random = new SecureRandom();
    	bytes = new byte[20];
    };
    
    
    public void getRandomNum() {		
		ByteArrayInputStream byte_in = new ByteArrayInputStream (bytes);
		DataInputStream data_in = new DataInputStream (byte_in);
		try {
			fData = data_in.readFloat();
		} catch (Exception e){
			System.err.println("Error: " + e.getMessage());
		}
	}
    
    
    public void run() {
		while (running) {			
			random.nextBytes(bytes);
			getRandomNum();
			System.out.println("Random Number: " + fData );
			
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				System.out.println("RandomNumberThread ERROR: " + e);
			}
		}
	}
    
    public void stopThread() {
		this.running = false;
	}
    
 // Implemented methods of the DSPComponent interface.
    
    public void deliver(Message message) throws DSPException {
    	Log.log("RandomNumberGenerator.deliver()");
	}

	public Message deliverWithReply(Message message) throws DSPException {		
		return null;
	}

	public Message deliverWithReply(Message message, long waitTime) throws DSPException {
		return null;
	}

	public ComponentDescriptor getComponentDescriptor() {		
		return componentDescriptor;	
	}

	public void startComponent() throws DSPException {
		Log.log("RandomNumberGenerator.startComponent()");
		this.run();
	}

	public void stopComponent() throws DSPException {		
		Log.log("RandomNumberGenerator.stopComponent()");
		this.stopThread();
	}

	public String getComponentNodeId() {
		return null;
	}

	public String getComponentType() {		
		return COMPONENT_TYPE;		
	}

	public void initComponent(String componentNodeId, DSPContext context) throws DSPException {
		Log.log("RandomNumberGenerator.initComponent()");
        this.context = context;
        this.componentNodeId = componentNodeId;
	}

}
