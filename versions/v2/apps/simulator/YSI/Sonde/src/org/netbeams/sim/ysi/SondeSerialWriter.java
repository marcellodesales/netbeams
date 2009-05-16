package org.netbeams.sim.ysi;

import java.io.OutputStream;
import java.io.IOException;

/**
 * 
 * @author Teresa L. Johnson <gamma.particle@gmail.com>
 *
 */
public class SondeSerialWriter implements Runnable {

	public static long interval = 6000;
	private OutputStream out;
	private SondeTestData sdt;
	private StringBuffer strBuffer;

	
	public SondeSerialWriter(OutputStream out) {
		sdt = new SondeTestData();
		//strBuffer = new StringBuffer (sdt.getDataStream());
	    //strBuffer.append("\n");
		this.out = out;
	}
	
	public void run() {
		System.err.println("Writing data to output stream...");
		try {
			while (true) {
				strBuffer = new StringBuffer (sdt.getDataStream());
				strBuffer.append("\n");
				for (int i = 0; i < strBuffer.length(); i++) {
					this.out.write((byte) strBuffer.charAt(i));
				}
				//System.out.println();
				//System.out.write((byte) 0x40);
				this.out.flush();
				try{ 
		           Thread.sleep(interval);
		        } catch( InterruptedException e ) {
		            System.err.println("ERROR: " + e.getMessage());
		            e.printStackTrace();
		        }
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}		
}
