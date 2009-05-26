<%@ page import="org.smf.smp.*" %>
<%@ page import="org.smf.smp.event.*" %>
<%@ page import="org.smf.smp.dp.*" %>
<%@ page import="org.dp.*" %>
<%@ page import="org.kpi.*" %>
<%@ page import="java.util.*" %>
<%@ page import="java.io.*" %>
<%@ page import="net.java.jddac.jmdi.fblock.*" %>
<%@ page import="net.java.jddac.common.util.*" %>
<%@ page import="net.java.jddac.jmdi.transducer.*" %>
<%@ page import="net.java.jddac.common.type.*" %>
<%@ page import="org.kpi.*" %>
<%@ page isErrorPage="true" %>







<html>
<head>
<title>NetBEAMS</title>
<link href='../../resources/netbeams.css' rel='stylesheet' type='text/css' />

<script language='javascript'>

	var is_ie = false;
	if (navigator.appVersion.indexOf("MSIE") != -1) {
		is_ie = true;
	}
	
	var metadataCount = 2;


	
	function addMetadata () {
		try {
		
			if (metadataCount >= 100) {
				alert("Too many metadata. Maximum is 100.");
				return;
			}



			var metadataTable = document.getElementById("metadataTable").getElementsByTagName("tbody")[0];
			
			var row = document.createElement("TR");
			var col = document.createElement("TD");
	
			var input = null;
			if (is_ie) {
				input = document.createElement("<INPUT name='metadataName'>");
			} else {
				input = document.createElement("INPUT");
				input.name = "metadataName";
			}
			input.type = "text";
			input.style.width = "100%";
			input.id = "metadataName";			
			input.maxLength = 50;

			col.appendChild(input);
			row.appendChild(col);
			
			
			var col = document.createElement("TD");
			if (is_ie) {
				input = document.createElement("<INPUT name='metadataValue'>");
			} else {
				input = document.createElement("INPUT");
				input.name = "metadataValue";
			}
			input.type = "text";
			input.style.width = "100%";
			input.id = "metadataValue";			
			input.maxLength = 200;

			col.appendChild(input);
			row.appendChild(col);

			
			metadataTable.appendChild(row);
			
			metadataCount++;
			
		} catch (e) {
			alert(e.message || e);
		}
	}




	function addSelectedSensor () {
		var availableSensors = document.getElementById("availableSensors");
		var selectedSensors = document.getElementById("selectedSensors");
		
		if (availableSensors.selectedIndex >= 0) {
			var option = availableSensors.options[availableSensors.selectedIndex];
			availableSensors.remove(availableSensors.selectedIndex);
			if (is_ie) {
				selectedSensors.add(option);
			} else {
				selectedSensors.appendChild(option);
			}
		}
	}
	
	
	
	function removeSelectedSensor () {
		
		var availableSensors = document.getElementById("availableSensors");
		var selectedSensors = document.getElementById("selectedSensors");
		
		if (selectedSensors.selectedIndex >= 0) {
			var option = selectedSensors.options[selectedSensors.selectedIndex];
			selectedSensors.remove(selectedSensors.selectedIndex);
			if (is_ie) {
				availableSensors.add(option);
			} else {
				availableSensors.appendChild(option);
			}
		}
	}
	

	function selectAllSelectedSensors () {
		var values = "";
		var selectedSensors = document.getElementById("selectedSensors");
		var options = selectedSensors.options;
		for (var i = 0; i < options.length; i++) {
			values = values + options[i].value;
			if (i < options.length - 1) {
				values = values + ",";
			}
		}

		var sensors = document.getElementById("sensors");
		sensors.value = values;
	}
	
	


	// A sensor name can have spaces and other character to that NOT valid for variable
	// name so this function will convert all the invalid characters into "_"
	function convertSensorNameToVariableName (name) {
		var result = "";
		for (var i = 0; i < name.length; i++) {
			var c = name.charAt(i);
			if (c == '_' || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')) {
				if (c >= '0' && c <= '9' && i == 0) {
					// invalid because the name can not starts with a digit
					result = result + "_" + c;
				} else {
					// valid character
					result = result + c;
				}
			} else {
				// don't add invalid character to variable name unless it is a space or tab

				if (c == ' ' || c == '\t')
					result = result + "_";
			}
		}
		
		return result;
	}
	
	

	function openKPIEditor () {
		var ids = "";
		var names = "";
		var selectedSensors = document.getElementById("selectedSensors");
		var options = selectedSensors.options;
		for (var i = 0; i < options.length; i++) {
			ids = ids + options[i].value;
			names = names + convertSensorNameToVariableName(options[i].text);
			if (i < options.length - 1) {
				ids = ids + ",";
				names = names + ",";
			}
		}

		var win = window.open("KPI_Rule_Editor.html?sensorIDs=" + ids + "&sensorNames=" + names + "&dataURL=../../sdml.jsp", "_blank"); 
	}
	
	
	
	
	function setKPIRule (rule) {
		if (rule != null) {
			var txtKPIRule = document.getElementById("kpiRule");
			txtKPIRule.value = rule;
		}
	}


