package org.netbeams.sim.ysi;

import java.util.ArrayList;

/**
 * 
 * @author Teresa L. Johnson <gamma.particle@gmail.com>
 *
 */
public class SondeUnattendedSampleMenu extends SondeMenu {
	
	private static final String UNATTENDED_SAMPLE_MENU_TITLE = "------------Unattended setup-----------";
	
	private String unattendedMenuItem1 = "1-Interval=";
	private String unattendedMenuItem2 = "2-Start date=";
	private String unattendedMenuItem3 = "3-Start time=";
	private String unattendedMenuItem4 = "4-Duration days=";
	private String unattendedMenuItem5 = "5-File=";
	private String unattendedMenuItem6 = "6-Site=";
	private String unattendedMenuItem7 = "7-Bat volts: 9.1";
	private String unattendedMenuItem8 = "8-Bat life 21.2 days";
	private String unattendedMenuItem9 = "9-Free mem 18.9 days";
	private String unattendedMenuItemA = "A-1st sample in 8.10 minutes";
	private String unattendedMenuItemB = "B-View params to log";
	private String unattendedMenuItemC = "C-Start logging";
	
	private ArrayList<String> unattendedMenuItems;
	
	public SondeUnattendedSampleMenu() {
		super();
		this.initMenuItems();
	}

	public ArrayList<String> getMenuItems() {
		return unattendedMenuItems;
	}

	public String getMenuTitle() {
		return UNATTENDED_SAMPLE_MENU_TITLE;
	}

	public void initMenuItems() {
		unattendedMenuItems = new ArrayList<String> ();
		unattendedMenuItems.add(unattendedMenuItem1);
		unattendedMenuItems.add(unattendedMenuItem2);
		unattendedMenuItems.add(unattendedMenuItem3);
		unattendedMenuItems.add(unattendedMenuItem4);
		unattendedMenuItems.add(unattendedMenuItem5);
		unattendedMenuItems.add(unattendedMenuItem6);
		unattendedMenuItems.add(unattendedMenuItem7);
		unattendedMenuItems.add(unattendedMenuItem8);
		unattendedMenuItems.add(unattendedMenuItem9);
		unattendedMenuItems.add(unattendedMenuItemA);
		unattendedMenuItems.add(unattendedMenuItemB);
		unattendedMenuItems.add(unattendedMenuItemC);
	}

	public void displayMenu() {
		System.out.println(UNATTENDED_SAMPLE_MENU_TITLE);
		System.out.println(unattendedMenuItem1);
		System.out.println(unattendedMenuItem2);
		System.out.println(unattendedMenuItem3);
		System.out.println(unattendedMenuItem4);
		System.out.println(unattendedMenuItem5);
		System.out.println(unattendedMenuItem6);
		System.out.println(unattendedMenuItem7);
		System.out.println(unattendedMenuItem8);
		System.out.println(unattendedMenuItem9);
		System.out.println(unattendedMenuItemA);
		System.out.println(unattendedMenuItemB);
		System.out.println(unattendedMenuItemC);
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SondeUnattendedSampleMenu menu = new SondeUnattendedSampleMenu();
		
		menu.displayMenu();
	}

}
