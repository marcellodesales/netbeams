package org.netbeams.test.serial;

/**
 * 
 * @author Teresa L. Johnson <gamma.particle@gmail.com>
 *
 */
public class SerialBundle extends Thread {

	private static final String SERIAL_PORT = "/dev/ttyS0";
	private SerialHandler sh;
	
	public SerialBundle() {
		sh = new SerialHandler();
	};
	
	public void run() {
		try {
			System.out.println("Serial connection established from bundle...");
			sh.connect(SERIAL_PORT);
		} catch (Exception e) {
			System.out.println("ERROR: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
}
