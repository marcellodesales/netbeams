<%@ page import="edu.sfsu.netbeams.web.*" %>
<%@ page import="java.io.*" %>
<%@ page import="javax.swing.*" %>
<%@ page errorPage="error.jsp" %>




<%!
	public String getNavigationBar (int pictureCount, int pictureNumber, String event) {

		String result = "<table border='0' cellpadding='0' cellspacing='0' align='center' width='500px%'><tr>";

		if (pictureNumber > 1) {
			result = result + "<td width='50px' align='left'><a href='picture.jsp?event=" + event + "&picturenumber=1'><img border='0' src='resources/first.gif' /></a></td>";
			result = result + "<td width='50px' align='left'><a href='picture.jsp?event=" + event + "&picturenumber=" + (pictureNumber - 1) + "'><img border='0' src='resources/prev.gif' /></a></td>";
		} else {
			result = result + "<td width='50px'>&nbsp</td>";
			result = result + "<td width='50px'>&nbsp</td>";
		}


		result = result + "<td align='center'>" + pictureNumber + " of " + pictureCount + "</td>";


		if (pictureNumber < pictureCount) {
			result = result + "<td width='50px' align='right'><a href='picture.jsp?event=" + event + "&picturenumber=" + (pictureNumber + 1) + "'><img border='0' src='resources/next.gif' /></a></td>";
			result = result + "<td width='50px' align='right'><a href='picture.jsp?event=" + event + "&picturenumber=" + pictureCount + "'><img border='0' src='resources/last.gif' /></a></td>";
		} else {
			result = result + "<td width='50px'>&nbsp</td>";
			result = result + "<td width='50px'>&nbsp</td>";
		}

		result = result + "</tr></table>";
		return result;
	}

%>


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
					<a href="staff.jsp">About NetBEAMS Staffs</a><br>
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
		try {
			String event = request.getParameter("event");

			// load event's title
			try {
				BufferedReader in = new BufferedReader(new FileReader(NetBEAMSConstants.WEB_ROOT + "/pictures/" + event + "/title.txt"));
				String title = in.readLine();

				out.println("<p class='title'>" + title + "</p>");
				in.close();
			} catch (Exception e) {}


			BufferedReader in = new BufferedReader(new FileReader(NetBEAMSConstants.WEB_ROOT + "/pictures/" + event + "/pictures_count.txt"));
			int pictureCount = Integer.parseInt(in.readLine());
			int pictureNumber = Integer.parseInt(request.getParameter("picturenumber"));
			in.close();


			String navigationBar = getNavigationBar(pictureCount, pictureNumber, event);

			ImageIcon image = new ImageIcon(NetBEAMSConstants.WEB_ROOT + "/pictures/" + event + "/picture_" + pictureNumber + ".jpg");
			int origWidth = image.getIconWidth();
			int origHeight = image.getIconHeight();

			int width = origWidth;
			int height = origHeight;

			if (width > 500) {
				width = 500;
				double scale = 500.0 / (double)origWidth;
				height = (int)(origHeight * scale);
			}


			// try loading comment. If comment does not exist, ignore it.
			String comment = "";
			try {
				in = new BufferedReader(new FileReader(NetBEAMSConstants.WEB_ROOT + "/pictures/" + event + "/comments/comment_" + pictureNumber + ".txt"));
				comment = in.readLine();
				in.close();
			} catch (Exception e) {}


			out.println(navigationBar);
			out.println("<div style='text-align:center; padding-top:10px; padding-bottom:5px;'><a href='pictures/" + event + "/picture_" + pictureNumber + ".jpg' target='_blank'><img  border='1px' src='pictures/" + event + "/picture_" + pictureNumber + ".jpg' width='" + width + "px' height='" + height + "px'/></a></div>");
			out.println("<div style='text-align:center; padding-top:5px; padding-bottom:10px;'>" + comment + "</div>");
			out.println(navigationBar);

		} catch (Exception e) {
			throw e;
		}
%>
		</td>
	</tr>
</table>



<% out.println(WebTemplate.getFooter(WebTemplate.ABOUT_US_PAGE)); %>