</script>

</head>

<body>


<%
	try {
		String action = request.getParameter("action");
		String currentKPIName = request.getParameter("currentKPIName");
	
		KPI kpi = null;
		String kpiName = null;
		String kpiDescription = null;
		int trigger = -1;
		Sensor[] kpiSensors = null;
		String kpiRule = null;
		String metadataName = null;
		String metadataUnit = null;
		String metadataLatitude = null;
		String metadataLongitude = null;
		String metadataAltitude = null;
		
		Hashtable kpiMetadata = null;

		
		

		if (currentKPIName == null) {
			out.println("<h1 align='center'><font size='+2'>Create a new KPI</font></h1>");
			kpiName = "";
			kpiDescription = "";
			trigger = 0;
			kpiSensors = new Sensor[0];
			kpiRule = "";
			metadataName = "";
			metadataUnit = "";
			metadataLatitude = "";
			metadataLongitude = "";
			metadataAltitude = "";
		} else {
			out.println("<h1 align='center'><font size='+2'>Reconfigure KPI</font></h1>");
			kpi = KPIManager.getInstance().getKPI(currentKPIName.trim());
			if (kpi == null) {
				throw new Exception("Invalid KPI name '" + currentKPIName + "'. KPI DOES NOT exist.");
			}
			
			kpiDescription = kpi.getDescription();
			kpiName = currentKPIName.trim();
			trigger = kpi.getTrigger();
			kpiSensors = kpi.getSensors();
			kpiRule = kpi.getRule();
			kpiMetadata = kpi.getMetadata();
			metadataName = (String)kpi.getMetadata().get("name");
			metadataUnit = (String)kpi.getMetadata().get("units");			
			metadataLatitude = (String)kpi.getMetadata().get("latitude");
			metadataLongitude = (String)kpi.getMetadata().get("longitude");
			metadataAltitude = (String)kpi.getMetadata().get("altitude");
		}
%>


<form method="POST" action="configure_kpi_submit.jsp" name="kpiForm" id="kpiForm" onsubmit='selectAllSelectedSensors()'>
<input name='action' id='action' type='hidden' value='<%= action %>'>
<input name='currentKPIName' id='currentKPIName' type='hidden' value='<%= currentKPIName %>'>
<input name='sensors' id='sensors' type='hidden' value=''>


<table border='0px' cellspacing='5px'>
	<tr>
		<td style='font-weight:bold' align='right'>Name:</td><td><input id='kpiName' name='kpiName' type='text' size='50' maxlength='100' value='<%= kpiName %>'>&nbsp;&nbsp;<i>A unique name for the new KPI.</i></td>
	</tr>
	<tr>
		<td style='font-weight:bold' align='right'>Description:</td><td><input id='kpiDescription' name='kpiDescription' type='text' size='100' maxlength='200' value='<%= kpiDescription %>'></td>
	</tr>
	<tr>
		<td style='font-weight:bold' align='right'>Trigger interval (s):</td><td><input id='trigger' name='trigger' type='text' size='10'  maxlength='10' value='<%= trigger %>'>&nbsp;&nbsp;<i>The number of SECONDS to wait in between each execution of the KPI rule.</i></td>
	</tr>
	<tr>
		<td style='font-weight:bold' align='right' valign='top'>Required Sensors:</td>
		<td>
			<font color='#0000dd'>Select the sensors that are required to compute the KPI and then 'ADD' them to the list on the right.</font>
			<table>
				<tr>
					<td>
						<select id='availableSensors' name='availableSensors' size='7' style='min-width:250px'>
						<%
							SensorManagementPlatform smp = SensorManagementPlatform.getInstance();
							Sensor sensors[] = smp.getSensors();
							for (int i = 0; i < sensors.length; i++) {
								Sensor sensor = sensors[i];
								boolean selected = false;
								for (int j = 0; j < kpiSensors.length; j++) {
									if (kpiSensors[j] == sensor) {
										selected = true;
										break;
									}
								}
								
								if (! selected) {
									out.println("<option value='" + sensor.getId() + "'>" + sensor.getName() + "</option>");
								}
							}
						%>
						</select>
					</td>
					<td valign='middle' align='center'>
						<input type='button' value='add' style='width:100px' onclick='addSelectedSensor()'><br>
						<input type='button' value='remove' style='width:100px' onclick='removeSelectedSensor()'>
					</td>
					<td>
						<select id='selectedSensors' name='selectedSensors' size='7' style='min-width:250px'>
						<%
							for (int i = 0; i < kpiSensors.length; i++) {
								out.println("<option value='" + kpiSensors[i].getId() + "'>" + kpiSensors[i].getName() + "</option>");
							}								
						%>
						</select>
					</td>
					<td width='100%'>
						&nbsp;
					</td>
				</tr>
			</table>
		</td>
	</tr>


	<tr>
		<td style='font-weight:bold' align='right' valign='top'>
			Rule:
		</td>
		<td><i>
			The rule to be executed to compute the KPI value. The rule must be written in Python and the rule MUST have a function name 'kpi'.</i><br>
			<textarea id='kpiRule' name='kpiRule' rows='10' cols='75' wrap='off'><%= kpiRule %></textarea><br>
			<input type='button' value='Open KPI Rule Editor' onclick='openKPIEditor()'><br><br><br><br>
		</td>
	</tr>
	<tr>
		<td>&nbsp;</td><td style='font-weight:bold; color:#0000dd'>The KPI value computed will be treated as a new measurement therefore it will require some metadata. Please enter the metadata for the KPI value. The 'name' and 'unit' are required.</td>
	</tr>
	<tr>
		<td>&nbsp;</td>
		<td>
			<table name='metadataTable' id='metadataTable' align='left' width='400px' cellpadding='2px'>
				<tr>
					<td align='center' width='200px'><b>Metadata Name</b></td>
					<td align='center' width='200px'><b>Metadata Value</b></td>
				</tr>
				<tr>
					<td><input name='metadataName' id='metadataName' type='text' readonly value='name' maxlength='50' style='width:100%'></td>
					<td><input name='metadataValue' id='metadataValue' type='text' maxlength='200' style='width:100%' value='<%= metadataName %>'></td>
				</tr>
				<tr>
					<td><input name='metadataName' id='metadataName' type='text' readonly value='units' maxlength='50' style='width:100%'></td>
					<td><input id='metadataValue' name='metadataValue' type='text' maxlength='200' style='width:100%' value='<%= metadataUnit %>'></td>
				</tr>
				<tr>
					<td><input name='metadataName' id='metadataName' type='text' readonly value='latitude' maxlength='50' style='width:100%'></td>
					<td><input id='metadataValue' name='metadataValue' type='text' maxlength='200' style='width:100%' value='<%= metadataLatitude %>'></td>
				</tr>
				<tr>
					<td><input name='metadataName' id='metadataName' type='text' readonly value='longitude' maxlength='50' style='width:100%'></td>
					<td><input id='metadataValue' name='metadataValue' type='text' maxlength='200' style='width:100%' value='<%= metadataLongitude %>'></td>
				</tr>
				<tr>
					<td><input name='metadataName' id='metadataName' type='text' readonly value='altitude' maxlength='50' style='width:100%'></td>
					<td><input id='metadataValue' name='metadataValue' type='text' maxlength='200' style='width:100%' value='<%= metadataAltitude %>'></td>
				</tr>
				
				<%
					if (kpiMetadata != null) {
						Enumeration e = kpiMetadata.keys();
						while (e.hasMoreElements()) {
							String name = (String)e.nextElement();
							String value = (String)kpiMetadata.get(name);
							
							if (! name.equalsIgnoreCase("name") && 
								! name.equalsIgnoreCase("units") && 
								! name.equalsIgnoreCase("latitude") && 
								! name.equalsIgnoreCase("longitude") && 
								! name.equalsIgnoreCase("altitude")) {
								out.println("<tr>");
								out.println("<td><input name='metadataName' id='metadataName' type='text' style='width:100%' value='" + name + "' maxlength='50'></td>");
								out.println("<td><input name='metadataValue' id='metadataValue' type='text' style='width:100%' value='" + value + "' maxlength='200' ></td>");
								out.println("</tr>");
							}
						}
					}
				%>
				
			</table>
		</td>
	</tr>
	<tr>
		<td>&nbsp;</td>
		<td align='left'>
			<input type='button' onclick='addMetadata()' value='Add Metadata'>
		</td>
	</tr>
	<tr><td colspan='2'>&nbsp;</td></tr>
	<tr>
		<td colspan='2' align='center'><input type='button' value='cancel' onclick='history.back()'>&nbsp;&nbsp;&nbsp;&nbsp;<input type='submit' value='submit'></td>
	</tr>
</table>

</form>


<%

	} catch (Exception e) {
//		e.printStackTrace();
		System.out.println("ERROR: " + e.getMessage());
		out.println("<p style='color:#dd0000; font-weight:bold'>ERROR: " + e.getMessage() + "</p>");
		out.println("<input type='button' onclick='history.back()' value='Go back'/>");
	}
%>


<br><br><br>

</body>
</html>












