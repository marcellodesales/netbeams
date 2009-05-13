package org.netbeams.dsp.ysi;

import java.io.OutputStream;
import java.io.IOException;

/**
 * 
 * @author Teresa L. Johnson <gamma.particle@gmail.com>
 *
 */
public class SerialWriter implements Runnable {

	private OutputStream out;
	private StringBuffer strBuffer;
	
	public SerialWriter(OutputStream out) {
		this.out = out;
		strBuffer = new StringBuffer ("");
	}
	
	public void run() {
		if (SondeDSPComponent.hasSamplingFrequencyChanged) {
			strBuffer.append(SondeDSPComponent.samplingFrequency);					
			try {
				this.out.write((byte) strBuffer.charAt(0));
				this.out.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}		
	
}
