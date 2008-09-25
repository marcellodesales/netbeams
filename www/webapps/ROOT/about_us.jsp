<%@ page import="edu.sfsu.netbeams.web.*" %>

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
					<a href="about_us.jsp"><b>About NetBEAMS Project</b></a><br>
					<a href="staff.jsp">About NetBEAMS Staff</a><br>
					<a href="pictures.jsp">Pictures</a><br>
					<a href="browser_list.jsp">List of supported browsers</a>
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
			<td class="content_body"><p class="title">
			NetBEAMS Project</p>
			<p style="line-height:200%">
			<span style="font-size:7.5pt;line-height:200%;
font-family:Verdana;color:black">NetBEAMS extends the scalability, sensor 
			integration and data management capabilities of the
			<a style="color: #006699; text-decoration: none; text-underline: none; text-line-through: none" href="http://jddac.dev.java.net">
			Java Distributed Data Acquisition and Control (JDDAC)</a> 
			infrastructure. This project applies JDDAC to a significant, long 
			term, real-world sensor network application.</span></p>
			<p style="line-height:200%">
			<span style="font-size:7.5pt;line-height:200%;
font-family:Verdana;color:black">NetBEAMS significantly contributes to the 
			environmental monitoring capabilities of CICORE, the Center for 
			Integrative Coastal Observation, Research and Education. CICORE 
			ultimately plans to establish oceanographic monitoring along the 
			entire 1200 miles of California coastline.<br>
			<br>
			The NetBEAMS project interfaces a marine monitoring sensor array low 
			power wireless communication devices based on the JDDAC transducer 
			data models and interfaces. Communication is over a wide area 
			cellular network infrastructure. This significantly expands the 
			reach of existing measurement systems, which are tethered dock-side, 
			to allow them to be deployed at remote locations and offshore.<br>
			<br>
			The scope of this project includes wired and wireless sensor arrays 
			in various locations in San Francisco Bay. Measurements include 
			pertinent water quality and environmental data including 
			temperature, pressure, salinity, and turbitity.<br>
			<br>
			This project is a multidisciplinary collaboration between the JDDAC 
			Community, SUN Microsystems, Agilent Technologies, and San Francisco 
			State University Department of Computer Science, the Department of 
			Engineering and the San Francisco State&nbsp; University Romberg Tiburon 
			Center for Environmental Studies.<br>
			<br>
			San Francisco State University provides the application,&nbsp; academic 
			leadership and participation by top SFSU graduate students. Student 
			work is funded and will contribute to both professional preparation 
			and academic credits. The Romberg Tiburon Center will continue to 
			provide open public access to the real-time data obtained from the 
			sensor network as it does today for SF-BEAMS. This WWW site is 
			maintained by San Francisco State Computer Science Department.</span></p>
			<p style="line-height:200%">
			<span style="font-size:7.5pt;line-height:200%;
font-family:Verdana;color:black">Funding is provided through a private grant 
			from Agilent Technologies matched by in-kind contributions from Sun 
			Microsystems. </span></p>
			<p align="right">
			<a href="http://netbeams.dev.java.net">Read more about the NetBEAMS
			project.</a><p>&nbsp;</td>
		</tr>
	</table>



<% out.println(WebTemplate.getFooter(WebTemplate.ABOUT_US_PAGE)); %>


