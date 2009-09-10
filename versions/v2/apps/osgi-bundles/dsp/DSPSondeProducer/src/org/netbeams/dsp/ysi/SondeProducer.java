package org.netbeams.dsp.ysi;

import java.util.List;
import org.apache.log4j.Logger;
import org.netbeams.dsp.DSPContext;
import org.netbeams.dsp.DSPException;
import org.netbeams.dsp.MessageBrokerAccessor;
import org.netbeams.dsp.message.ComponentIdentifier;
import org.netbeams.dsp.MessageFactory;
import org.netbeams.dsp.message.MeasureMessage;
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
		sondeHandler = new SondeHandler(context);
		sondeHandler.start();
	}
	
	
	public void stopProducer() {
		if (sondeHandler != null) {
			sondeHandler.stopHandler();
			sondeHandler = null;
		}
	}
	

	private class SondeHandler extends Thread {

		private static final String SERIAL_PORT = "/dev/ttyS0";
		//private boolean running = true;
		private SerialHandler serialHandler;
		//private SondeDataContainer sondeDataContainer;
		//private SondeTestData sondeTestData;    // Testing for the producer side only.
				
		public SondeHandler (DSPContext context) {
			serialHandler = new SerialHandler(context);
			//sondeTestData = new SondeTestData();  // Get producer test data.
			//sondeDataContainer = sondeTestData.getSondeTestData();
		};
		
		
		public void run() {			
			try { 
				log.debug("Serial connection established from bundle...");
				serialHandler.connect(SERIAL_PORT);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
		
		public void stopHandler() {
			//this.running = false;
		}	
	
	}

	
}
