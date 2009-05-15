package org.netbeams.test.serial;

import java.io.OutputStream;
import java.io.IOException;

public class SerialWriter implements Runnable {

	private OutputStream out;
	private StringBuffer strBuffer;
	
	public SerialWriter(OutputStream out) {
		this.out = out;
		strBuffer = new StringBuffer ("");
	};
	
	public void run() {
		// Send a '1' to change the interval to 1 second in the simulator
		strBuffer.append("1");
		try {
			// To show that the sending rate changes from 5 seconds to 1 second,
			// pause for 30 seconds while data is sent every 5 seconds. After
			// 30 seconds expires, data is sent every 1 second.  This tests the
			// interval rate change.
			Thread.sleep(30000);
			this.out.write((byte) strBuffer.charAt(0));
			this.out.flush();
		} catch (IOException ioe) {
			System.out.println("ERROR: " + ioe.getMessage());
			ioe.printStackTrace();
		} catch (Exception e) {
			System.out.println("ERROR: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
}
