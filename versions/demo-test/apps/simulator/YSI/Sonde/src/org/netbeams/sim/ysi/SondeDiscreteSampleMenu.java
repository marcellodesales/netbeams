package org.netbeams.sim.ysi;

import java.util.ArrayList;

/**
 * 
 * @author Teresa L. Johnson <gamma.particle@gmail.com>
 *
 */
public class SondeDiscreteSampleMenu extends SondeMenu {

	private static final String DISCRETE_SAMPLE_MENU_TITLE = "------------Discrete sample------------";
	
	private String discreteMenuItem1 = "1-Start sampling";
	private String discreteMenuItem2 = "2-Sample interval=";
	private String discreteMenuItem3 = "3-File=";
	private String discreteMenuItem4 = "4-Site=";
	private String discreteMenuItem5 = "5-Open file";
	
	private ArrayList<String> discreteMenuItems;
	
	public SondeDiscreteSampleMenu() {
		super();
		this.initMenuItems();
	}
	
	public String getMenuTitle() {
		return DISCRETE_SAMPLE_MENU_TITLE;
	}
	
	public ArrayList<String> getMenuItems() {
		return discreteMenuItems;
	}
	
	public void initMenuItems() {
		discreteMenuItems = new ArrayList<String> ();
		discreteMenuItems.add(discreteMenuItem1);
		discreteMenuItems.add(discreteMenuItem2);
		discreteMenuItems.add(discreteMenuItem3);
		discreteMenuItems.add(discreteMenuItem4);
		discreteMenuItems.add(discreteMenuItem5);
	}

	public void displayMenu() {
		System.out.println(DISCRETE_SAMPLE_MENU_TITLE);
		System.out.println(discreteMenuItem1);
		System.out.println(discreteMenuItem2);
		System.out.println(discreteMenuItem3);
		System.out.println(discreteMenuItem4);
		System.out.println(discreteMenuItem5);
		System.out.println();
		System.out.println(super.getMenuPrompt());
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SondeDiscreteSampleMenu menu = new SondeDiscreteSampleMenu();
		
		menu.displayMenu();

	}

}
