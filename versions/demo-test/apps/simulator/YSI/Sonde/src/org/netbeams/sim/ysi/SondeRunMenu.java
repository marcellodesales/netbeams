package org.netbeams.sim.ysi;

import java.util.ArrayList;

/**
 * 
 * @author Teresa L. Johnson <gamma.particle@gmail.com>
 *
 */
public class SondeRunMenu extends SondeMenu {

	private static final String RUN_MENU_TITLE = "---------------Run setup---------------";
	
	private String runMenuItem1 = "1-Discrete sample";
	private String runMenuItem2 = "2-Unattended sample";
	
	private ArrayList<String> runMenuItems;
	
	public SondeRunMenu() {
		super();
		this.initMenuItems();
	};
	
	public String getMenuTitle() {
		return RUN_MENU_TITLE;
	}
	
	public ArrayList<String> getMenuItems() {
		return runMenuItems;
	}
	
	public void initMenuItems() {
		runMenuItems = new ArrayList<String> ();
		runMenuItems.add(runMenuItem1);
		runMenuItems.add(runMenuItem2);
	}

	public void displayMenu() {
		System.out.println(RUN_MENU_TITLE);
		System.out.println(runMenuItem1);
		System.out.println(runMenuItem2);
		System.out.println();
		System.out.println(super.getMenuPrompt());
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SondeRunMenu menu = new SondeRunMenu();
		
		menu.displayMenu();
	}

}
