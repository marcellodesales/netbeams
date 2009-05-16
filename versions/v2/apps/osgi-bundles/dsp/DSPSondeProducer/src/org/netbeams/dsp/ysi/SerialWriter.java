package org.netbeams.dsp.ysi;

import java.io.OutputStream;
import java.io.IOException;
import org.apache.log4j.Logger;

/**
 * 
 * @author Teresa L. Johnson <gamma.particle@gmail.com>
 *
 */
public class SerialWriter implements Runnable {

	private static final Logger log = Logger.getLogger(SerialWriter.class);
	private OutputStream out;
	private StringBuffer strBuffer;
	
	public SerialWriter(OutputStream out) {
		this.out = out;
		strBuffer = new StringBuffer ("");
	}
	
	public void run() {
		while(true) {
			if (SondeDSPComponent.hasSamplingFrequencyChanged) {
				strBuffer.append(SondeDSPComponent.samplingFrequency);
				log.debug("New Frequency change: " + strBuffer.charAt(0) + " has been sent");
				try {
					this.out.write((byte) strBuffer.charAt(0));
					this.out.flush();
					SondeDSPComponent.hasSamplingFrequencyChanged = false;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}		
	
}
