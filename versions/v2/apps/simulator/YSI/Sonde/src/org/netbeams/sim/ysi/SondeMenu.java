package org.netbeams.sim.ysi;

import java.util.ArrayList;

/**
 * 
 * @author Teresa L. Johnson <gamma.particle@gmail.com>
 *
 */
public abstract class SondeMenu {

	private static final String MENU_ITEM_SELECTION_PROMPT = "Select option (0 for previous menu): ";
	
	public SondeMenu() {};
	
	public String getMenuPrompt() {
		return MENU_ITEM_SELECTION_PROMPT;
	}
	
	public abstract String getMenuTitle();
	
	public abstract ArrayList<String> getMenuItems();
	
	public abstract void initMenuItems();
}
