<%@ page import="edu.sfsu.netbeams.web.*" %>
<%@ page errorPage="error.jsp" %>


<% out.println(WebTemplate.getHeader(WebTemplate.NEWS_AND_EVENTS_PAGE)); %>


	<table border="0" width="100%" id="table2" cellspacing="0" cellpadding="0">
		<tr>
			<td style='padding:20px'>
			<p class="title"><a name='resources' />News and Events</a></p>
			<div align="center">
				<table border="0" width="100%" id="table3" cellspacing="0" cellpadding="3">
					<tr>
						<td colspan='2' align="center" bgcolor="#000080">
						<p align="left"><b><font color="#00FFFF">Monday February 20, 2006
						</font><font color="#FFFFFF"> - AGU Ocean Sciences Meeting in Hawaii from Feb 20-24, 2006</font></b>
						</td>
					</tr>
					<tr>
						<td valign='top' align='left'>The Computer Science Department of San Francisco State University sent two graduate students,
						Brian Zambrano and Bill Huynh, to an Ocean Sciences Meeting in Hawaii. Brian and Bill will be doing a poster session on
						NetBEAMS at the meeting. Brian will be talking about the overview of NetBEAMS and Bill will be talking about Sensor
						Management Framework.
						<br><br><h1><a href="resources_and_links.jsp#agumeeting">See Brian and Bill's abstracts and posters.</a></h1>
						<h1><a href="http://www.agu.org/meetings/os06/" target="_blank">Learn more about the AGU meeting.</a></h1>
						<h1><a href="pictures.jsp?event=agumeeting"><img src="resources/agumeeting.jpg" border="0"/></a></h1>
						</td>
					</tr>
				</table>

				<table border="0" width="100%" id="table3" cellspacing="0" cellpadding="3">
					<tr>
						<td colspan='2' align="center" bgcolor="#000080">
						<p align="left"><b><font color="#00FFFF">Tuesday Jun 28,
						2005</font><font color="#FFFFFF"> - NetBeams receives Duke's Choice award at JavaOne!</font></b>
						</td>
					</tr>
					<tr>
						<td valign='top' align='left'><b>SFSU’s unique collaboration with tech industry wins JavaOne award.</b><br>
						"SAN FRANCISCO, June 28, 2005 - San Francisco State University and Silicon Valley leaders,
						Agilent Technologies and Sun Microsystems, have teamed up on a project that will improve the way
						critical environmental data is collected, stored, used and distributed..."
						<h1><a href='docs/NetBEAMSPRFinal.pdf' target="_blank">Read the complete Press Release</a></h1><p>&nbsp;
						</td>
						<td valign='top' align='right'>
							<img src='resources/duke_logo.jpg'>
						</td>
					</tr>
				</table>

				<table border="0" width="100%" id="table3" cellspacing="0" cellpadding="3">
					<tr>
						<td align="center" bgcolor="#000080">
						<p align="left"><b><font color="#00FFFF">Monday Jun 20,
						2005</font><font color="#FFFFFF"> - NetBeams to be demoed at JavaOne
						on June 27 - Jun 30 2005</font></b></td>
					</tr>
					<tr>
						<td>Please visit us at the JDDAC booth at Java.net arena
						in the pavilion.<br>
						<a href='docs/JDDACHandout.pdf' target='_blank'>JDDAC Handout</a><br>
						<a href='docs/NBHandout.pdf' target='_blank'>NetBEAMS Handout</a><br><br>
						For more information about JavaOne, please visit <a href='http://java.sun.com/javaone/sf/index.jsp' target="_blank">http://java.sun.com/javaone/sf/index.jsp</a><p>&nbsp;</td>
					</tr>
					<tr>
						<td bgcolor="#FFFFFF">&nbsp;</td>
					</tr>
				</table>
			</div>
			<p align='left'>
			&nbsp;</p>
			</td>
		</tr>
	</table>


<%	out.println(WebTemplate.getFooter(WebTemplate.NEWS_AND_EVENTS_PAGE)); %>
