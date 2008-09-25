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
					<a href="pictures.jsp"><b>Pictures</b></a><br>
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
		<td class="content_body">

<%

		String event = request.getParameter("event");


		if (event != null && event.trim().length() >= 1) {
			event = event.trim();


			// load event's title
			try {
				BufferedReader in = new BufferedReader(new FileReader(NetBEAMSConstants.WEB_ROOT + "/pictures/" + event + "/title.txt"));
				String title = in.readLine();

				out.println("<p class='title'>" + title + "</p>");
				in.close();
			} catch (Exception e) {}


			try {
				BufferedReader in = new BufferedReader(new FileReader(NetBEAMSConstants.WEB_ROOT + "/pictures/" + event + "/pictures_count.txt"));
				int pictureCount = Integer.parseInt(in.readLine());
				in.close();

				out.println("<table width='100%' border='0'>");
				for (int i = 0; i < pictureCount; i++) {
					if (i % 3 == 0) {
						out.println("<tr>");
					}

					out.println("<td align='center' valign='middle' style='padding:10px'>");
					out.println("<a href='picture.jsp?event=" + event + "&picturenumber=" + (i+1) + "'><img border='1px' src='pictures/" + event + "/thumbnails/thumb_" + (i+1) + ".jpg' /></a>");
					out.println("</td>");

					if (i != 0 && i + 1 % 3 == 0) {
						out.println("</tr>");
					}
				}
				out.println("</table>");

			} catch (Exception e) {
				throw e;
			}
		} else {

			%>
				<table width='100%' border='0'>
					<tr>
						<td align="center" colspan="2">
							<a href="pictures.jsp?event=agumeeting"><img src="pictures/agumeeting.jpg" border='0'></a>
						</td>
					</tr>
					<tr>
						<td>&nbsp;</td><td>&nbsp;</td>
					</tr>
					<tr>
						<td>&nbsp;</td><td>&nbsp;</td>
					</tr>
					<tr>
						<td align="center">
							<a href="pictures.jsp?event=javaone"><img src="pictures/javaone.jpg" border='0'></a>
						</td>
						<td>
							<a href="pictures.jsp?event=general"><img src="pictures/general.jpg" border='0'></a>
						</td>
					</tr>

				</table>
				<br><br>
			<%
		}


%>
		</td>
	</tr>
</table>



<% out.println(WebTemplate.getFooter(WebTemplate.ABOUT_US_PAGE)); %>
