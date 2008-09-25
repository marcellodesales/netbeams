




function ReturnStatement (x, y, w, h, draggable, resizeOption) {
	this.expressionLayer = null;

	this.Statement = Statement;
	this.Statement(x, y, w, h, draggable, (resizeOption || Shape.RESIZE_WIDTH_ONLY));
}
dynapi.setPrototype('ReturnStatement', 'Statement');




ReturnStatement.prototype.setupUI = function () {
	
	this.setMinimumSize(100, 25);
	this.setMaximumSize(1000, 25);
	
	this.setBorder(1, "solid", "#D26900");
	this.setBgColor("#ffffff");





	this.returnLabelLayer = new DynLayer(null, null, null, 20);
	this.returnLabelLayer.setHTML("<table valign='middle' align='left'><tr><td><b>return</b></td></tr></table>");
	this.returnLabelLayer.setAnchor({top:0, bottom:0, stretchV:"100%"});

	
	this.addChild(this.returnLabelLayer);

	this.expressionLayer = new Expression();
	this.expressionLayer.setAnchor({top:0, bottom:0, stretchV:"100%", stretchH:"100%"});
	this.addChild(this.expressionLayer);



	this.addEventListener({
		onresize: function (e) {
			e.getSource().updateUI();
		}
	});
}






ReturnStatement.prototype.updateUI = function () {
	this.returnLabelLayer.setLocation(10, 0);
	this.returnLabelLayer.setWidth(50);
	this.expressionLayer.setLocation(this.returnLabelLayer.getWidth() + this.returnLabelLayer.getX(), 0);
	this.expressionLayer.setAnchor({left:this.expressionLayer.getX(), right:10, strechH:"100%"});
}




ReturnStatement.prototype.setExpression = function (expression) {
	if (expression != null) {
		this.expressionLayer.setExpression(expression);
	} else {
		alert("ERROR in ReturnStatement: Expression can not be null.");
	}
}




ReturnStatement.prototype.generatePythonCode = function (levelNumber) {

	var code = "return";
			
	var expression = this.expressionLayer.getExpression();
	if (expression != null && expression.trim().length > 0) {
		code = "return " + expression;
	}
		
	code = code.padLeft("  ", levelNumber);
	return code;	
}



ReturnStatement.prototype.serializeToXML = function (dom) {

	var element = dom.createElement("ReturnStatement");
	element.setAttribute("x", this.getX());
	element.setAttribute("y", this.getY());
	element.setAttribute("w", this.getWidth());
	element.setAttribute("h", this.getHeight());

			
	var expression = this.expressionLayer.getExpression();
	if (expression != null && expression.trim().length > 0) {
		element.setAttribute("expression", expression);
	}
		
	return element;
}



