




function AssignmentStatement (x, y, w, h, draggable, resizeOption) {
	this.variableName = null;
	this.expressionLayer = null;


	this.Statement = Statement;
	this.Statement(x, y, w, h, draggable, (resizeOption || Shape.RESIZE_WIDTH_ONLY));
}
dynapi.setPrototype('AssignmentStatement', 'Statement');




AssignmentStatement.prototype.setupUI = function () {
	
	this.setMinimumSize(100, 25);
	this.setMaximumSize(1000, 25);
	
	this.setBorder(1, "solid", "#D26900");
	this.setBgColor("#ffffff");





	this.variableName = "x";
	this.variableNameLayer = new DynLayer(null, null, null, 20);
	this.variableNameLayer.setHTML("<table valign='middle' align='right'><tr><td><b>" + this.variableName + "</b></td></tr></table>");
	this.variableNameLayer.setAutoSize(true, true);
	this.variableNameLayer.setAnchor({top:0, bottom:0, stretchV:"100%"});

	
	this.addChild(this.variableNameLayer);
	this.variableNameLayer.assignmentStatement = this;
	this.variableNameLayer.addEventListener({
		ondblclick: function (e) {
			var assignmentStatement = e.getSource().assignmentStatement;
			var varName = assignmentStatement.variableName;
			var newName = prompt("Please enter the variable name for the assignment statement", ((varName != null)? varName: ""));
			
			if (newName != null) {
				newName = newName.trim();
				assignmentStatement.setVariableName(newName);
			}
			
		}
	});
	
	
	
	
	this.assignmentImageLayer = new DynLayer("<img src='" + getResourcesPath() +  "assignment_arrow.gif'>", 0, 0, 22, 25);
	this.assignmentImageLayer.setAnchor({centerV:0});
	this.addChild(this.assignmentImageLayer);




	this.expressionLayer = new Expression();
	this.expressionLayer.setAnchor({top:0, bottom:0, stretchV:"100%", stretchH:"100%"});
	this.addChild(this.expressionLayer);





	this.addEventListener({
		onresize: function (e) {
			e.getSource().updateUI();
		}
	});


}






AssignmentStatement.prototype.updateUI = function () {
	this.variableNameLayer.setLocation(10, 0);
	this.assignmentImageLayer.setLocation(this.variableNameLayer.getWidth() + this.variableNameLayer.getX(), 0);
	this.expressionLayer.setLocation(this.assignmentImageLayer.getWidth() + this.assignmentImageLayer.getX(), 0);
	this.expressionLayer.setAnchor({left:this.expressionLayer.getX(), right:10, strechH:"100%"});
}




AssignmentStatement.prototype.setVariableName = function (newName) {
	var nameRegex = "^[a-zA-Z_]([a-zA-Z_0-9]{0,100})$";

	if ((new RegExp(nameRegex, "ig")).test(newName)) {
		this.variableName = newName;
		this.variableNameLayer.setHTML("<table valign='middle'><tr><td><b>" + newName + "</b></td></tr></table>");		
		this.updateUI();
	} else {
		var message = "Invalid variable name '" + newName + "'. Below are the rules for creating variable names.";
		message = message + "\n    - The name MUST contain at least 1 character."
		message = message + "\n    - The name MUST starts with an underscore character ( _ ), OR an alpha character (a-z or A-Z)."					
		message = message + "\n    - After the first character, there can be any mixed number of underscore and alpha numeric characters (a-z, A-Z, 0-9)."
		message = message + "\n\nExample: _measurementName, measurementValue_5, sensor_0_name";

		alert(message);
	}
}



AssignmentStatement.prototype.setExpression = function (expression) {
	if (expression != null) {
		this.expressionLayer.setExpression(expression);
	} else {
		alert("ERROR: Expression can not be null.");
	}
}





AssignmentStatement.prototype.generatePythonCode = function (levelNumber) {
	try {
		
		if (this.variableName == null || this.variableName.trim().length <= 0)
			throw "Assignment Statement's variable name can not be NULL or empty";
			
			
		var expression = this.expressionLayer.getExpression();
		if (expression == null || expression.trim().length <= 0)
			throw "Assignment Statement's expression can not be NULL or empty";
			
		
		var code = this.variableName + " = " + expression;
		code = code.padLeft("  ", levelNumber);
		return code;

	} catch (e) {
		this.setSelected(true);
		throw e;
	}
	
	return null;
}





AssignmentStatement.prototype.serializeToXML = function (dom) {
	try {
		
		if (this.variableName == null || this.variableName.trim().length <= 0)
			throw "Assignment Statement's variable name can not be NULL or empty";
			
			
		var expression = this.expressionLayer.getExpression();
		if (expression == null || expression.trim().length <= 0)
			throw "Assignment Statement's expression can not be NULL or empty";
			
		
		// Use the class name as the tag name
		var element = dom.createElement("AssignmentStatement");
		element.setAttribute("x", this.getX());
		element.setAttribute("y", this.getY());
		element.setAttribute("w", this.getWidth());
		element.setAttribute("h", this.getHeight());

		element.setAttribute("variableName", this.variableName);
		element.setAttribute("expression", expression);
		return element;
		

	} catch (e) {
		this.setSelected(true);
		throw e;
	}
	
	return null;
}







