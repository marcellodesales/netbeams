<%@ page import="edu.sfsu.netbeams.web.*" %>


<% out.println(WebTemplate.getHeader(WebTemplate.HOME_PAGE)); %>



<table border="0" width="100%" id="table2" cellspacing="0" cellpadding="0">
	<tr>
		<td class="navigationBar" style='width:200px'>
			<table border="0" width="100%" id="table3" cellspacing="0" cellpadding="0">
				<tr>
					<td class="navigation_title" background="resources/bottom_menu_bg.jpg" style='background-repeat: repeat-x;'>
						Latest News
					</td>
				</tr>
				<tr>
					<td class="navigation_body" style='text-align:left; padding:10px'>
						<p><img src='resources/bullet.gif' border='0' />&nbsp;<b>Mon Feb 20, 2006</b><br>AGU Meeting in Hawaii<br><div style='text-align:right'><a href='news_and_events.jsp'>see detail</a></div></p>
						<p><img src='resources/bullet.gif' border='0' />&nbsp;<b>Tue Jun 28, 2005</b><br>NetBEAMS receives Duke's Choice Award at JavaOne!<br><div style='text-align:right'><a href='news_and_events.jsp'>see detail</a></div></p>
					</td>
				</tr>
				<tr>
					<td style='text-align:center; padding:0px'>
						<a href='news_and_events.jsp'><img src='resources/duke_logo.jpg' border='0'/></a>
					</td>
				</tr>
			</table>
			<p></p>

		</td>
		<td class="content_body"><p class="title">
			Welcome to NetBEAMS<br>Networked Bay Environmental Assessment Monitoring System</p>
			<div align="center">
				<table border="0" width="100%" id="table4" cellspacing="0" cellpadding="0">
					<tr>
						<td valign='top'>
						<img border="1" src="resources/netbeams_img0.jpg">
						<div style='text-align:center; line-height:1.5'><b>
							Seabird sensor mounted inside cage.</b>
						</div>
						</td>
						<td align="left" valign="top" style="padding-left: 10px">The NetBEAMS project team interfaces a marine monitoring sensor array low power wireless communication devices based on the JDDAC transducer data models and interfaces. Communication is over a wide area cellular network infrastructure. NetBEAMS significantly expands the reach of existing measurement systems, which are tethered dock-side, to allow them to be deployed at remote locations and offshore.<p>
						<b>Try it! To access sensor measurements go to <a href="sensors.jsp">Sensors</a> and
		click on any of the sensors to see the sensor's measurements.</b><p>
						NetBEAMS extends the scalability, sensor integration and data management capabilities of the Java Distributed Data Acquisition and Control (JDDAC) infrastructure. This project applies JDDAC to a significant, long term, real-world sensor network application. The scope of this project includes wired and wireless sensor arrays in various locations in San Francisco Bay. Measurements include pertinent water quality and environmental data including temperature, pressure, salinity, and turbitity. These measurements can be accessed via any WWW browser, and in the future via cell phones.</td>
					</tr>
				</table>
		</div>
			<div align="center">
				<table border="0" width="100%" id="table5" cellspacing="0" cellpadding="0">
					<tr>
						<td style="padding-right: 10px" align="left" valign="top" colspan="2"><br>NetBEAMS project significantly contributes to the environmental monitoring capabilities of
						<a href="http://cicore.mlml.calstate.edu/" target="_blank">CICORE</a>, the Center for Integrative Coastal Observation, Research and Education. CICORE ultimately plans to establish oceanographic monitoring along the entire 1200 miles of California coastline.<br><br></td>
					</tr>
					<tr>
						<td style="padding-right: 10px" align="left" valign="top"><p>NetBEAMS is a multidisciplinary collaboration between the
						<a href="http://jddac.dev.java.net" target="_blank">JDDAC</a> Community and
						<a href="http://www.cs.sfsu.edu" target="_blank">San Francisco State University Departments of Computer Science</a>, Electrical Engineering and the
						<a href="http://rtc.sfsu.edu/" target="_blank">Romberg Tiburon Center for Environmental Studies</a>,
						and <a href="http://www.sun.com" target="_blank">Sun Microsystems</a> and
						<a href="http://www.agilent.com/labs/" target="_blank">Agilent Technologies</a>
						Corporations, and <a href="http://www.jxta.org/" target="_blank">JXTA Community</a>. Funding is provided through a
						private grant from Agilent Technologies matched by in-kind
						contributions from Sun Microsystems.</p>
						<p>&nbsp;</td>
						<td valign="top" width='250px'>
							<img border="1" src="resources/netbeams_img1.jpg">
							<div style='text-align:center; line-height:1.5' width='100%'><b>Romberg Tiburon Center Pier in SF Bay for mounting sensors.</b>
							</div>
						</td>
					</tr>
				</table>
		</div>
		<p>&nbsp;</p>
		</td>
	</tr>
</table>



<% out.println(WebTemplate.getFooter(WebTemplate.HOME_PAGE)); %>

