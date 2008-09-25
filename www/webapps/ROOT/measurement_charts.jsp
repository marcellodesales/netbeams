<%@ page import="edu.sfsu.netbeams.web.*" %>
<%@ page import="org.smf.smp.*" %>
<%@ page import="org.smf.smp.event.*" %>
<%@ page import="org.smf.smp.sdml.*" %>
<%@ page import="java.util.*" %>
<%@ page import="java.text.*" %>
<%@ page errorPage="error.jsp" %>


<% out.println(WebTemplate.getHeader(WebTemplate.HOME_PAGE)); %>


<%
	SensorManagementPlatform smp = SensorManagementPlatform.getInstance();

	String sensorID = (String)request.getParameter("sensorid");
	Sensor sensor = null;
	if (sensorID == null || sensorID.trim().length() == 0) {
		throw new Exception("Please provide a sensor ID.");
	} else {
		sensor = smp.getSensor(sensorID);
		if (sensor == null) {
			throw new Exception("Invalid sensor ID, '" + sensorID + "'.");
		}
	}

	
	SDML sdml = sensor.getData().getSDML();
%>


<script type='text/javascript'>

	var currentType = "WEEK";
	var sensorID = "<%= sensor.getId() %>";
	var measurementNames = new Array();
	var MEASUREMENT_COUNT = <%= sdml.getAllMeasurements().length %>;

	<%
		{
			Measurement []measurements = sdml.getAllMeasurements();
			for (int i = 0; i < measurements.length; i++) {
				out.println("\tmeasurementNames[" + i + "] = \"" + measurements[i].getMetadata("name").getValueString() + "\";");
			}
		}
	%>


	function selectMeasurement (n) {
		if (n >= 0) {
			if (document.getElementById("measurement" + n).checked == true) {
				var chartArea = document.getElementById("chartArea");
				var newChart = document.createElement("img");
				newChart.setAttribute("border", "1px");
				newChart.setAttribute("name", "chart" + n);
				newChart.setAttribute("id", "chart" + n);

				newChart.setAttribute("src", "measurement_chart.jsp?sensorid=" + sensorID + "&measurementname=" + measurementNames[n] + "&width=550&height=250&period=" + currentType);
				chartArea.appendChild(newChart);
			} else {
				document.getElementById("chartArea").removeChild(document.getElementById("chart" + n));
			}
		}
	}

	function removeAllMeasurement () {
			for (var i = 0; i < MEASUREMENT_COUNT; i++) {
				try {
					document.getElementById("chartArea").removeChild(document.getElementById("chart" + i));
					document.getElementById("measurement" + i).checked = false;
				} catch (e) {}
			}
	}


	function selectAllMeasurement () {
		removeAllMeasurement();
		for (var i = 0; i < MEASUREMENT_COUNT; i++) {
			document.getElementById("measurement" + i).checked = true;
			selectMeasurement(i);
		}
	}



	function changeChartType (type) {
		if (type == "WEEK") {
			document.getElementById("txtWeek").innerHTML = "<b>Weekly</b>";
			document.getElementById("txtDay").innerHTML = "Daily";
			document.getElementById("txtHour").innerHTML = "Hourly";
		} else if (type == "DAY") {
			document.getElementById("txtDay").innerHTML = "<b>Daily</b>";
			document.getElementById("txtWeek").innerHTML = "Weekly";
			document.getElementById("txtHour").innerHTML = "Hourly";
		} else if (type == "HOUR") {
			document.getElementById("txtHour").innerHTML = "<b>Hourly</b>";
			document.getElementById("txtDay").innerHTML = "Daily";
			document.getElementById("txtWeek").innerHTML = "Weekly";
		}

		currentType = type;
		for (var i = 0; i < MEASUREMENT_COUNT; i++) {
			if (document.getElementById("measurement" + i).checked == true) {
				document.getElementById("chart" + i).setAttribute("src", "measurement_chart.jsp?sensorid=" + sensorID + "&measurementname=" + measurementNames[i] + "&width=550&height=250&period=" + currentType);
			}
		}
	}

</script>




<table border="0" width="100%" id="table2" cellspacing="0" cellpadding="0">
	<tr>
		<td class="navigationBar" style='width:200px'>
			<table border="0" width="200px" id="table3" cellspacing="0" cellpadding="0">
				<tr>
					<td class="navigation_title" background="resources/bottom_menu_bg.jpg" style='background-repeat: repeat-x;'>
						Chart Type
					</td>
				</tr>
				<tr>
					<td class="navigation_body" style='text-align:center; padding:10px'>
						<span name='txtWeek' id='txtWeek' onmouseover='style.cursor="pointer"' onclick='changeChartType("WEEK")' class='button'><b>Weekly</b></span><br>
						<span name='txtDay' id='txtDay' onmouseover='style.cursor="pointer"' onclick='changeChartType("DAY")' class='button'>Daily</span><br>
						<span name='txtHour' id='txtHour' onmouseover='style.cursor="pointer"' onclick='changeChartType("HOUR")' class='button'>Hourly</span><br>
					</td>
				</tr>
			</table>
			<table border="0" width="200px" id="table3" cellspacing="0" cellpadding="0">
				<tr>
					<td class="navigation_title" background="resources/bottom_menu_bg.jpg" style='background-repeat: repeat-x;'>
						Measurements
					</td>
				</tr>
				<tr>
					<td class="navigation_body" style='text-align:left; padding:10px'>
						<%
							Measurement[] measurements = sdml.getAllMeasurements();
							for (int i = 0; i < measurements.length; i++) {
								Measurement m = measurements[i];
								out.println("<label for='measurement" + i + "'><input type='checkbox' id='measurement" + i + "' name='measurement" + i + "' onclick='selectMeasurement(" + i + ")'> " + m.getMetadata("name").getValueString() + "</label><br>");
							}

							out.println("<p align='center'><span class='button' onmouseover='style.cursor=\"pointer\"' onclick='selectAllMeasurement()'> Select All</span><br>");
							out.println("<span class='button' onmouseover='style.cursor=\"pointer\"' onclick='removeAllMeasurement()'> Remove All</span></p>");
						%>
					</td>
				</tr>
			</table>
			<p></p>

		</td>
		<td class="content_body" align='center'>
		<div name='charArea' id='chartArea' align='center'>

		</div>
		</td>
	</tr>
</table>

<script type='text/javascript'>
	selectAllMeasurement();
</script>

<% out.println(WebTemplate.getFooter(WebTemplate.HOME_PAGE)); %>



