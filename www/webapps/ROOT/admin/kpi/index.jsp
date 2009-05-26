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
<%@ page isErrorPage="true" %>





<%

/*
	StartupInit.destroyObject("Memory");
	StartupInit.destroyObject("MemoryTransducer");


	StartupInit.createObject("net.java.jddac.jmdi.transducer.MemoryUsage", "Memory", true);
	
	ArgArray configuration = new ArgArray();
	ArgArray temp = new ArgArray();
	temp.put("BasicTransducerBlock.transducerName", "Memory");
	configuration.add(temp);

	temp = new ArgArray();
	temp.put("BasicTransducerBlock.period", 10);
	configuration.add(temp);

	temp = new ArgArray();
	temp.put("BasicTransducerBlock.autoStart", true);
	configuration.add(temp);

	
	ArgArray pubPorts = new ArgArray();
	ArgArray myResult = new ArgArray();
	ArgArray topic = new ArgArray();
	topic.put("topic", "storeMeasurement");
	myResult.put("result", topic);
	pubPorts.put("FunctionBlock.pubPorts", myResult);
	configuration.add(pubPorts);
	
	
	System.out.println("\n\n\n");
	System.out.println(configuration);
	
	
	BasicTransducerBlock block = (BasicTransducerBlock)StartupInit.createAndConfig("net.java.jddac.jmdi.fblock.BasicTransducerBlock", "MemoryTransducer", configuration, true);




	System.out.println("\n\n\n");
	Vector objects = NCAPBlock.getNCAPBlock().getAllObjects();
	for (int i = 0; i < objects.size(); i++) {
		System.out.println(objects.elementAt(i));
	}	
*/


%>



<html>
<head>
<title>NetBEAMS</title>
<link href='../../resources/netbeams.css' rel='stylesheet' type='text/css' />
</head>

<body>


<h1 align="center"><font size="+2">KPI admin page</font></h1>
	<table border="0px" width="100%" id="table1">
		<tr style='background:#0000aa; color:#ffffff; font-weight:bold; font-size:130%;'>
			<td width="20px">&nbsp;</td>
			<td width="200px" align="center">KPI Name</td>
			<td width='100px' align="center">Trigger (s)</td>
			<td align="center">Description</td>
			<td width='100px' align="center">Data</td>
		</tr>
		<%

			KPIManager kpiManager = KPIManager.getInstance();
			KPI kpis[] = kpiManager.getAllKPIs();
			for (int i = 0; i < kpis.length; i++) {
				KPI kpi = kpis[i];
				out.println("<tr>");
				out.println("<td><input type='checkbox' /></td>" +
							"<td align='left'>" + "<a href='configure_kpi.jsp?action=edit&currentKPIName=" + kpi.getName() + "'>" + kpi.getName() + "</a></td>" +
							"<td align='center'>" + "<a href='configure_kpi.jsp?action=edit&currentKPIName=" + kpi.getName() + "'>" + kpi.getTrigger() + " seconds</td>" +
							"<td align='left'>" + "<a href='configure_kpi.jsp?action=edit&currentKPIName=" + kpi.getName() + "'>" + kpi.getDescription() + "</td>" +
							"<td align='center'>" + "<a href='kpi_data.jsp?kpiName=" + kpi.getName() + "'><img border='0px' src='../../resources/sdml.jpg'>");
				out.println("</tr>");
			}
		%>
	</table>

	<p>
	Delete selected KPI | <a href="configure_kpi.jsp?action=create">Add new KPI</a>
	</p>



</body>
</html>













