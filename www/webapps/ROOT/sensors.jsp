<%@ page import="edu.sfsu.dmp.*" %>
<%@ page import="edu.sfsu.dmp.data.*" %>
<%@ page import="edu.sfsu.dmp.event.*" %>
<%@ page import="edu.sfsu.dmp.data.configuration.*" %>
<%@ page import="edu.sfsu.dmp.data.sensordata.*" %>
<%@ page import="edu.sfsu.netbeams.web.*" %>
<%@ page import="java.text.*" %>
<%@ page import="java.util.*" %>
<%@ page errorPage="error.jsp" %>


<% out.println(WebTemplate.getHeader(WebTemplate.SENSORS_PAGE)); %>



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
	}%>






<%
	String[] typeNames = {"SEABIRD", "WEATHER", "TEST"};
	Hashtable sensorTypes = new Hashtable();
	sensorTypes.put("WEATHER", "resources/weather");
	sensorTypes.put("SEABIRD", "resources/seabird");
	sensorTypes.put("TEST", "resources/test");
	sensorTypes.put("CTD", "resources/seabird");


	String sensorType = (String)request.getParameter("type");
	if (sensorType != null && sensorTypes.get(sensorType) == null) {
		sensorType = null;
	}


	DataManagementPlatform dmp = DataManagementPlatform.getInstance();
    Hashtable<DataProducer, SensorData> dataProducers = new Hashtable<DataProducer, SensorData>();
	for (DataProducer dp : dmp.getAllDataProducerPluginInstances(SensorData.class)) {
		SensorData data = (SensorData)dp.getData();
		if (data != null) {
			dataProducers.put(dp, data);
		}
	}

%>

<%


	String host = (String)request.getHeader("host");
	if (! host.toLowerCase().startsWith("www.netbeams.org")) {
		if (request.getQueryString() == null)
			response.sendRedirect("http://www.netbeams.org/sensors.jsp");
		else
			response.sendRedirect("http://www.netbeams.org/sensors.jsp?" + request.getQueryString());
	}

	
%>


<!--
<script src="http://maps.google.com/maps?file=api&v=1&key=ABQIAAAAapThvQLG2Qub0_WNgfq9ShTwM0brOpm-All5BF6PoaKBxRWWERQnHYZwHCJmihbM5uH6PNJUITmeUQ" type="text/javascript"></script>
-->

<!--
<script src="http://maps.google.com/maps?file=api&v=1&key=ABQIAAAAapThvQLG2Qub0_WNgfq9ShTBfUk9TZrBRaIteybtnU2KziHEpRSJNC-iFrlRKHTvO8BvW2aX8THfdQ" type="text/javascript"></script>
-->


<script src="http://maps.google.com/maps?file=api&v=1&key=ABQIAAAAapThvQLG2Qub0_WNgfq9ShQ6iu8mmcenJB0OTkBkzubpUspY6hQqrc29flnY3W9o69i5JGGnIEoy3A" type="text/javascript"></script>

<div style="font-weight:bold; font-size:20px">
<%
	try {
		out.println(dataProducers.size());
	} catch (Exception e) {
		out.println(e.getMessage());
	}
