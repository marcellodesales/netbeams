<%@ page import="org.smf.smp.*" %>
<%@ page import="org.smf.smp.event.*" %>
<%@ page import="org.smf.smp.dp.*" %>
<%@ page import="org.dp.*" %>
<%@ page import="java.util.*" %>
<%@ page import="java.io.*" %>
<%@ page import="org.jdom.*" %>
<%@ page import="org.jdom.input.*" %>
<%@ page import="org.jdom.output.XMLOutputter" %>
<%@ page import="org.jdom.output.Format" %>
<%@ page import="org.netbeams.web.*" %>
<%@ page isErrorPage="true" %>


<%
	try {

		String dataProducerClassName = request.getParameter("dataProducerClassName");

		SensorManagementPlatform smp = SensorManagementPlatform.getInstance();
		SensorManagementPlatformController controller = smp.getController();
		controller.addDataProducer(dataProducerClassName);




		SAXBuilder builder = new SAXBuilder();
		Document document = builder.build(new File(NetBEAMSConstants.DATA_PRODUCERS_CONFIGURATIONS));

		Element newElement = new Element("dataProducer");
		newElement.addContent(dataProducerClassName);

		Element root = document.getRootElement();
		root.addContent(newElement);



		// save the new data producer to the list of data producer
		PrintWriter output = new PrintWriter(new BufferedWriter(new FileWriter(NetBEAMSConstants.DATA_PRODUCERS_CONFIGURATIONS)));
		XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
		outputter.output(document, output);

		output.close();



		out.println("<h1>Data Producer added successfully</h1>");
		out.println("<a href='data_producers.jsp'>View the list of Data Producer</a>");

	} catch (Exception e) {
		out.println("<h2>Error: " + e.getMessage() + "</h2>");
		out.println("<input type='button' onclick='history.back()' value='Go back'/>");
		NetBEAMSConstants.logs(e.getMessage());
	}
%>








