<%@ page import="edu.sfsu.netbeams.web.*" %>
<%@ page import="java.io.*" %>
<%@ page import="java.util.*" %>
<%@ page import="java.text.*" %>
<%@ page import="java.awt.*" %>
<%@ page import="java.awt.image.*" %>
<%@ page import="javax.imageio.*" %>
<%@ page import="javax.imageio.stream.*" %>
<%@ page import="java.net.*" %>
<%@ page import="org.jfree.chart.*" %>
<%@ page import="org.jfree.chart.axis.*" %>
<%@ page import="org.jfree.chart.plot.*" %>
<%@ page import="org.jfree.chart.renderer.*" %>
<%@ page import="org.jfree.chart.renderer.xy.*" %>
<%@ page import="org.jfree.data.*" %>
<%@ page import="org.jfree.data.general.*" %>
<%@ page import="org.jfree.data.time.*" %>
<%@ page import="org.jfree.ui.*" %>
<%@ page import="org.smf.smp.*" %>
<%@ page import="org.smf.smp.event.*" %>
<%@ page import="org.smf.smp.sdml.*" %>

<%@ page errorPage="error.jsp" %>




<%!

	private String dateToString (Date date, String format) throws Exception {
		SimpleDateFormat dateFormat = new SimpleDateFormat (format);
		return dateFormat.format(date);
	}




	public Date stringToDate (String date, String format) throws Exception {
		SimpleDateFormat dateFormat = new SimpleDateFormat (format);
		return dateFormat.parse(date);
	}



%>




<%!
	public JFreeChart buildChart(TimeSeriesCollection dataset, String title, String xUnit, String yUnit, boolean endPoints, String dateFormat) throws Exception {
		// Create the chart


		JFreeChart chart = ChartFactory.createTimeSeriesChart(title, xUnit, yUnit, dataset, true, true, false);


		// Setup the appearance of the chart
//		chart.setBackgroundPaint(Color.white);
		XYPlot plot = chart.getXYPlot();
		plot.setBackgroundPaint(Color.lightGray);
		plot.setDomainGridlinePaint(Color.white);
		plot.setRangeGridlinePaint(Color.white);
		plot.setDomainCrosshairVisible(true);
		plot.setRangeCrosshairVisible(true);

		// Display data points or just the lines?
		if( endPoints ) {
			XYItemRenderer renderer = plot.getRenderer();
			if (renderer instanceof StandardXYItemRenderer) {
				StandardXYItemRenderer rr = (StandardXYItemRenderer) renderer;
				rr.setPlotShapes(true);
				rr.setShapesFilled(true);
				rr.setItemLabelsVisible(true);
			}
		}

		// Tell the chart how we would like dates to read
		DateAxis axis = (DateAxis) plot.getDomainAxis();
		axis.setDateFormatOverride(new SimpleDateFormat(dateFormat));
		return chart;
	}

%>