%>
</div>

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

			<br>

			<table border="0" width="100%" id="table3" cellspacing="0" cellpadding="0">
				<tr>
					<td class="navigation_title" background="resources/bottom_menu_bg.jpg" style='background-repeat: repeat-x;'>
						Sensor Types
					</td>
				</tr>
				<tr>
					<td class="navigation_body">
					<%
						out.println("<table border='0' cellspacing='0' cellpadding='5' width='100%' align='center'>");
						for (int i = 0; i < typeNames.length; i++) {
							out.println("<tr>");
							String name = typeNames[i];
							String image = (String)sensorTypes.get(name);
							out.println("<td style='text-align:right'><img border='0' src='" + image + ".jpg' /></td>");
							out.println("<td style='text-align:left'><a href='sensors.jsp?type=" + name + "'><b>" + name + "</b></a></td>");
							out.println("</tr>");
						}
						out.println("<tr><td colspan='2' align='center'><br><b><a href='sensors.jsp'>Show all sensors</a></b></td></tr>");
						out.println("</table>");
					%>
					</td>
				</tr>
			</table>

		</td>
		<td class="content_body">
			<div style='text-align:center' class='title'>Sensors</div>
			<div class='important'><b>The instruments are periodically offline as we are working to expand the system so please excuse the delayed measurement data.</b></div>
			<div id='mapArea' name='mapArea'>
				<p><b>Below is the map of the sensors. Click on sensor's icon to view available measurements. You can also drag the map with your mouse.</b><br>Note: Not all sensors will appear on the map. Please scroll down for the complete list of sensors.
				<div id="map_container" align="center">
					<table cellpadding="5" cellspacing="0" border="0" width="100%">
						<tr>
							<td width="50%" align="right">
								Provided by <a href='http://maps.google.com' target='_blank'>Google Map</a>
							</td>
						</tr>
					</table>
					<div id="map">
					</div>

					<div style='text-align:left; padding-left:20px'>
						<span style="padding-top:10px; padding-right:20px; font-weight:plain; text-align:left"><b>Note: map might not work properly with all browsers. <a href='browser_list.jsp'>See the list of supported browsers.</a></b></span>
					</div>
				</div>
			</div>

			<br><br><p style='text-align:left'><b>
			<%
				if (sensorType == null) {
					out.println("Below is the complete SORTED list of all the sensors starting with the most recently updated.");
				} else {
					out.println("Below is the complete SORTED list of all the <span class='important_info'>" + sensorType + "</span> sensors starting with the most recently updated.");
				}
			%>
			</b></p>
			<%

				Arrays.sort(dataProducers.values().toArray(), new Comparator() {
					public int compare (Object o1, Object o2) {

						try {
							SensorData s1 = (SensorData)o1;
							SensorData s2 = (SensorData)o2;

							long t1 = getSensorLatestTimestamp(s1);
							long t2 = getSensorLatestTimestamp(s2);

							String format = "yyyy-MM-dd HH:mm:ss.SSS";
							Date date1 = new Date(t1);
							Date date2 = new Date(t2);

							if (date1.before(date2)) return 1;
							else if (date1.after(date2)) return -1;

						} catch (Exception e) {
						}

						return 0;
					}
				});



				out.println("<table border='0' width='100%' cellspacing='0' cellpadding='3'>");
				int counter = 0;
				for (DataProducer dp : dataProducers.keySet()) {
					Plugin plugin = (Plugin)dp;

					try {
						SensorData sensorData = dataProducers.get(dp);
						
						if (sensorData.getMeasurements() != null && sensorData.getMeasurements().size() > 0) {
							Measurement[] measurements = sensorData.getMeasurements().toArray(new Measurement[0]);

		
							String sensorCategory = getSensorType(sensorData);
	
							if (sensorType == null || (sensorCategory != null && sensorCategory.equalsIgnoreCase(sensorType))) {
								counter++;
	
								Metadata location = getSensorLocation(sensorData);
	
								String bgColor = (counter % 2 == 0)? "#ffffff": "#e5e5e5";
								out.println("<tr bgcolor='" + bgColor + "'><td align='right' valign='top'>" + counter + "<br></td>");
								out.println("<td align='left' valign='top'>");
	
								out.println("<b>Sensor name:</b> " + plugin.getName());
								out.println("<br><b>Sensor description:</b> " + plugin.getDescription());
								if (location != null) {
									out.println("<br><b>Location:</b> Lat=" + location.getMetadata("latitude").getValue() + ", Lon=" + location.getMetadata("longitude").getValue() + ", Alt=" + location.getMetadata("altitude").getValue());
								} else {
									out.println("<br><b>Location:</b> Unknown");
								}
	
								out.println("<br><b>Measurements:</b> ");
	
	
								for (int j = 0; j < measurements.length; j++) {
									Measurement m = measurements[j];
									out.println("<a href='sensor_detail.jsp?sensorid=" + plugin.getId() + "&measurementname=" + m.getName() + "'>" + m.getName() + "</a>");
									if (j < measurements.length - 1) {
										out.println(", ");
									}
								}
	
	
	
								long timestamp = getSensorLatestTimestamp(dataProducers.get(dp));
								Date date = new Date(timestamp);
	
								// Subctract 7 hours from the sensor's last update date because the sensor's time is in GMT and the
								// system time is in PDT
								date.setTime(date.getTime() - 25200000);
	
	
	
								Date now = new Date();
	
								long diff = now.getTime() - date.getTime();
								String age = null;
	
								if (diff >= 0) {
									long seconds = diff / 1000;
	
									if (seconds < 60) age = seconds + " seconds ago";
									else if (seconds < 3600) age = (long)(seconds / 60) + " minutes ago";
									else if (seconds < 86400) age = (long)(seconds / 3600) + " hours ago";
									else if (seconds < 2592000) age = (long)(seconds / 86400) + " days ago";
									else if (seconds < 31104000) age = (long)(seconds / 2592000) + " months ago";
									else age = (seconds / 31104000) + " years ago";
								} else {
									age = "1 seconds ago";
								}
	
								out.println("<br><b>Last update:</b> " + date);
								if (age != null) {
									out.println("&nbsp;&nbsp;&nbsp;&nbsp;<span class='important_info'>" + age + "</span>");
								}
	
								out.println("<div align='right' style='text-align:right'><b><a href='sensor_detail.jsp?sensorid=" + plugin.getId() + "'>see detail&nbsp;</a></b> </div>");
								out.println("</td>");
								out.println("</tr>");
							}
						}
					} catch (Exception e) {}

				}
				out.println("</table>");



			%>

	 	  <div style="width:0; height:0; display:inline" id="panel"></div>
          <div id="metapanel"></div>
          <div id="permalink"></div>
          <div id="printheader"></div>
		</td>
	</tr>
