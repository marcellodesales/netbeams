package org.netbeams.sim.ysi;


/**
 * 
 * @author Teresa L. Johnson <gamma.particle@gmail.com>
 *
 */
public class SondeServer extends Thread {
	
	private static final String SONDE_PROMPT = "# ";
	private static final String SERIAL_PORT = "/dev/ttyS0";
	private SondeSerialHandler serialHandler;
	private SondeMenu sondeMenu;
	private byte[] buffer;
	
	public SondeServer() {
		buffer = new byte[1024];
		sondeMenu = new SondeMainMenu();
		serialHandler = new SondeSerialHandler();
	};
	
	public String getSondPrompt() {
		return SONDE_PROMPT;
	}

	public void sendPrompt() {
		System.out.println(SONDE_PROMPT);
	}
	
	
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
