package org.netbeams.dsp.ysi;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.io.InputStream;
import java.io.OutputStream;

public class SerialHandler {

	public static final int BAUD_RATE = 9600;
	public static final String SERIAL_PORT = "/dev/ttyS0";
	
	public SerialHandler() {
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

	            (new Thread(new SerialReader(in))).start();
	            (new Thread(new SerialWriter(out))).start();
	            System.err.println("Success: Connection established");
	        } else {
	            System.err.println("Error: Only serial ports are handled by this example.");
	        }
		}		        
	}
	
	public static void main ( String[] args ) {

		try {
			(new SerialHandler()).connect(SERIAL_PORT);
		} catch (Exception e) {
	        e.printStackTrace();
	    }
	}	
}
