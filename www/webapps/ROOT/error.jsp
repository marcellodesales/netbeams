<%@ page import="edu.sfsu.netbeams.web.*" %>
<%@ page import="java.util.*" %>
<%@ page isErrorPage="true" %>


<%
	out.println(WebTemplate.getHeader(WebTemplate.SENSORS_PAGE));
	
	out.println("<p class='error_message'>" + exception.getMessage() + "</p>");
	StackTraceElement[] element = exception.getStackTrace();
	for (int i = 0; i < element.length; i++) {
		out.println(element[i].toString() + "<br>\n");
	}
	
	out.println(WebTemplate.getFooter(WebTemplate.SENSORS_PAGE));
	
%>



