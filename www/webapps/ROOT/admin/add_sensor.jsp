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
			<p><b>Please provide information about the sensor you want to add.</b></p>
			<form method="POST" action="add_sensor_submit1.jsp" name="sensorForm" id="sensorForm">

				<table border="0" width="100%" id="table1">
					<tr>
						<td width="130" align="right"><b>Name:</b> </td>
						<td><input type="text" size="30" name="sensorName" id="sensorName" /> The name of the sensor. This name is not important but is required. It is only for your own reference.</td>
					</tr>

					<tr>
						<td width="130" align="right"><b>Description:</b> </td>
						<td><input type="text" size="50" name="sensorDescription" id="sensorDescription" /> Description of the sensor. This is also not important but is required.</td>
					</tr>

					<tr>
						<td width="130" align="right"><b>Data Producer:</b> </td>
						<td>
							<select size='1' name="dataProducerName" id="dataProducerName">
								<option selected value="">Select One</option>
								<%
									SensorManagementPlatform smp = SensorManagementPlatform.getInstance();
									SensorManagementPlatformController controller = smp.getController();


									DataProducerPlugin plugins[] = controller.getDataProducerPlugins();
									for (int i = 0; i < plugins.length; i++) {
										DataProducerPlugin plugin = plugins[i];
										out.println("<option value='" + plugin.getClassName() + "'>" + plugin.getName() + "</option>");
									}

								%>
							</select>

						Please select the Data Producer you want to use to get the data for the sensor.</td>
					</tr>

					<tr>
						<td width="130" align="right"><b>Update Frequency:</b> </td>
						<td><input type="text" size="10" name="updateFreq" id="updateFreq" /> How often do you want the Data Producer to check for updated data. Enter number in seconds.</td>
					</tr>

				</table>

				<br><br>
				<center><input type="submit" value="Submit" /></center>

			</form>
		</td>
	</tr>
</table>






