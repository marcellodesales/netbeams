


function Expression () {
	this.DynLayer = DynLayer;
	this.DynLayer();
	
	
	
	this.expression = "";
	
	
	
	// create an INPUT FIELD for the user to enter the expression
	this.expressionLayer = new DynLayer();
	this.expressionLayer.setAnchor({left:0, right:20, top:0, bottom:0, stretchH:"100%", stretchV:"100%"});
	this.expressionLayer.setHTML("<table cellpadding='0px' cellspacing='0px' style='background-color:transparent; width:100%; height:100%;' valign='middle'><tr><td><input onfocus='this.select()' name='" + this.id + "InputField' id='" + this.id + "InputField' type='text' style='width:100%;' maxLength='1000' value='' onkeyup='this.currentValue = this.value'></td></tr></table>");	
	this.addChild(this.expressionLayer);
	
	
	this.helpCreateExpressionButton = new DynLayer("<table style='width:100%; height:100%' valign='middle'><tr><td><img src='"+ getResourcesPath() + "help.gif'></td></tr></table>", null, null, 22);
	this.helpCreateExpressionButton.setAnchor({right:2, top:0, bottom:0, stretchV:"100%"});
	this.helpCreateExpressionButton.setCursor("hand");
	this.addChild(this.helpCreateExpressionButton);
	
	
	this.helpCreateExpressionButton.expressionObj = this;		// add the reference of 'this' to the button so we can access 'this' object later
	this.helpCreateExpressionButton.onclick = function (e) {
		var expressionObj = this.expressionObj;		// get the reference to the expression object we set earlier
		
		
		// "sensorData" object is a GLOBAL object we declared at the main page
		sensorsDataPanel.show(expressionObj.getPageX() + expressionObj.getWidth(), expressionObj.getPageY(), expressionObj);
	}
}
dynapi.setPrototype('Expression', 'DynLayer');





Expression.prototype.setExpression = function (expression, replace) {
	if (expression != null) {
		if (replace == true) {
			var inputField = document.getElementById(this.id + "InputField");
			inputField.value = expression;
		} else {
			var inputField = document.getElementById(this.id + "InputField");
			inputField.value = inputField.value + " " + expression;
		}
	}	
}







Expression.prototype.getExpression = function () {
	var inputField = document.getElementById(this.id + "InputField");
	
	return inputField.value;
}





Expression.prototype.updateUI = function () {
	var inputField = document.getElementById(this.id + "InputField");
	inputField.value = inputField.currentValue || "";
}





