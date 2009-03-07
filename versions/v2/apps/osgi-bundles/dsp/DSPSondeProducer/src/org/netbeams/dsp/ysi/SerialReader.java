package org.netbeams.dsp.ysi;

import java.io.InputStream;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import org.apache.log4j.Logger;


/**
 * 
 * @author Teresa L. Johnson <gamma.particle@gmail.com>
 *
 */
public class SerialReader implements Runnable {
	
	private static final Logger log = Logger.getLogger(SerialReader.class);
	private ArrayList<String> dataList;
	private ArrayList<SondeDataType> list;
	private String bufferString;
	private SondeDataContainer sdc;
	SondeDataType sdt;
	private InputStream in;
	
	
	public SerialReader (InputStream in) {
		sdc = new SondeDataContainer();
		dataList = new ArrayList<String> ();
		list = new ArrayList<SondeDataType> ();
		sdc.sondeData = list;
		bufferString = new String("");
		this.in = in;
	}
	
	
	private void parseDataStream(String dataStream) {
		StringTokenizer st = new StringTokenizer(bufferString);
        while (st.hasMoreTokens()) {
			dataList.add(st.nextToken());
        }
        try {
        	sdt = new SondeDataType();
        	sdt.setDateTime(dataList.get(0),dataList.get(1));
        	sdt.setTemp(Float.parseFloat(dataList.get(2).trim()));
        	sdt.setSpCond(Float.parseFloat(dataList.get(3).trim()));
        	sdt.setCond(Float.parseFloat(dataList.get(4).trim()));
        	sdt.setResist(Float.parseFloat(dataList.get(5).trim()));
        	sdt.setSal(Float.parseFloat(dataList.get(6).trim()));
        	sdt.setPress(Float.parseFloat(dataList.get(7).trim()));
        	sdt.setDepth(Float.parseFloat(dataList.get(8).trim()));
        	sdt.setPH(Float.parseFloat(dataList.get(9).trim()));
        	sdt.setPhmV(Float.parseFloat(dataList.get(10).trim()));
        	sdt.setODOSat(Float.parseFloat(dataList.get(11).trim()));
        	sdt.setODOConc(Float.parseFloat(dataList.get(12).trim()));
        	sdt.setTurbid(Float.parseFloat(dataList.get(13).trim()));
        	sdt.setBattery(Float.parseFloat(dataList.get(14).trim()));
        } catch (NumberFormatException nfe) {
        	System.out.println("NumberFormatException: " + nfe.getMessage());
        }
        list.add(sdt);        
	}


	public void run() {
		byte[] buffer = new byte[1024];
        int len = -1;
        try {
        	log.debug("Receiving data stream...");
            while ((len = this.in.read(buffer)) > -1) {
            	bufferString = new String(buffer,0,len);
                System.out.print(bufferString);
                parseDataStream(bufferString);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
}
