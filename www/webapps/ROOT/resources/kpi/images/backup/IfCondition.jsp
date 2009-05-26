<%@ page import="java.io.*" %>
<%@ page import="java.awt.geom.*" %>
<%@ page import="java.awt.*" %>
<%@ page import="javax.swing.*" %>
<%@ page import="java.awt.image.*" %>
<%@ page import="javax.imageio.*" %>




<%!
	public void drawImage (Graphics2D g, int width, int height) {


	
		int[] x = new int[6];
		int[] y = new int[6];
	
		int halfWidth = width / 2;
		int tilt = 13;
		int titleHeight = 26;			// the height of the title layer
		int arrowLength = 30;			// the length of the arrow
		int arrowWidth = 5;				// the width of the arrow
		int arrowSpace = 5;				// the space between the arrow and the title layer
	
		int minHeight = 30;
		int minWidth = (arrowLength * 2) + (arrowSpace * 2) + (tilt * 2) + 2;
		
		if (height < minHeight || width < minWidth) {
			
			// ONLY draw the title layer
			x[0] = 1;
			y[0] = height / 2;
		
			x[1] = width / 2;
			y[1] = 1;
	
			x[2] = width - 1;
			y[2] = height / 2;
		
			x[3] = width / 2;
			y[3] = height - 1;
		
			g.setColor(new Color(235, 215, 255, 255));
			g.fillPolygon(x, y, 4);
	
			g.setColor(new Color(155, 0, 255, 255));
			g.drawPolygon(x, y, 4);
			
		} else {
	
			
			// FILL the middle content with WHITE color
			x[0] = 1;
			y[0] = titleHeight / 2 + 1;
	
			x[1] = halfWidth - 1;
			y[1] = titleHeight / 2 + 1;
	
			x[2] = halfWidth - 1;
			y[2] = height - 1;
	
			x[3] = 1;
			y[3] = height - 1;
	
			
			g.setColor(new Color(255, 255, 255, 255));
			g.fillPolygon(x, y, 4);
	
			g.setColor(new Color(155, 0, 255, 255));
			g.drawPolygon(x, y, 4);


			
			// FILL the middle content with WHITE color
			x[0] = halfWidth + 1;
			y[0] = titleHeight / 2 + 1;
	
			x[1] = width - 1;
			y[1] = titleHeight / 2 + 1;
	
			x[2] = width - 1;
			y[2] = height - 1;
	
			x[3] = 1 + halfWidth;
			y[3] = height - 1;
	
			
			g.setColor(new Color(255, 255, 255, 255));
			g.fillPolygon(x, y, 4);
	
			g.setColor(new Color(155, 0, 255, 255));
			g.drawPolygon(x, y, 4);

			
			
			
			
			// draw the title layer
			x[0] = 1 + arrowLength + arrowSpace;
			y[0] = 1 + titleHeight / 2;
		
			x[1] = 1 + arrowLength + arrowSpace + tilt;
			y[1] = 1;
	
			x[2] = width - 1 - arrowLength - arrowSpace - tilt;
			y[2] = 1;
		
			x[3] = width - 1 - arrowLength - arrowSpace;
			y[3] = 1 + titleHeight / 2;
	
			x[4] = width - 1 - arrowLength - arrowSpace - tilt;
			y[4] = 1 + titleHeight;
	
			x[5] = 1 + arrowLength + arrowSpace + tilt;
			y[5] = titleHeight + 1;
	
			g.setColor(new Color(235, 215, 255, 255));
			g.fillPolygon(x, y, 6);
	
			g.setColor(new Color(155, 0, 255, 255));
			g.drawPolygon(x, y, 6);
			
	
			
		}
		


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







