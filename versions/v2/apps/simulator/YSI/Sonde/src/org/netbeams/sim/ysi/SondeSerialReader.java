package org.netbeams.sim.ysi;

import java.io.InputStream;
import java.io.IOException;

/**
 * 
 * @author Teresa L. Johnson <gamma.particle@gmail.com>
 *
 */
public class SondeSerialReader implements Runnable {
	
	private InputStream in;
	
	public SondeSerialReader (InputStream in) {
		this.in = in;
	}


	public void run() {
		byte[] buffer = new byte[1024];
        int len = -1;
        try {
            while ((len = this.in.read(buffer)) > -1) {
                System.out.print(new String(buffer,0,len));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
}
