package org.netbeams.test.randomnumber;

import java.security.SecureRandom;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

/**
 * 
 * @author Teresa L. Johnson <gamma.particle@gmail.com>
 *
 */
public class RandomNumber extends Thread {
	
	private boolean running = true;
	SecureRandom random = new SecureRandom();
	byte bytes[] = new byte[20];
	float d;
	
	public RandomNumber() {};
	
	public void getRandomNum() {		
		ByteArrayInputStream byte_in = new ByteArrayInputStream (bytes);
		DataInputStream data_in = new DataInputStream (byte_in);
		try {
			d = data_in.readFloat();
		} catch (Exception e){
			System.err.println("Error: " + e.getMessage());
		}
	}
	
	public void run() {
		while (running) {
			
			random.nextBytes(bytes);
			getRandomNum();
			System.out.println("Random Number: " + d );
			
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				System.out.println("RandomNumberThread ERROR: " + e);
			}
		}
	}
	
	public void stopThread() {
		this.running = false;
	}
}
