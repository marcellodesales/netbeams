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
		<td width="235" bgcolor="#000080">
		<p align="center"><b><font color="#FFFFFF" size="5">Menu</font></b></td>
		<td bgcolor="#000080">
		<p align="center"><font size="5" color="#FFFFFF"><b>Here is the list of Data Producers</b></font></td>
	</tr>
	<tr>
		<td width="235">
		<p align="left">&nbsp;</p>
		<p align="center"><a href="data_producers.jsp">Show Data Producers</a></p>
		<p align="center"><a href="index.jsp">Show Sensors</a></p>
		<p align="center">&nbsp;</td>
		<td valign="top">
			<br><br>
			<table border="1" width="100%" id="table1">
				<tr>
					<td width="20px">&nbsp;</td><td width="200px" align="center"><b>Name</b></td><td align="center"><b>Description</b></td><td width="300px" align="center"><b>Class Name</b></td>
				</tr>
				<%
					SensorManagementPlatform smp = SensorManagementPlatform.getInstance();
					SensorManagementPlatformController controller = smp.getController();

					DataProducerPlugin plugins[] = controller.getDataProducerPlugins();
					for (int i = 0 ; i < plugins.length; i++) {
						DataProducerPlugin plugin = plugins[i];
						out.println("<tr>");
						out.println("<td><input type='checkbox' /></td>" +
									"<td align='left'>" + plugin.getName() + "</td>" +
									"<td align='left'>" + plugin.getDescription() + "</td>" +
									"<td align='left'>" + plugin.getClassName() + "</td>");
						out.println("</tr>");
					}
				%>
			</table>

			<p>
			Delete selected Data Producer | <a href="add_data_producer.jsp">Add new Data Producer</a>
			</p>
		</td>
	</tr>
</table>





