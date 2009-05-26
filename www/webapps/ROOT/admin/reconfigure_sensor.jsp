<%@ page import="org.smf.smp.*" %>
<%@ page import="org.smf.smp.event.*" %>
<%@ page import="org.smf.smp.dp.*" %>
<%@ page import="org.dp.*" %>
<%@ page import="java.util.*" %>
<%@ page import="java.io.*" %>
<%@ page isErrorPage="true" %>





<%!

	public String arrayToString (String []array) {
		String result = "";

		for (int i = 0; i < array.length; i++) {
			result = result + array[i];
			if (i < array.length - 1) {
				result = result + "\n";
			}
		}


		return result;
	}

%>




<h1 align="center">NetBEAMS admin page</h1>
<p align="center">&nbsp;</p>
<table border="1" width="100%" id="table1">
	<tr>
		<td width="235" bgcolor="#000080" valign="top">
		<p align="center"><b><font color="#FFFFFF" size="5">Menu</font></b></td>
		<td bgcolor="#000080">
		<p align="center"><b><font color="#FFFFFF" size="5">Add Sensor</font></b></td>
	</tr>
	<tr>
		<td width="235">
		<p align="left">&nbsp;</p>
		<p align="center"><a href="data_producers.jsp">Show Data Producers</a></p>
		<p align="center"><a href="index.jsp">Show Sensors</a></p>
		<p align="center">&nbsp;</td>
		<td valign="top">

			<br><br>
			<form method="POST" action="update_sensor_configuration.jsp" name="sensorForm" id="sensorForm">
				<%
					Sensor sensor = null;
					String sensorID = null;

					try {

						sensorID = request.getParameter("sensorID");


						SensorManagementPlatform smp = SensorManagementPlatform.getInstance();
						SensorManagementPlatformController controller = smp.getController();

						if (sensorID == null || sensorID.trim().length() <= 0) throw new Exception("Please provide a sensor ID.");

						sensor = smp.getSensor(sensorID);
						if (sensor == null) {
							throw new Exception("Invalid Sensor id '" + sensorID + "'");
						}

					} catch (Exception e) {
						out.println("<h2>Error: " + e.getMessage() + "</h2>");
						out.println("<input type='button' onclick='history.back()' value='Go back'/>");
						e.printStackTrace();
					}

				%>


				<input type='hidden' name='sensorID' value='<%= sensorID %>' />


				<table border="0" width="100%" id="table1">
					<tr>
						<td width="130" align="right"><b>Name:</b> </td>
						<td><input type="text" size="30" name="sensorName" id="sensorName" value="<%= sensor.getName() %>"/> The name of the sensor. This name is not important but is required. It is only for your own reference.</td>
					</tr>

					<tr>
						<td width="130" align="right"><b>Description:</b> </td>
						<td><input type="text" size="50" name="sensorDescription" id="sensorDescription" value="<%= sensor.getDescription() %>" /> Description of the sensor. This is also not important but is required.</td>
					</tr>


					<tr>
						<td width="130" align="right"><b>Data Producer:</b> </td>
						<td><input type="text" readonly size="50" name="dataProducer" id="dataProducer" value="<%= sensor.getDataProducerPlugin().getName() %>" /></td>
					</tr>


					<tr>
						<td width="130" align="right"><b>Update Frequency:</b> </td>
						<td><input type="text" size="10" name="updateFreq" id="updateFreq" value="<%= (int)(sensor.getUpdateFrequency() / 1000) %>"  /> How often do you want the Data Producer to check for updated data. Enter number in seconds.</td>
					</tr>

					<tr>
						<td width="130" align="right" valign='top'><b>Configuration:</b> </td>
						<td><textarea name="configuration" id="configuration" rows='10' cols='100'><%= arrayToString(sensor.getConfigurationParameters()) %></textarea></td>
					</tr>

				</table>

				<br>
				<center><input type="submit" value="Update Configuration" /></center>

			</form>
		</td>
	</tr>
</table>






