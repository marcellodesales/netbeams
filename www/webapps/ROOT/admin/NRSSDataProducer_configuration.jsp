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
		<td width="235" bgcolor="#000080" valign="top">
		<p align="center"><b><font color="#FFFFFF" size="5">Menu</font></b></td>
		<td bgcolor="#000080">
		<p align="center"><b><font color="#FFFFFF" size="5">Configure Sensor</font></b></td>
	</tr>
	<tr>
		<td width="235">
			<p align="left">&nbsp;</p>
			<p align="center"><a href="data_producers.jsp">Show Data Producers</a></p>
			<p align="center"><a href="index.jsp">Show Sensors</a></p>
			<p align="center">&nbsp;
		</td>
		<td valign="top">

			<%

				try {

					String sensorName = request.getParameter("sensorName");
					String sensorDescription = request.getParameter("sensorDescription");
					String dataProducerName = request.getParameter("dataProducerName");

					SensorManagementPlatform smp = SensorManagementPlatform.getInstance();
					SensorManagementPlatformController controller = smp.getController();
					DataProducerPlugin plugin = controller.getDataProducerPlugin(dataProducerName);

					if (sensorName == null || sensorName.trim().length() <= 0) throw new Exception("Please provide a name for the sensor");
					if (sensorDescription == null || sensorDescription.trim().length() <= 0) throw new Exception("Please provide a description for the sensor");

					if (plugin != null) {

						%>

							<input type='button' value='Add channel' />
						<%

					} else {
						throw new Exception("Invalid Data Producer. Please select another Data Producer.");
					}


				} catch (Exception e) {
					out.println("<h2>Error: " + e.getMessage() + "</h2>");
					out.println("<input type='button' onclick='history.back()' value='Go back'/>");
				}
			%>
		</td>
	</tr>
</table>






