package org.netbeams.dsp.ysi;

import java.util.ArrayList;
import java.util.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.StringTokenizer;
import java.text.SimpleDateFormat;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.DatatypeConfigurationException;

/**
 * 
 * @author Teresa L. Johnson <gamma.particle@gmail.com>
 *
 */
public class SondeTestData {

	private SondeDataContainer sdc;
	private ArrayList<SondeDataType> list;
	private SondeDataType sdt;
	private static GregorianCalendar dateTime;
	private Month m;
		
	public SondeTestData() {
		m = new Month();
		sdc = new SondeDataContainer();
		list = new ArrayList<SondeDataType> ();
		initDateTime();
		initTestData();
		sdc.sondeData = list;
	};
	
	public SondeDataContainer getSondeTestData() {
		return sdc;
	}
		
	private void initDateTime() {
		dateTime = new GregorianCalendar();
		dateTime.clear();
		dateTime.set(2008, Calendar.DECEMBER, 2, 22, 48, 53);
	}
	
	private void initTestData() {;
		setDataSet1();
		setDataSet2();
		setDataSet3();
	}
	
	private void setDataSet1() {
		sdt = new SondeDataType();
		//sdt.setDate(dateTime);
		//sdt.setTime(dateTime);
		sdt.setTemp((float) 22.69);
		sdt.setSpCond((float) 183.0);
		sdt.setCond((float) 175.0);
		sdt.setResist((float) 5704.66);
		sdt.setSal((float) 0.09);
		sdt.setPress((float) 0.00);
		sdt.setDepth((float) 0.00);
		sdt.setPH((float) 8.22);
		sdt.setPhmV((float) -94.00);
		sdt.setODOSat((float) 111.70);
		sdt.setODOConc((float) 9.63);
		sdt.setTurbid((float) 0.30);
		sdt.setBattery((float) 10.10);
		list.add(sdt);
	}
	
	private void setDataSet2() {
		sdt = new SondeDataType();
		//sdt.setDate(dateTime);
		//sdt.setTime(dateTime);
		sdt.setTemp((float) 22.69);
		sdt.setSpCond((float) 183.0);
		sdt.setCond((float) 175.0);
		sdt.setResist((float) 5710.39);
		sdt.setSal((float) 0.09);
		sdt.setPress((float) 0.00);
		sdt.setDepth((float) 0.00);
		sdt.setPH((float) 8.22);
		sdt.setPhmV((float) -94.10);
		sdt.setODOSat((float) 111.60);
		sdt.setTurbid((float) 0.40);
		sdt.setBattery((float) 10.00);
		list.add(sdt);
	}
	
	private void setDataSet3() {
		sdt = new SondeDataType();
		//sdt.setDate(dateTime);
		//sdt.setTime(dateTime);
		sdt.setTemp((float) 22.70);
		sdt.setSpCond((float) 183.0);
		sdt.setCond((float) 175.0);
		sdt.setResist((float) 5710.49);
		sdt.setSal((float) 0.09);
		sdt.setPress((float) 0.00);
		sdt.setDepth((float) 0.00);
		sdt.setPH((float) 8.22);
		sdt.setPhmV((float) -94.00);
		sdt.setODOSat((float) 111.70);
		sdt.setTurbid((float) 0.40);
		sdt.setBattery((float) 10.10);
		list.add(sdt);
	}
	
	
	
	public String dateToString(GregorianCalendar cal) {
		String result = "", temp = "", year = "", day = "";
		Integer month = 0;
		int i = 0;
		
		StringTokenizer st = new StringTokenizer(cal.getTime().toString());
		while (st.hasMoreTokens()) {
			i++;
			temp = st.nextToken();
			if (i == 2) {
				month = (Integer) m.getMonth(temp);
			}
			if (i == 3) day = temp;
			if (i == 6) year = temp;			
		}
		result = year + "/" + month + "/" + day;
		return result;
	}
	
	
	
	public String timeToString(GregorianCalendar cal) {
		String result = "";
		int i = 0;
		
		StringTokenizer st = new StringTokenizer(cal.getTime().toString());
		while (st.hasMoreTokens()) {
			i++;
		    result = st.nextToken();
		    if (i == 4 && result.charAt(2) == ':') {
		        break;
		    }
		}
		return result;
	}
	
	
	/*
	public static void main(String[] args) {
		SondeTestData testData = new SondeTestData();
		SondeDataContainer container;
		
		container = testData.getSondeTestData();
		
		System.out.println("size: " + container.getSondeData().size()); 
		System.out.println();
		
		// Verify that the data can be retrieved.
		for (int i = 0; i < container.getSondeData().size(); i++) {
			System.out.println("Date: " + testData.dateToString((GregorianCalendar) container.getSondeData().get(i).getDate()));
			System.out.println("Time: " + testData.timeToString((GregorianCalendar) container.getSondeData().get(i).getTime()));
			System.out.println("Temp: " + container.getSondeData().get(i).getTemp());
			System.out.println("SpCond: " + container.getSondeData().get(i).getSpCond());
			System.out.println("Cond: " + container.getSondeData().get(i).getCond());
			System.out.println("Resist: " + container.getSondeData().get(i).getResist());
			System.out.println("Sal: " + container.getSondeData().get(i).getSal());
			System.out.println("Press: " + container.getSondeData().get(i).getPress());
			System.out.println("Depth: " + container.getSondeData().get(i).getDepth());
			System.out.println("pH: " + container.getSondeData().get(i).getPH());
			System.out.println("pH mV: " + container.getSondeData().get(i).getPhmV());
			System.out.println("ODO Sat: " + container.getSondeData().get(i).getODOSat());
			System.out.println("Turbidity: " + container.getSondeData().get(i).getTurbid());
			System.out.println("Battery: " + container.getSondeData().get(i).getBattery());
			System.out.println();
			System.out.println();
		}
	}
	*/		
}
