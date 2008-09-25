<%@ page import="edu.sfsu.netbeams.web.*" %>

<% out.println(WebTemplate.getHeader(WebTemplate.RESOURCES_AND_LINKS_PAGE)); %>

	<table border="0" width="100%" id="table2" cellspacing="0" cellpadding="0">
		<tr>
			<td class="navigationBar">
			<table border="0" width="250px" id="table3" cellspacing="0" cellpadding="0">
				<tr>
					<td class="navigation_title" background="resources/bottom_menu_bg.jpg" style='background-repeat: repeat-x;'>
						Resources And links
					</td>
				</tr>
				<tr>
					<td class="navigation_body">
					<a href="#resources">Resources</a><br>
					<a href="#links">Links</a><br>
					<a href="#agumeeting">AGU Ocean Science Meeting Resources</a><br>
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
			<td class="content_body">
				<p class="title"><a name='resources' />Resources</p>
				<ul>
					<img src='resources/bullet.gif'>&nbsp;&nbsp;<a href="docs/NetBEAMS.pdf" target='_blank'>NetBEAMS Architecture Proposal</span></a><br><br>
					<img src='resources/bullet.gif'>&nbsp;&nbsp;<a href="docs/NetBEAMSUseCases.doc" target='_blank'>NetBEAMS Usecases</a><br><br>
					<img src='resources/bullet.gif'>&nbsp;&nbsp;<a href="docs/NetBEAMS_Access_Services_Overview.pdf" target='_blank'>NetBEAMS Access Services Overview</a><br><br>
					<img src='resources/bullet.gif'>&nbsp;&nbsp;<a href="http://www.nrss.org/resources/NRSS.pdf" target='_blank'>NRSS Documentation</a><br><br>
					<img src='resources/bullet.gif'>&nbsp;&nbsp;<a href="docs/JDDACTutorial.pdf" target='_blank'>JDDAC Tutorial</a><br><br>
					<img src='resources/bullet.gif'>&nbsp;&nbsp;<a href="docs/JDDAC_Sensor_Net_Intro.pdf" target='_blank'>JDDAC Sensor Network Introduction</a><br><br>
					<img src='resources/bullet.gif'>&nbsp;&nbsp;<a href="docs/regionalSensorReqs.doc" target='_blank'>Regional Sensor Requirements</a><br><br>
					<img src='resources/bullet.gif'>&nbsp;&nbsp;<a href="docs/16plus_rs232_012.pdf" target='_blank'>Sensor SB16 Plus's Manual</a><br><br>
					<img src='resources/bullet.gif'>&nbsp;&nbsp;<a href="docs/NetBeamsOverview.pdf" target='_blank'>NetBEAMS Overview by Brian Zambrano</a><br><br>
				</ul>
				<br>

				<p class="title"><a name='links' />Links</p>
				<ul>
				<img src='resources/bullet.gif'>&nbsp;&nbsp;<a href="https://netbeams.dev.java.net/" target='_blank'>NetBEAMS</a><br><br>
				<img src='resources/bullet.gif'>&nbsp;&nbsp;<a href="https://jddac.dev.java.net/" target='_blank'>JDDAC</a><br><br>
				<img src='resources/bullet.gif'>&nbsp;&nbsp;<a href="http://jddac.labs.agilent.com:8080/nrss/feed?mid=44" target='_blank'>NRSS feed Example</a><br><br>
				<img src='resources/bullet.gif'>&nbsp;&nbsp;<a href="http://www.seabird.com/" target='_blank'>Sea Bird Web Site</a><br><br>
				<img src='resources/bullet.gif'>&nbsp;&nbsp;<a href="http://cicore.mlml.calstate.edu/" target='_blank'>CI-CORE Web Site</a><br><br>
				</ul>
				<br>


				<p class="title"><a name='agumeeting' />AGU Ocean Sciences Meeting resources</p>
				<ul>
				<img src='resources/bullet.gif'>&nbsp;&nbsp;<a href="docs/brian_abstract.pdf" target='_blank'>Brian's abstract</a><br><br>
				<img src='resources/bullet.gif'>&nbsp;&nbsp;<a href="docs/brian_poster.ppt" target='_blank'>Brian's poster</a><br><br>
				<img src='resources/bullet.gif'>&nbsp;&nbsp;<a href="docs/bill_abstract.pdf" target='_blank'>Bill's abstract</a><br><br>
				<img src='resources/bullet.gif'>&nbsp;&nbsp;<a href="docs/bill_poster.jpg" target='_blank'>Bill's poster</a><br><br>
				</ul>
			</td>
		</tr>
	</table>


<%	out.println(WebTemplate.getFooter(WebTemplate.RESOURCES_AND_LINKS_PAGE)); %>





