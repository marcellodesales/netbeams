package org.netbeams.dsp.ysi;

import java.io.InputStream;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import org.apache.log4j.Logger;
import org.netbeams.dsp.DSPContext;
import org.netbeams.dsp.DSPException;
import org.netbeams.dsp.MessageBrokerAccessor;
import org.netbeams.dsp.message.ComponentIdentifier;
import org.netbeams.dsp.MessageFactory;
import org.netbeams.dsp.message.MeasureMessage;
import org.netbeams.dsp.message.DSPMessagesFactory;
import org.netbeams.dsp.message.Header;
import org.netbeams.dsp.message.Message;
import org.netbeams.dsp.util.NetworkUtil;


/**
 * 
 * @author Teresa L. Johnson <gamma.particle@gmail.com>
 *
 */
public class SerialReader implements Runnable {
	
	private static final Logger log = Logger.getLogger(SerialReader.class);
	private DSPContext context;
	private List<String> dataList;
	private List<SondeDataType> list;
	private String bufferString;
	private StringBuffer dataString;
	private SondeDataContainer sdc;
	private SondeDataType sdt;
	private InputStream in;
	
	
	public SerialReader (InputStream in, DSPContext context) {
		sdc = new SondeDataContainer();
		dataList = new ArrayList<String> ();
		list = new ArrayList<SondeDataType> ();
		sdc.sondeData = new ArrayList<SondeDataType> ();
		this.in = in;
		this.context = context;
	}
	
	
	private void parseDataStream(StringBuffer dataStream) {
		log.debug("Raw data stream is: " + dataStream.toString());
		StringTokenizer st = new StringTokenizer(dataStream.toString());
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
        dataList.clear();
	}

	
	private void copyToOriginal() {
		sdc.sondeData.add(list.get(0));
		log.debug("SONDE DATA");
		log.debug("==========");
		log.debug(sdc.sondeData.get(0).getDateString());
		log.debug(sdc.sondeData.get(0).getTimeString());
		log.debug(sdc.sondeData.get(0).getTemp());
		log.debug(sdc.sondeData.get(0).getSpCond());
		log.debug(sdc.sondeData.get(0).getCond());
		log.debug(sdc.sondeData.get(0).getResist());
		log.debug(sdc.sondeData.get(0).getSal());
		log.debug(sdc.sondeData.get(0).getPress());
		log.debug(sdc.sondeData.get(0).getDepth());
		log.debug(sdc.sondeData.get(0).getPH());
		log.debug(sdc.sondeData.get(0).getPhmV());
		log.debug(sdc.sondeData.get(0).getODOSat());
		log.debug(sdc.sondeData.get(0).getODOConc());
		log.debug(sdc.sondeData.get(0).getTurbid());
		log.debug(sdc.sondeData.get(0).getBattery());
	}
	
	
	private void send(SondeDataContainer data) throws DSPException{
		
		String localIPAddress = NetworkUtil.getCurrentEnvironmentNetworkIp();
		
		log.debug("Sonde Data to be sent from " + localIPAddress);
        log.debug("Number of Container Elements: " + data.getSondeData().size());
		
        ComponentIdentifier producer = DSPMessagesFactory.INSTANCE.makeDSPComponentIdentifier(
                "SondeProducer", localIPAddress, data.getContentContextForJAXB());
		
        ComponentIdentifier consumer = null;
        
        Header header = DSPMessagesFactory.INSTANCE.makeDSPMessageHeader(null, producer, consumer);
        
        
        try {
        	Message message = DSPMessagesFactory.INSTANCE.makeDSPMeasureMessage(header, data);
        	
        	// Always check if there is a broker available
        	MessageBrokerAccessor messageBroker = context.getDataBroker();
        	if(messageBroker != null){
        		messageBroker.send(message);
        	}else{
        		log.debug("Message broker not available");
        	}     	
        } catch (DSPException e) {
        	log.error("DSPException");
        	log.error(e.getMessage(), e);
        }		
	}

	public void run() {
		byte[] buffer = new byte[1024];
        int len = -1;
        try {
        	log.debug("Receiving data stream...");
        	while (true) {
        		dataString = new StringBuffer("");
        		while ((len = this.in.read(buffer)) > -1) {
        			bufferString = new String(buffer,0,len);
        			dataString.append(bufferString);
        			if (bufferString.charAt(len-1) == '\n') {
        				break;
        			}        			
        		}
        		parseDataStream(dataString);
        		try {
        			copyToOriginal();
        			send(sdc);
        			//sdc.sondeData.clear();
        			list.clear();   // The next time we send data, we don't want the old data appended.
        		} catch (DSPException e) {
        			System.err.println("ERROR: " + e.getMessage());
        		}
        	}
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
}
