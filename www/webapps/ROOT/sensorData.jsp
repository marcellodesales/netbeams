<%@ page import="edu.sfsu.dmp.*" %><%@ page import="edu.sfsu.dmp.data.sensordata.*" %><%@ page import="javax.xml.bind.*" %><%@ page errorPage="error.jsp" %><%
	try {
		DataManagementPlatform dmp = DataManagementPlatform.getInstance();
		String sensorID = (String)request.getParameter("sensorid");
		DataProducer dp = null;
		if (sensorID == null || sensorID.trim().length() == 0) {
			throw new Exception("Please provide a sensor ID.");
		}
		
		
		dp = dmp.getDataProducerPluginInstance(sensorID);
		if (dp == null) {
			throw new Exception("Invalid sensor ID, '" + sensorID + "'.");
		}

		SensorData sensorData = (SensorData)dp.getData();
		
		if (sensorData != null) {
			response.setContentType("application/xml");
			JAXBContext jc = JAXBContext.newInstance(SensorData.class);
			Marshaller ms = jc.createMarshaller();
			ms.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			ms.marshal(sensorData, out);
		}
	
	} catch (Exception e) {
		System.out.println(e.getMessage());
		throw e;
	}
%>



