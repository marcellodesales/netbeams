package org.netbeams.sim.ysi;


/**
 * 
 * @author Teresa L. Johnson <gamma.particle@gmail.com>
 *
 */
public class SondeServer extends Thread {
	
	private static final String SERIAL_PORT = "/dev/ttyS0";
	private SondeSerialHandler serialHandler;
	
	public SondeServer() {
		serialHandler = new SondeSerialHandler();
	};
	
	
	public void run() {		
		try {
				serialHandler.connect(SERIAL_PORT);
				System.err.println("Connection Successful!");				
			} catch (Exception e) {
				System.err.println("Error: " + e.getMessage());
				e.printStackTrace();
			}
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SondeServer sonde = new SondeServer();
		sonde.run();
	}

}
