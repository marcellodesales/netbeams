<%@ page import="edu.sfsu.netbeams.web.*" %>
<%@ page import="java.io.*" %>
<%@ page errorPage="error.jsp" %>


<% out.println(WebTemplate.getHeader(WebTemplate.ABOUT_US_PAGE)); %>


<table border="0" width="100%" id="table2" cellspacing="0" cellpadding="0">
	<tr>
		<td class="navigationBar">
			<table border="0" width="250px" id="table3" cellspacing="0" cellpadding="0">
				<tr>
					<td class="navigation_title" background="resources/bottom_menu_bg.jpg" style='background-repeat: repeat-x;'>
						About Us
					</td>
				</tr>
				<tr>
					<td class="navigation_body">
					<a href="about_us.jsp">About NetBEAMS Project</a><br>
					<a href="staff.jsp">About NetBEAMS Staff</a><br>
					<a href="pictures.jsp">Pictures</a><br>
					<a href="browser_list.jsp"><b>List of supported browsers</b></a>
					</td>
				</tr>
			</table>
				<p>&nbsp;</p>
				<table border="0" width="250px" id="table3" cellspacing="0" cellpadding="0">
					<tr>
						<td class="navigation_title" background="resources/bottom_menu_bg.jpg" style='background-repeat: repeat-x;'>
							Related Info
						</td>
					</tr>
					<tr>
						<td class="navigation_body" style='text-align:center; padding:10'>
							<a href="http://www.cs.sfsu.edu" target='_blank'>SFSU Computer Science Dept</a><br>
							<a href="http://www.rtc.sfsu.edu" target='_blank'>Romberg Tiburon Center</a><br>
							<a href="http://cs.sfsu.edu/ccls" target='_blank'>Center for Computing for Life	Sciences</a><br>
							<a href="http://www.agilent.com/labs/" target='_blank'>Agilent Technologies</a><br>
							<a href="http://www.sun.com" target='_blank'>Sun Microsystems, Inc</a><br>
							<a href="http://cicore.mlml.calstate.edu/" target='_blank'>CICORE</a><br>
							<a href="http://jddac.dev.java.net" target='_blank'>JDDAC</a><br>
							<a href="http://jxta.dev.java.net" target='_blank'>JXTA</a><br>
						</td>
					</tr>
				</table>
		</td>
		<td class="content_body"><p class="title">List of supported browsers</p>


		<p>This web site make use of some new technologies such as Google map and ActiveX Object therefore not all browsers will work properly on this site. Below is the list of supported browsers. If your browser does not display the content from this web site properly, we recommend that you download one of the browsers from the list below.</p>


		<ul>
			<li><a href="http://www.microsoft.com/windows/ie/downloads/default.asp" target='_blank'>IE</a> 5.5+ (Windows)</li>
			<li><a href="http://www.mozilla.org/products/firefox/" target='_blank'>Firefox</a> 0.8+ (Windows, Mac, Linux)</li>
			<li><a href="http://channels.netscape.com/ns/browsers/download.jsp" target='_blank'>Netscape</a> 7.1+ (Windows, Mac, Linux)</li>
			<li><a href="http://www.mozilla.org/products/mozilla1.x/" target='_blank'>Mozilla</a> 1.4+ (Windows, Mac, Linux)</li>
			<li><a href="http://www.opera.com/download/" target='_blank'>Opera</a> 7.5+ (Windows, Mac, Linux)</li>
		</ul>




		</td>
	</tr>
</table>



<% out.println(WebTemplate.getFooter(WebTemplate.ABOUT_US_PAGE)); %>

