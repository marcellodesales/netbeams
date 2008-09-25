<%@ page import="edu.sfsu.dmp.*" %>
<%@ page import="edu.sfsu.dmp.data.*" %>
<%@ page import="edu.sfsu.dmp.event.*" %>
<%@ page import="edu.sfsu.dmp.data.configuration.*" %>
<%@ page import="edu.sfsu.dmp.data.sensordata.*" %>
<%@ page import="edu.sfsu.netbeams.web.*" %>
<%@ page import="java.text.*" %>
<%@ page import="java.util.*" %>
<%@ page errorPage="error.jsp" %>





<%!public String getSensorType (SensorData sensorData) {
		try {
			for (Measurement m : sensorData.getMeasurements()) {
				if (m.getInternalMetadata() != null) {
					Metadata metadata = m.getInternalMetadata().getMetadata("category", true, true);						
					if (metadata != null) {
						return (String)metadata.getValue();
					}
				}
				
				if (m.getExternalMetadata() != null) {
					MetadataCollection def = sensorData.getGlobalMetadata(m.getExternalMetadata());
					if (def != null) {
						Metadata metadata = def.getMetadata("category", true, true);
						if (metadata != null) {
							return (String)metadata.getValue();
						}
					}
				}
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	
		return "CTD";
	}



	public long getSensorLatestTimestamp (SensorData sensorData) {
		long timestamp = 0;
		
		try {
			for (Measurement m : sensorData.getMeasurements()) {
				if (m.getTimestamp() > timestamp) {
					timestamp = m.getTimestamp();
				}
			}
		} catch (Exception e) {}
	
		return timestamp;
	}
	
	
	
	public Metadata getSensorLocation (SensorData sensorData) {
	
		for (Measurement m : sensorData.getMeasurements()) {
			if (m.getInternalMetadata() != null) {
				Metadata metadata = m.getInternalMetadata().getMetadata("location", true, true);
				if (metadata != null) {
					return metadata;
				}
			}
			
			if (m.getExternalMetadata() != null) {
				MetadataCollection def = sensorData.getGlobalMetadata(m.getExternalMetadata());
				if (def != null) {
					Metadata metadata = def.getMetadata("location", true, true);
					if (metadata != null) {
						return metadata;
					}
				}
			}
		}
		
		return null;
	}


	
	
	public long getSensorEarliestTimestamp (SensorData sensorData) {
		long timestamp = (new Date()).getTime();
		
		try {
			for (Measurement m : sensorData.getMeasurements()) {
				if (m.getTimestamp() < timestamp) {
					timestamp = m.getTimestamp();
				}
			}
		} catch (Exception e) {}
	
		return timestamp;
	}%>






<% out.println(WebTemplate.getHeader(WebTemplate.SENSORS_PAGE)); %>
<%

	try {
		DataManagementPlatform dmp = DataManagementPlatform.getInstance();
		String sensorID = (String)request.getParameter("sensorid");
		DataProducer dataProducer = null;
		if (sensorID == null || sensorID.trim().length() == 0) {
			throw new Exception("Please provide a sensor ID.");
		}
		
		
		dataProducer = dmp.getDataProducerPluginInstance(sensorID);
		if (dataProducer == null) {
			throw new Exception("Invalid sensor ID, '" + sensorID + "'.");
		}
	
		SensorData sensorData = (SensorData)dataProducer.getData();
	
		String measurementName = (String)request.getParameter("measurementname");
		Measurement measurement = null;
		if (measurementName != null) {
			measurement = sensorData.getMeasurement(measurementName, true);
			if (measurement == null) {
				throw new Exception("Invalid measurement name, '" + measurementName + "'.");
			}			
		}

%>


<script language="javascript">
	var agt=navigator.userAgent.toLowerCase();
	var is_ie     = ((agt.indexOf("msie") != -1) && (agt.indexOf("opera") == -1));
	var UPDATE_FREQUENCY = 30;					// make update every 30 seconds
	var timer = 30;
	var sensorID = "<%= ((Plugin)dataProducer).getId() %>";
	var measurementName = "<%= (measurement == null)? "NULL": measurement.getName()%>";



	function update () {

		document.getElementById("clock").innerHTML = timer;
		timer--;
		if (timer < 0) {
			timer = UPDATE_FREQUENCY;

			history.go();
		}

		setTimeout("update()", 1000);
	}


	function getChart (period) {
		if (measurementName != "NULL") {
			document.getElementById("chart").src = 'measurement_chart.jsp?sensorid=' + sensorID + '&measurementname=' + measurementName + '&period=' + period + '&width=550&height=250';
			document.getElementById("chartLink").href = 'measurement_chart.jsp?sensorid=' + sensorID + '&measurementname=' + measurementName + '&period=' + period + '&width=1000&height=500';
			document.getElementById("chartPeriod").value = period;
		}
	}


</script>


<%
	String period = request.getParameter("chartPeriod");
	
	if (period == null) period = "HOUR";

	out.println("<input type='hidden' name='chartPeriod' id='chartPeriod' value='" + period + "'>");
%>



<table border="0" width="100%" id="table2" cellspacing="0" cellpadding="0">
	<tr>
		<td class="navigationBar">
			<table border="0" width="100%" id="table3" cellspacing="0" cellpadding="0">
				<tr>
					<td class="navigation_title" background="resources/bottom_menu_bg.jpg" style='background-repeat: repeat-x;'>
						Sensors
					</td>
				</tr>
				<tr>
					<td class="navigation_body">
						<a href="sensors.jsp"><b>Sensors Map</b></a><br>
					</td>
				</tr>
			</table>


			<br><br>

			<table border="0" width="100%" id="table3" cellspacing="0" cellpadding="0">
				<tr>
					<td class="navigation_title" background="resources/bottom_menu_bg.jpg" style='background-repeat: repeat-x;'>
						View Measurements Detail
					</td>
				</tr>
				<tr>
					<td class="navigation_body">
					<%

						for (Measurement m : sensorData.getMeasurements()) {
							String name = m.getName();
							if (measurement != null && measurement.getName().equals(name))
								out.println("<a href='sensor_detail.jsp?sensorid=" + ((Plugin)dataProducer).getId() + "&measurementname=" + m.getName() + "'><b>" + name + "</b></a><br>");
							else
								out.println("<a href='sensor_detail.jsp?sensorid=" + ((Plugin)dataProducer).getId() + "&measurementname=" + m.getName() + "'>" + name + "</a><br>");
						}

						out.println("<br>");
						out.println("<b><a href='sensor_detail.jsp?sensorid=" + ((Plugin)dataProducer).getId() + "'>Back to sensor's detail</a></b><br>");
						out.println("<b><a href='sensors.jsp'>Back to sensor's map</a></b>");

					%>
					</td>
				</tr>
			</table>

		</td>
		<td class="content_body">
			<table width='100%' border='0' cellspacing='0' cellpadding='0' align='center'>
				<tr>
					<td align='left' valign='top' style='padding-right:10'>
						<%
						
							if (measurement != null) {
								String description = measurement.getName();
								
								out.println("<div class='title'>" + description + "</div>");
								
								out.println("<br><li><b>Sensor's raw data:" + "</b><br>");
								out.println("&nbsp;&nbsp;&nbsp;&nbsp;<a href='sensorData.jsp?sensorid=" + ((Plugin)dataProducer).getId() + "' target='_blank'><img border='0' src='resources/sdml.jpg'></a>");
							} else {
								out.println("<div class='title'>Sensor's detail</div>");
								out.println("<center><span class='important_info'>" + ((Plugin)dataProducer).getName() + "</span></center>");
								out.println("<li><b>Type:" + "</b><br>");
								out.println("&nbsp;&nbsp;&nbsp;&nbsp;" + getSensorType(sensorData));
								out.println("<br><li><b>Location:" + "</b><br>");
								Metadata location = getSensorLocation(sensorData);
								if (location != null) {
									out.println("&nbsp;&nbsp;&nbsp;&nbsp;<b>Lat:</b> " + location.getMetadata("latitude").getValue() + "<br>");
									out.println("&nbsp;&nbsp;&nbsp;&nbsp;<b>Lon:</b> " + location.getMetadata("longitude").getValue() + "<br>");
									out.println("&nbsp;&nbsp;&nbsp;&nbsp;<b>Alt:</b> " + location.getMetadata("altitude").getValue() + "<br>");
								}
								Date date = new Date(getSensorEarliestTimestamp(sensorData));
								date.setTime(date.getTime() - 25200000);
								out.println("<li><b>Start Date:" + "</b><br>");
								out.println("&nbsp;&nbsp;&nbsp;&nbsp;" + date);

								date = new Date(getSensorLatestTimestamp(sensorData));
								date.setTime(date.getTime() - 25200000);
								out.println("<li><b>Stop Date:" + "</b><br>");
								out.println("&nbsp;&nbsp;&nbsp;&nbsp;" + date);
								
								out.println("<br><li><b>Sensor's raw data:" + "</b><br>");
								out.println("&nbsp;&nbsp;&nbsp;&nbsp;<a href='sensorData.jsp?sensorid=" + ((Plugin)dataProducer).getId() + "' target='_blank'><img border='0' src='resources/sdml.jpg'></a>");
							}
						
						%>

					</td>
					<td width='350px' align='right' valign='top'>
						<table width='350px' border='0' cellspacing='0' cellpadding='0' align='center'>
							<tr>
								<td class='navigation_title' colspan='2' background="resources/bottom_menu_bg.jpg" style='background-repeat: repeat-x;'>
									Current Condition
								</td>
							</tr>

							<%

								int i = 0;
								for (Measurement m : sensorData.getMeasurements()) {
									String name = m.getName();
									if (measurement != null && measurement.getName().equals(name)) {
										out.println("<tr bgcolor=\"#E5FFE5\">");
									} else {
										out.println("<tr>");
									}
									out.println("<td class='measurement_row' style='padding:3px' class='measurement_col' align='left'><a href='sensor_detail.jsp?sensorid=" + ((Plugin)dataProducer).getId() + "&measurementname=" + name + "'><span class='measurement_name'>" + name + "</a></span>");
									out.println("</td>");
									out.println("<td class='measurement_row' id='td" + i + "' style='padding:3px' class='measurement_col' align='right'><span id='value" + i + "' name='value" + i + "' class='measurement_value' style='padding-right:5'>");
									out.println(m.getData().getValue() + "</span>");
									out.println("<span class='measurement_unit'>" + m.getUnit() + "</span>");
									out.println("</td>");
									out.println("</tr>");
									i++;
								}


								Date date = new Date(getSensorLatestTimestamp(sensorData));
								// Subctract 7 hours from the sensor's last update date because the sensor's time is in GMT and the
								// system time is in PDT
								date.setTime(date.getTime() - 25200000);

								
								Date now = new Date();

								long diff = now.getTime() - date.getTime();
								String age = "";

								if (diff >= 0) {
									long seconds = diff / 1000;

									if (seconds < 60) age = seconds + " seconds ago";
									else if (seconds < 3600) age = (long)(seconds / 60) + " minutes ago";
									else if (seconds < 86400) age = (long)(seconds / 3600) + " hours ago";
									else if (seconds < 2592000) age = (long)(seconds / 86400) + " days ago";
									else if (seconds < 31104000) age = (long)(seconds / 2592000) + " months ago";
									else age = (seconds / 31104000) + " years ago";
								} else {
									age = "Invalid time. Contact web admin.";
								}


								SimpleDateFormat dateFormat = new SimpleDateFormat("M/d/yy hh:mm:ss a z");
								out.println("<tr bgColor=\"#EFEFEF\">");
								out.println("<td style='padding:2px; line-height:1.5' class='measurement_col' align='left' valign='top'><span class='measurement_name'>Last Update</span>");
								out.println("</td>");
								out.println("<td style='padding:2px; line-height:1.5' class='measurement_col' align='right'  valign='top'>");
								out.println("<span class='measurement_value'>" + age + "</span><br />");
								out.println("<span>" + dateFormat.format(date) + "</span>");
								out.println("</td>");
								out.println("</tr>");

							%>
							<tr>
								<td class='measurement_row' bgcolor='#DDDDDD' colspan=2'>
									<b>Checking for update in:&nbsp;&nbsp;</b><span name='clock' id='clock' class='important_info'></span>&nbsp;seconds
								</td>
							</tr>

							<%
								out.println("<tr><td colspan='2' align='left'><b>Click on a measurement's name to view more detail.</b></td></tr>");

							%>

						</table>
					</td>
				</tr>

			</table>
			<%
				if (measurement != null) {
			%>
			<table>
					<tr>
						<td width='100%' style='padding-top:30px'>
							<p style='font-weight:bold'>Below is the chart of the measurement. (Click on the graph to enlarge).</p>
							<div align='center'><span class='button' onmouseover='style.cursor="pointer"' onclick='getChart("WEEK")'>Weekly Chart</span> &nbsp;| &nbsp;<span class='button' onmouseover='style.cursor="pointer"' onclick='getChart("DAY")'>Daily Chart</span> &nbsp;| &nbsp;<span class='button' onmouseover='style.cursor="pointer"' onclick='getChart("HOUR")'>Hourly Chart</span> &nbsp;| &nbsp;<span class='button' onmouseover='style.cursor="pointer"' onclick='window.open("measurement_charts.jsp?sensorid=<%= ((Plugin)dataProducer).getId() %>", "_blank")'>Other</span></div><br>
							<div align='center' style='width:100%'>
								<a name='chartLink' id='chartLink' href='' target='_blank'>
									<image name='chart' id='chart' border='1'/>
								</a>
							</div>
						</td>
					</tr>

			</table>
			<%
				}
			%>

		</td>
	</tr>
</table>



<%
	} catch (Exception e) {
		e.printStackTrace();
		throw e;
	}
%>


<script type="text/javascript">

	update();
	getChart(document.getElementById("chartPeriod").value);

</script>


<br><br><br><br>

<% out.println(WebTemplate.getFooter(WebTemplate.SENSORS_PAGE)); %>



