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
		<p align="center"><b><font color="#FFFFFF" size="5">Configure the sensor</font></b></td>
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
					String configurationXML = request.getParameter("configurationXML");
					int updateFreq = Integer.parseInt(request.getParameter("updateFreq"));

					SensorManagementPlatform smp = SensorManagementPlatform.getInstance();
					SensorManagementPlatformController controller = smp.getController();
					DataProducerPlugin plugin = controller.getDataProducerPlugin(dataProducerName);

					if (sensorName == null || sensorName.trim().length() <= 0) throw new Exception("Please provide a name for the sensor");
					if (sensorDescription == null || sensorDescription.trim().length() <= 0) throw new Exception("Please provide a description for the sensor");
					if (updateFreq <= 0) throw new Exception("The update frequency must be greater than 0.");

					if (plugin != null) {
					%>

					<form method="POST" action="add_sensor_submit2.jsp" name="sensorForm" id="sensorForm">

						<input type='hidden' name='sensorName' value='<%= sensorName %>' />
						<input type='hidden' name='sensorDescription' value='<%= sensorDescription %>' />
						<input type='hidden' name='dataProducerName' value='<%= dataProducerName %>' />
						<input type='hidden' name='updateFreq' value='<%= updateFreq %>' />


						<p><b>Please enter your configuration for the data producer '<%= dataProducerName %>'. The data you enter will be converted to an ARRAY of strings.
						Each line will be converted to ONE array element. The array will then be passed to the data producer you have selected. Each type of
						data producer can be implemented differently so please read the documentation on the data producer you have selected to learn how to
						configure it.<b></p>
						<textarea rows='10' cols='100' wrap='off' name='configuration' id='configuration'></textarea>

						<br><br>
						<center>
							<input type='submit' value='Submit' />
						</center>

					</form>

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








