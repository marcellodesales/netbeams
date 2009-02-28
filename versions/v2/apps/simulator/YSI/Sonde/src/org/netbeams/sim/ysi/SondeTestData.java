package org.netbeams.sim.ysi;

import java.util.StringTokenizer;
import java.util.ArrayList;

public class SondeTestData {

	private String dataStream = "2008/12/02 22:48:53 21.20    193    179 5588.40   0.09   0.084   0.059  7.98   -79.6   99.5   8.83     0.4     8.7";
	private ArrayList<String> dataList;
	
	private String date;		// YYYY/MM/DD
	private String time;		// hh:mm:ss 
	private String temp;		// Temperature in degrees Celsius
	private String spCond;		// Specific Conductance in microSiemens per centimeter
	private String cond;		// Conductance in microSiemens per centimeter
	private String resist;		// Resistivity in Ohms * centimeter
	private String sal;			// Salinity in parts per thousand (set to local barometer at calibration)
	private String press;		// Pressure in pounds per square inch relative
	private String depth;		// Water column in meters
	private String pH;			// pH in standard units
	private String pHmV;		// millivolts associated with the pH reading
	private String odoSat;		// Dissolved oxygen in %air saturation from ROX Optical Sensor
	private String odoConc;		// Dissolved oxygen in mg/L from ROX Optical Sensor
	private String turbid;		// Turbidity in nephelometric turbidity units from 6136 sensor
	private String battery;		// Volts
		
	public SondeTestData() {
		dataList = new ArrayList<String> ();
		initializeData();
	};
	
	public void createNewDataStream(String dataParameter) {
		// TODO: Create a new data stream after receiving a configuration change request.
	}
	
	
	private void initializeData() {
		
		StringTokenizer st = new StringTokenizer(dataStream);
		while (st.hasMoreTokens()) {
			dataList.add(st.nextToken());
		}
		
		date = dataList.get(0);
		time = dataList.get(1);
		temp = dataList.get(2);
		spCond = dataList.get(3);
		cond = dataList.get(4);
		resist = dataList.get(5);
		sal = dataList.get(6);
		press = dataList.get(7);
		depth = dataList.get(8);
		pH = dataList.get(9);
		pHmV = dataList.get(10);
		odoSat = dataList.get(11);
		odoConc = dataList.get(12);
		turbid = dataList.get(13);
		battery = dataList.get(14);
	}
	
	
	public String getDate() {
		return this.date;
	}
	
	public String getTime() {
		return this.time;
	}
	
	public String getTemp() {
		return this.temp;
	}
	
	public String getSpCond() {
		return this.spCond;
	}
	
	public String getCond() {
		return this.cond;
	}
	
	public String getResist() {
		return this.resist;
	}
		
	public String getSal() {
		return this.sal;
	}
	
	public String getPress() {
		return this.press;
	}
	
	public String getDepth() {
		return this.depth;
	}
	
	public String getPh() {
		return this.pH;
	}
	
	public String getpHmV() {
		return this.pHmV;
	}
	
	public String getOdoSat() {
		return this.odoSat;
	}
	
	public String getOdoConc() {
		return this.odoConc;
	}
	
	public String getTurbid() {
		return this.turbid;
	}
	
	public String getBattery() {
		return this.battery;
	}
	
	public String getDataStream() {
		return this.dataStream;
	}
	

	/*
	public static void main(String[] args) {		
		SondeTestData std = new SondeTestData();
		
		System.out.println("Date: " + std.getDate());
		System.out.println("Time: " + std.getTime());
		System.out.println("Battery: " + std.getBattery());
		
	}
	*/
}
