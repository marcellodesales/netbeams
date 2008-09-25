/*
 * Created on May 29, 2005
 *
 */
package edu.sfsu.netbeams.web;


public class WebTemplate {
	
	public static final int HOME_PAGE = 0;
	public static final int ABOUT_US_PAGE = 1;
	public static final int SENSORS_PAGE = 2;
	public static final int NEWS_AND_EVENTS_PAGE = 3;
	public static final int RESOURCES_AND_LINKS_PAGE = 4;
	public static final int FORUMS_PAGE = 5;
	public static final int CONTACT_US_PAGE = 6;
	public static final int OTHER_PAGE = 7;
	
	// prevent from creating an instant of the class
	private WebTemplate () {}
	
	
	
	/**
	 * This method return the HTML code for the header of the web template. The header
	 * includes the Logo, Menu, Search bar.
	 * 
	 * @return String - the HTML code for the header
	 */
	public static String getHeader (int page) {
		
		String []offImages = {"home_off.jpg", "about_us_off.jpg", "sensors_off.jpg", "news_and_events_off.jpg", "resources_and_links_off.jpg", "forums_off.jpg", "contact_us_off.jpg"};
		String []onImages = {"home_on.jpg", "about_us_on.jpg", "sensors_on.jpg", "news_and_events_on.jpg", "resources_and_links_on.jpg", "forums_on.jpg", "contact_us_on.jpg"};
		String []links = {"index.jsp", "about_us.jsp", "sensors.jsp", "news_and_events.jsp", "resources_and_links.jsp", "forums.jsp", "contact_us.jsp"};
		
		String menuButtons = "";
		for (int i = HOME_PAGE; i <= CONTACT_US_PAGE; i++) {
			if (page == i) {
				menuButtons = menuButtons + "<img src='resources/menu_seperator_off_on.jpg' /><a href='" + links[i] + "'><img src='resources/" + onImages[i] + "' border='0'/></a>";
			} else {
				if (page == (i - 1)) {
					menuButtons = menuButtons + "<img src='resources/menu_seperator_on_off.jpg' border='0'/>";
				} else {
					menuButtons = menuButtons + "<img src='resources/menu_seperator.jpg' border='0'/>";
				}
				
				menuButtons = menuButtons + "<a href='" + links[i] + "'><img src='resources/" + offImages[i] + "' border='0' /></a>";				
			}
		}

		if (page == CONTACT_US_PAGE) {
			menuButtons = menuButtons + "<img src='resources/menu_seperator_on_off.jpg' border='0' />";
		} else {
			menuButtons = menuButtons + "<img src='resources/menu_seperator.jpg' border='0' />";
		}
		
		StringBuffer header = new StringBuffer("");
		
		header = header.append("<html>" + "\n");
		header = header.append("<head></head>" + "\n");
		header = header.append("<title>NetBEAMS</title>" + "\n");
		header = header.append("<link href='resources/netbeams.css' rel='stylesheet' type='text/css' />" + "\n");
		header = header.append("<body style='background-image: url(\"resources/bg.gif\")'>" + "\n");
		header = header.append("	<div align='center'>" + "\n");
		header = header.append("		<table border='0' width='800' id='table1' cellspacing='0' cellpadding='0' style='border-color: #000000; border-style: solid; border-width: 1px; padding: 0'>" + "\n");
		header = header.append("			<tr>" + "\n");
		header = header.append("				<td width='100%' height='100' bgcolor='#FFFFFF' valign='top' align='center'><a href='index.jsp'><img border='0' src='resources/banner.jpg'></a></td>" + "\n");
		header = header.append("			</tr>" + "\n");
/*		
		header = header.append("			<tr>" + "\n");
		header = header.append("				<td width='258' valign='bottom' align='right' style='padding-right: 10px; padding-bottom: 10px' bgcolor='#FFFFFF'>" + "\n");
		header = header.append("				<b>Search: </b> <input type='text' name='T1' size='25'></td>" + "\n");
		header = header.append("			</tr>" + "\n");
*/		
		header = header.append("			<tr>" + "\n");
		header = header.append("				<td style='padding:0' background='resources/button_bg.jpg' height='25' align='center' valign='top'>" + "\n");
		
		header = header.append(menuButtons + "</td>\n");

		header = header.append("			</tr>" + "\n");
		header = header.append("			<tr>" + "\n");
		header = header.append("				<td bgcolor='#FFFFFF' colspan='2' background='resources/body_bg.jpg' style='background-repeat: repeat-x; ' align='center' valign='top' height='30'>" + "\n");
		header = header.append("				&nbsp;</td>" + "\n");
		header = header.append("			</tr>" + "\n");
		header = header.append("			<tr>" + "\n");
		header = header.append("				<td bgcolor='#FFFFFF' colspan='2' align='center' valign='top'>" + "\n");

		return header.toString();
	}
	
	
	/**
	 * This method return the HTML code for the footer of the web template. The footer
	 * includes the Bottom Menu and the copy right.
	 * 
	 * @return String - the HTML code for the header
	 */
	public static String getFooter (int page) {
		StringBuffer footer = new StringBuffer("");
		
		footer = footer.append("		</td>" + "\n");
		footer = footer.append("		</tr>" + "\n");
		footer = footer.append("		<tr>" + "\n");
		footer = footer.append("			<td bgcolor='#FFFFFF' colspan='2' align='center' valign='top' height='30'>" + "\n");
		footer = footer.append("			&nbsp;</td>" + "\n");
		footer = footer.append("		</tr>" + "\n");
		footer = footer.append("		<tr>" + "\n");
		footer = footer.append("			<td background='resources/bottom_menu_bg.jpg' height='20' style='border-left-width: 1px; border-right-width: 1px; padding: 0; border-left-color:#CCCCCC; border-right-color:#CCCCCC' colspan='2'>" + "\n");
		footer = footer.append("			<p align='center'><a href='index.jsp'>Home</a>&nbsp;&nbsp; |&nbsp;&nbsp; " + "\n");
		footer = footer.append("			<a href='contact_us.jsp'>Contact us</a>&nbsp;&nbsp; |&nbsp;&nbsp;" + "\n");
		footer = footer.append("			<a href='terms.jsp'>Terms of use</a>&nbsp;&nbsp; |&nbsp;&nbsp;" + "\n");
		footer = footer.append("			<a href='privacy.jsp'>Privacy</a></td>" + "\n");
		footer = footer.append("		</tr>" + "\n");
		footer = footer.append("		<tr>" + "\n");
		footer = footer.append("			<td height='30' colspan='2' bgcolor='#FFFFFF'>" + "\n");
		footer = footer.append("			<p align='center'><a href='copyright.jsp'>copyright ©2005 NetBEAMS</a></td>" + "\n");
		footer = footer.append("		</tr>" + "\n");
		footer = footer.append("	</table>		" + "\n");
		footer = footer.append("</div>" + "\n");
		footer = footer.append("</body>" + "\n");
		footer = footer.append("</html>" + "\n");


		return footer.toString();
	}
}


