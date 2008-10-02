package org.netbeams.dsp;

import org.netbeams.dsp.util.ErrorCode;

public class DMPException extends Exception {

	private static final long serialVersionUID = -2098007379260999336L;
	
	private int error;
	
	public DMPException(String msg){
		this(ErrorCode.ERROR_UNKNOWN, msg);
	}
	
	public DMPException(int error, String msg){
		super(msg);
		this.error = error;
	}

	public DMPException(int error, String msg, Object[] params){
		super(formatMessage(msg, params));
		this.error = error;
	}

	private static Throwable formatMessage(String msg, Object[] params) {
		// TODO Auto-generated method stub
		return null;
	}

	public int getError(){
		return this.error;
	}
}
