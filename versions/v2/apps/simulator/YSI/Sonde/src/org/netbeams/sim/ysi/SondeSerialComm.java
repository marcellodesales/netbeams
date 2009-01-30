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
public class SondeSerialComm {
	
	public static final int BAUD_RATE = 9600;
	
	public SondeSerialComm() {
		super();
	};
	
	void connect (String portName) throws Exception {

		CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
		if (portIdentifier.isCurrentlyOwned()) {
			System.err.println("Error: Port is currently in use");
		} else {
			CommPort commPort = portIdentifier.open(this.getClass().getName(),2000);
			if (commPort instanceof SerialPort) {
				SerialPort serialPort = (SerialPort) commPort;
	            serialPort.setSerialPortParams(BAUD_RATE,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);

	            InputStream in = serialPort.getInputStream();
	            OutputStream out = serialPort.getOutputStream();

	            (new Thread(new SondeSerialReader(in))).start();
	            (new Thread(new SondeSerialWriter(out))).start();
	            System.err.println("Success: Connection established");
	        } else {
	            System.err.println("Error: Only serial ports are handled by this example.");
	        }
		}		        
	}
	
	public static void main ( String[] args ) {

		try {
			(new SondeSerialComm()).connect("/dev/ttyS0");
		} catch (Exception e) {
	        e.printStackTrace();
	    }
	}
}