</table>

<br><br>

<% out.println(WebTemplate.getFooter(WebTemplate.SENSORS_PAGE)); %>


<script type="text/javascript">
	if (window._load) {
		window._load();
	}
</script>

<script type="text/javascript">


	// Creates a marker whose info window displays the letter corresponding to
	// the given index
	function createMarker(point, image, html) {
	  var icon = new GIcon(baseIcon);
	  icon.image = image;
	  var marker = new GMarker(point, icon);

	  // Show this marker's index in the info window when it is clicked
	  GEvent.addListener(marker, "click", function() {
		marker.openInfoWindowHtml(html);
	  });

	  return marker;
	}


	var baseIcon = new GIcon();
	baseIcon.shadow = "http://www.google.com/mapfiles/shadow50.png";
	baseIcon.iconSize = new GSize(20, 34);
	baseIcon.shadowSize = new GSize(37, 34);
	baseIcon.iconAnchor = new GPoint(9, 34);
	baseIcon.infoWindowAnchor = new GPoint(9, 2);
	baseIcon.infoShadowAnchor = new GPoint(18, 25);


	var map = new GMap(document.getElementById("map"));
	map.addControl(new GMapTypeControl());
	map.addControl(new GLargeMapControl());
	map.centerAndZoom(new GPoint(-122.47778, 37.81972), 8);

	var point;
	var marker;

	<%



		for (DataProducer dp : dataProducers.keySet()) {
			
			try {
				SensorData sensorData = dataProducers.get(dp);
				Metadata location = getSensorLocation(sensorData);

	
				if (location != null) {
					Object lat = location.getMetadata("latitude", true, true).getValue();
					Object lon = location.getMetadata("longitude", true, true).getValue();
	
					String category = getSensorType(sensorData);
					if (category == null) {
						category = "TEST";
					}
	
					String image = (String)sensorTypes.get(category.toUpperCase());
	
					
					if (sensorType == null || sensorType.equalsIgnoreCase(category)) {
						
						
						String html = "<table width='250px' border='0' cellspacing='0' cellpadding='0'><tr align='center'><td class='title' valign='top' align='center' style='padding-bottom:10px; line-height:1.5'>Current Condition at<br /><span class='important_info'>" + ((Plugin)dp).getName() + "</span></td></tr><tr><td><table width='100%' border='0px' class='measurement_row' bgcolor='#ffffff' cellspacing='0' cellpadding='0' align='center'>";
	
						for (Measurement measurement : sensorData.getMeasurements()) {
							html = html + "<tr width='100%'><td align='left' valign='top' style='border-bottom:solid 1px #cccccc'><span class='measurement_name'>" + measurement.getName() + "&nbsp;</span></td><td align='right' valign='top' style='border-bottom:solid 1px #cccccc'><span class='measurement_value'>" + measurement.getData().getValue() + "</span><span class='measurement_unit'>&nbsp;" + measurement.getUnit() + "</span></td></tr>";
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
							age = "1 seconds ago";
						}
	
						SimpleDateFormat dateFormat = new SimpleDateFormat("M/d/yy hh:mm:ss a z");
	
						html = html + "</table></td></tr><tr><td><table width='100%' align='center' border='0px' class='measurement_row' bgcolor='#ffffff' cellspacing='0' cellpadding='0' style='background-color:#EEEEEE'><tr><td align='left' valign='top'><span class='measurement_name'>Last update:&nbsp;</span></td><td align='right' valign='top'><span class='measurement_value'>" + age + "</span></td></tr><tr><td colspan='2' align='right' style='line-height:1.5;' valign='top'>" + dateFormat.format(date) + "</td></tr></table></td></tr><tr class='measurement_row'><td align='right' valign='bottom' style='padding-top:10px'><a href='sensor_detail.jsp?sensorid=" + ((Plugin)dp).getId() + "'><b>See detail</b></a></td></tr></table>";
	
	
						out.println("point = new GPoint(" + lon + ", " + lat + ");");
						out.println("marker = createMarker(point, \"" + image + ".png\", \"" + html + "\");");
						out.println("map.addOverlay(marker);");
	
	
					}
	
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	%>

</script>








