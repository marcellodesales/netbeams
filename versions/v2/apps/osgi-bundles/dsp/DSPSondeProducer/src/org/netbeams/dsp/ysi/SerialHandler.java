package org.netbeams.dsp.ysi;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.io.InputStream;
import java.io.OutputStream;

public class SerialHandler {

	private static final int BAUD_RATE = 9600;
	private CommPort commPort;
	private SerialPort serialPort;
	private InputStream in;
	private OutputStream out;
	private SerialReader sr;
	private SerialWriter sw;
	
	
	public SerialHandler() {
		super();
	};
	
	public void connect (String portName) throws Exception {

		CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
		if (portIdentifier.isCurrentlyOwned()) {
			System.err.println("Error: Port is currently in use");
		} else {
			commPort = portIdentifier.open(this.getClass().getName(),2000);
			if (commPort instanceof SerialPort) {
				
				serialPort = (SerialPort) commPort;
	            serialPort.setSerialPortParams(BAUD_RATE,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);
	            
	            in  = serialPort.getInputStream();
	            out = serialPort.getOutputStream();
	    		
	            System.err.println("Success: Connection established");
	        } else {
	            System.err.println("Error: Only serial ports are handled by this example.");
	        }
		}		        
	}
	
	public void sendSerial(String cmdBuffer) {		
		byte[] buffer = new byte[1024];
		
		buffer = cmdBuffer.getBytes();
		try {
			out.write(buffer);
		} catch (Exception e) {
			System.err.println("ERROR: " + e.getMessage());
			e.printStackTrace();
		}
		sw = new SerialWriter(out);
		sw.run();
	}
	
	public void recvSerial() {
		
	}
	
	
}
