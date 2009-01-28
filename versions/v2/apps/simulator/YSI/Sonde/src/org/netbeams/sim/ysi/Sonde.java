package org.netbeams.sim.ysi;


/**
 * 
 * @author Teresa L. Johnson <gamma.particle@gmail.com>
 *
 */
public class Sonde {
	
	private static final String SONDE_PROMPT = "# "; 
	private SondeSerialComm serialComm;
	private SondeMenu sondeMenu;
	
	public Sonde() {
		sondeMenu = new SondeMainMenu();
		serialComm = new SondeSerialComm();
	};
	
	public String getSondPrompt() {
		return SONDE_PROMPT;
	}

	public void displayPrompt() {
		System.out.println(SONDE_PROMPT);
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Sonde sonde = new Sonde();
		
		sonde.displayPrompt();

	}

}
