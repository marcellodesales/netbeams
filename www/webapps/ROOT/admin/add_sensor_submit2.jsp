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

					String sensorName = request.getParameter("sensorName");
					String sensorDescription = request.getParameter("sensorDescription");
					String dataProducerName = request.getParameter("dataProducerName");
					String configuration = request.getParameter("configuration");
					int updateFreq = Integer.parseInt(request.getParameter("updateFreq"));


					SensorManagementPlatform smp = SensorManagementPlatform.getInstance();
					SensorManagementPlatformController controller = smp.getController();
					DataProducerPlugin plugin = controller.getDataProducerPlugin(dataProducerName);

					if (sensorName == null || sensorName.trim().length() <= 0) throw new Exception("Please provide a name for the sensor.");
					if (sensorDescription == null || sensorDescription.trim().length() <= 0) throw new Exception("Please provide a description for the sensor.");
					if (configuration == null || configuration.trim().length() <= 0) throw new Exception("Please enter some value for the configuration.");
					if (updateFreq <= 0 || updateFreq > 2592000) throw new Exception("The update frequency must be more than 1 seconds and less than 1 months.");


					if (plugin != null) {
						// break the configuration into an ARRAY
						String[] temp = configuration.trim().split("\n");
						Sensor sensor = controller.addSensor(plugin, temp, sensorName, sensorDescription, updateFreq * 1000, true);



						SAXBuilder builder = new SAXBuilder();
						Document document = builder.build(new File(NetBEAMSConstants.SENSORS_CONFIGURATIONS));

						Element sensorElement = new Element("sensor");
						sensorElement.setAttribute("id", sensor.getId());
						sensorElement.setAttribute("public", sensor.isPublic() + "");
						sensorElement.setAttribute("dataProducerName", dataProducerName);
						sensorElement.setAttribute("updateFrequency", (updateFreq * 1000) + "");



						Element sensorNameElement = new Element("name");
						sensorNameElement.addContent(sensorName);

						Element sensorDescriptionElement = new Element("description");
						sensorDescriptionElement.addContent(sensorDescription);


						Element configElement = new Element("configurations");
						configElement.addContent(configuration);

						sensorElement.addContent(sensorNameElement);
						sensorElement.addContent(sensorDescriptionElement);
						sensorElement.addContent(configElement);


						Element root = document.getRootElement();
						root.addContent(sensorElement);



						// save the new data producer to the list of data producer
						PrintWriter output = new PrintWriter(new BufferedWriter(new FileWriter(NetBEAMSConstants.SENSORS_CONFIGURATIONS)));
						XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
						outputter.output(document, output);

						output.close();


						out.println("<h2>Sensor added successfully.</h2><br>");
						out.println("<a href='index.jsp'>View the list of sensors.</h2>");
					} else {
						throw new Exception("Invalid Data Producer. Please select another Data Producer.");
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