<%



	try {
		SensorManagementPlatform smp = SensorManagementPlatform.getInstance();

		String sensorID = (String)request.getParameter("sensorid");
		String measurementname = (String)request.getParameter("measurementname");
		if (sensorID != null && measurementname != null) {
			
			Sensor sensor = smp.getSensor(sensorID);
			if (sensor == null) throw new Exception("invalid sensor ID '" + sensorID + "'");
			
			
			int width = Integer.parseInt((String)request.getParameter("width"));
			int height = Integer.parseInt((String)request.getParameter("height"));


			// make sure dimension is either 550 x 250 or 1000 x 500. Default is 550 x 250
			if (!(width == 550 && height == 250) && ! (width == 1000 && height == 500)) {
				width = 550;
				height = 250;
			}



			// make sure the period is only one of the 3 periods, WEEK, DAY, or HOUR

			Hashtable periods = new Hashtable();
			periods.put("WEEK", new Integer(604800));
			periods.put("DAY", new Integer(86400));
			periods.put("HOUR", new Integer(3600));

			Integer period = null;

			try {
				period = (Integer)periods.get(request.getParameter("period"));
			} catch (Exception e) {}

			if (period == null)
				period = (Integer)periods.get("HOUR");



			SDML data = sensor.getData().getSDML(); 
			Measurement measurement = data.getMeasurementsByName(measurementname)[0];

			String format = "yyyy-MM-dd HH:mm:ss.SSS";
			Date timestamp = stringToDate(measurement.getMetadata("timestamp").getValueString(), format);
			File chartFile = new File(NetBEAMSConstants.WEB_ROOT + "/chart/" + measurementname.replaceAll(" ", "_") + "_" + timestamp.getTime() + "_" + period + "_" + width + "_" + height + ".jpg");



			
			// if chart does not exist, draw the chart and save it on the server

			if (! chartFile.exists()) {

				long start = (new Date()).getTime() - (period.intValue() * 1000);			// the start time of the first value in miliseconds
				
				Date begin = new Date();
				begin.setTime(start + 25200000);		// add 7 hours to start time because timestamp in archived data is in GMT 
				
				Date end = new Date();
				end.setTime(end.getTime() + 25200000);		// add 7 hours for GMT time

				
				

				SDML archivedData = sensor.getArchivedData(begin, end, measurementname).getSDML(); 
				Measurement[] measurements = archivedData.getAllMeasurements();
				

				if (measurements.length <= 0) {
					response.sendRedirect("resources/no_graph.jpg");
					return;
				}


				
				// create chart points
				int count = 0;
				TimeSeries timeSeries = new TimeSeries(measurementname + " for the last " + request.getParameter("period"), Second.class);

				for (int i = 0; i < measurements.length; i++) {
					Measurement m = measurements[i];
					Date time = stringToDate(m.getMetadata("timestamp").getValueString(), format);
					time.setTime(time.getTime() - 25200000);
					timeSeries.addOrUpdate(new Second(time), Double.parseDouble(m.getValues()[0].getValueString()));
				}





				// create a chart

				String dateFormat = "h:mm a";
				if (period == periods.get("WEEK"))
					dateFormat = "EEE ha";
				else if (period == periods.get("DAY"))
					dateFormat = "h:mm a";

				
				
				


				TimeSeriesCollection dataset = new TimeSeriesCollection();
				dataset.addSeries(timeSeries);


				JFreeChart chart = buildChart(dataset, measurementname, "Time in PDT", measurements[0].getMetadata("units").getValueString(), true, dateFormat);



				// output the chart image to the response
				response.setContentType("image/jpeg");

				OutputStream output = response.getOutputStream();

				BufferedImage myImage = chart.createBufferedImage(width, height);


				try {
					FileOutputStream fileOutputStream = new FileOutputStream(chartFile);
					ImageOutputStream imageOutputStream = ImageIO.createImageOutputStream(fileOutputStream);
					Iterator iter = ImageIO.getImageWritersByMIMEType("image/jpeg");
					if (iter.hasNext()) {
						ImageWriter writer = (ImageWriter)iter.next();
						ImageWriteParam iwp = writer.getDefaultWriteParam();
						iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
						iwp.setCompressionType("JPEG");
						iwp.setCompressionQuality(0.75F);
						writer.setOutput(imageOutputStream);
						IIOImage image = new IIOImage(myImage, null, null);
						writer.write(null, image, iwp);
					}

					fileOutputStream.close();
				} catch (Exception e) {
					System.out.println(e);
					throw e;
				}

			}


			// after drew the chart, redirect to the chart file
			response.sendRedirect("chart/" + chartFile.getName());




		} else {
			throw new Exception ("Missing sensor ID or measurement name.");
		}

	} catch (java.lang.IllegalStateException e) {
		// don't need to do anything
	} catch (NumberFormatException e) {
		throw new Exception ("Invalid or missing width, or height.");
	} catch (Exception e) {
		System.out.println(e.getMessage());
		throw e;
	}
%>





