package org.netbeams.dsp.ysi;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import org.netbeams.dsp.DSPContext;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 
 * @author Teresa L. Johnson <gamma.particle@gmail.com>
 *
 */
public class SerialHandler {

	private static final int BAUD_RATE = 9600;
	private DSPContext context;
	private InputStream in;
	private OutputStream out;
	private SerialReader sr;
	private SerialWriter sw;
	
	
	public SerialHandler(DSPContext context) {
		super();
		this.context = context;
	};
	
	public void connect (String portName) throws Exception {

		CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
		if (portIdentifier.isCurrentlyOwned()) {
			System.err.println("Error: Port is currently in use");
		} else {
			CommPort commPort = portIdentifier.open(this.getClass().getName(),2000);
			if (commPort instanceof SerialPort) {
				
				SerialPort serialPort = (SerialPort) commPort;
	            serialPort.setSerialPortParams(BAUD_RATE,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);
	            
	            in  = serialPort.getInputStream();
	            out = serialPort.getOutputStream();
	    		
	            sr = new SerialReader(in, context);
	    		sw = new SerialWriter(out);
	            
	    		Thread srThread = new Thread(sr);
	    		Thread swThread = new Thread(sw);
	    		
	    		srThread.start();
	    		swThread.start();
	            
	            System.err.println("Success: Connection established");
	        } else {
	            System.err.println("Error: Only serial ports are handled.");
	        }
		}		        
	}
}
