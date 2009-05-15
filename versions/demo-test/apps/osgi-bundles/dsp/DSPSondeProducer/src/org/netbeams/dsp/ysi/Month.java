package org.netbeams.dsp.ysi;

import java.util.Hashtable;

public class Month {

	private Hashtable hashtable;
	
	public Month() {
		hashtable = new Hashtable();
		populateHash();
	};
	
	private void populateHash() {
		hashtable.put("Jan", new Integer(1));
		hashtable.put("Feb", new Integer(2));
		hashtable.put("Mar", new Integer(3));
		hashtable.put("Apr", new Integer(4));
		hashtable.put("May", new Integer(5));
		hashtable.put("Jun", new Integer(6));
		hashtable.put("Jul", new Integer(7));
		hashtable.put("Aug", new Integer(8));
		hashtable.put("Sep", new Integer(9));
		hashtable.put("Oct", new Integer(10));
		hashtable.put("Nov", new Integer(11));
		hashtable.put("Dec", new Integer(12));
	}
	
	public Object getMonth(String month) {
		return hashtable.get(month);
	}
	
	/*
	public static void main(String[] args) {
		Month month = new Month();
		
		System.out.println("December is: " + (Integer) month.getMonth("Dec"));
	}
	*/
}


