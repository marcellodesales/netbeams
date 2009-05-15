package org.netbeams.sim.ysi;


import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * 
 * @author Teresa L. Johnson <gamma.particle@gmail.com>
 *
 */
public class SondeSerialHandler {
	
	private static final int BAUD_RATE = 9600;
	private InputStream in;
	private OutputStream out;
	private SondeSerialReader ssr;
	private SondeSerialWriter ssw;
	private byte[] buffer;
	
	public SondeSerialHandler() {
		super();
		buffer = new byte[1024];
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

	            in = serialPort.getInputStream();
	            out = serialPort.getOutputStream();

	            ssr = new SondeSerialReader(in);
	    		ssw = new SondeSerialWriter(out);
	    		
	    		Thread ssrThread = new Thread(ssr);
	    		Thread sswThread = new Thread(ssw);
	    		
	    		ssrThread.start();
	    		sswThread.start();
	            
	            System.out.println("Success: Connection established");
	        } else {
	            System.out.println("Error: Only serial ports are handled.");
	        }
		}		        
	}
}
