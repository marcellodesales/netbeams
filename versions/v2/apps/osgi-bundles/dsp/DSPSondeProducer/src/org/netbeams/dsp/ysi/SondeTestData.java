package org.netbeams.dsp.ysi;

import java.util.ArrayList;

/**
 * 
 * @author Teresa L. Johnson <gamma.particle@gmail.com>
 *
 */
public class SondeTestData {

	private SondeDataContainer sdc;
	private ArrayList<SondeDataType> list;
	private SondeDataType sdt;
		
	public SondeTestData() {
		sdc = new SondeDataContainer();
		list = new ArrayList<SondeDataType> ();
		initTestData();
		sdc.sondeData = list;
	};
	
	public SondeDataContainer getSondeTestData() {
		return sdc;
	}
	
	private void initTestData() {
		setDataSet1();
		setDataSet2();
		setDataSet3();
	}
	
	private void setDataSet1() {
		sdt = new SondeDataType();
		sdt.setDate("11/26/80");
		sdt.setTime("05:24:51");
		sdt.setTemp("22.69");
		sdt.setSpCond("183.0");
		sdt.setCond("175.0");
		sdt.setResist("5704.66");
		sdt.setSal("0.09");
		sdt.setPress("-0.00");
		sdt.setDepth("-0.00");
		sdt.setPH("8.22");
		sdt.setPhmV("-94.00");
		sdt.setODOSat("111.70");
		sdt.setODOConc("9.63");
		sdt.setTurbid("0.30");
		sdt.setBattery("10.10");
		list.add(sdt);
	}
	
	private void setDataSet2() {
		sdt = new SondeDataType();
		sdt.setDate("11/26/80");
		sdt.setTime("05:30:52");
		sdt.setTemp("22.69");
		sdt.setSpCond("183.0");
		sdt.setCond("175.0");
		sdt.setResist("5710.39");
		sdt.setSal("0.09");
		sdt.setPress("-0.00");
		sdt.setDepth("-0.00");
		sdt.setPH("8.22");
		sdt.setPhmV("-94.10");
		sdt.setODOSat("111.60");
		sdt.setTurbid("0.40");
		sdt.setBattery("10.00");
		list.add(sdt);
	}
	
	private void setDataSet3() {
		sdt = new SondeDataType();
		sdt.setDate("11/26/80");
		sdt.setTime("05:36:52");
		sdt.setTemp("22.70");
		sdt.setSpCond("183.0");
		sdt.setCond("175.0");
		sdt.setResist("5710.49");
		sdt.setSal("0.09");
		sdt.setPress("-0.00");
		sdt.setDepth("-0.00");
		sdt.setPH("8.22");
		sdt.setPhmV("-94.00");
		sdt.setODOSat("111.70");
		sdt.setTurbid("0.40");
		sdt.setBattery("10.10");
		list.add(sdt);
	}
	
	
	public static void main(String[] args) {
		SondeTestData testData = new SondeTestData();
		SondeDataContainer container;
		
		container = testData.getSondeTestData();
		
		System.out.println("size: " + container.getSondeData().size()); 
		System.out.println();
		
		for (int i = 0; i < container.getSondeData().size(); i++) {
			System.out.println("Date: " + container.getSondeData().get(i).getDate());
			System.out.println("Time: " + container.getSondeData().get(i).getTime());
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
}
