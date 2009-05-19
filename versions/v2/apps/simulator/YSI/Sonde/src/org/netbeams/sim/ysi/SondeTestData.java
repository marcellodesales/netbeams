package org.netbeams.sim.ysi;

import java.text.SimpleDateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.StringTokenizer;


public class SondeTestData {

	byte bytes[] = new byte[20];
	private static final String DATE_FORMAT_NOW = "yyyy/MM/dd HH:mm:ss";
	private String dataStream = "  21.20    193    179 5588.40   0.09   0.084   0.059  7.98   -79.6   99.5   8.83     0.4     8.7";
	private ArrayList<String> dataList;
	private DecimalFormat oneDecimal;
	private DecimalFormat twoDecimals;
	private DecimalFormat threeDecimals;

	
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
		oneDecimal = new DecimalFormat("###0.0");
		twoDecimals = new DecimalFormat("###0.00");
		threeDecimals = new DecimalFormat("###0.000");
		dataList = new ArrayList<String> ();
		initializeData();
	};
	
	public static String now() {
	    Calendar cal = Calendar.getInstance();
	    SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
	    return sdf.format(cal.getTime());

	 }

	private void initializeData() {
		String dataStream2 = now() + dataStream;
		StringTokenizer st = new StringTokenizer(dataStream2);
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
		return twoDecimals.format(Double.valueOf(this.temp) + Math.random());
	}
	
	public String getSpCond() {
		return oneDecimal.format(Double.valueOf(this.spCond) + Math.random());
	}
	
	public String getCond() {
		return oneDecimal.format(Double.valueOf(this.cond) + Math.random());
	}
	
	public String getResist() {
		return twoDecimals.format(Double.valueOf(this.resist) + Math.random());
	}
		
	public String getSal() {
		return twoDecimals.format(Double.valueOf(this.sal) + Math.random());
	}
	
	public String getPress() {
		return threeDecimals.format(Double.valueOf(this.press) + Math.random());
	}
	
	public String getDepth() {
		return threeDecimals.format(Double.valueOf(this.depth) + Math.random());
	}
	
	public String getPh() {
		return twoDecimals.format(Double.valueOf(this.pH) + Math.random());
	}
	
	public String getpHmV() {
		return oneDecimal.format(Double.valueOf(this.pHmV) + Math.random());
	}
	
	public String getOdoSat() {
		return oneDecimal.format(Double.valueOf(this.odoSat) + Math.random());
	}
	
	public String getOdoConc() {
		return twoDecimals.format(Double.valueOf(this.odoConc) + Math.random());
	}
	
	public String getTurbid() {
		return oneDecimal.format(Double.valueOf(this.turbid) + Math.random());
	}
	
	public String getBattery() {
		return this.battery;
	}
	
	public String getDataStream() {
		String dataStream2 = now() + "   " + getTemp() +  "   " + getSpCond() + "   " + getCond() + "   " + getResist() + "   " + getSal() + "   " + getPress() + "   " + getDepth() + "   " + getPh() + "   " + getpHmV() + "   " + getOdoSat() + "   " + getOdoConc() + "   " + getTurbid() + "   " + getBattery(); 
		return dataStream2;
	}
	
	public String getOldDataStream() {
		String dataStream2 = now() + dataStream;
		return dataStream2;
	}

	/*
	public static void main(String[] args) {		
		SondeTestData std = new SondeTestData();
		System.out.println(std.getOldDataStream());
		System.out.println();
		for (int i = 0; i < 5; i++) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println(std.getDataStream());			
		}
	}
	*/
	
}
