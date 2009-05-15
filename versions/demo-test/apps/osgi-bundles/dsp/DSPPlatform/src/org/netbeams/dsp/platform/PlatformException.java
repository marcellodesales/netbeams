package org.netbeams.dsp.platform;

public class PlatformException extends Exception {

	public PlatformException(String msg){
		super(msg);
	}
	
	public PlatformException(Exception e){
		super(e);
	}
	
	public PlatformException(String msg, Exception e){
		super(msg, e);
	}	
}
