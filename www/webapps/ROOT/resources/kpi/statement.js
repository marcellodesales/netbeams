




function Statement (x, y, w, h, draggable, resizeOption) {
	this.statement = null;


	this.Shape = Shape;
	this.Shape(x, y, w, h, draggable, (resizeOption || Shape.RESIZE_WIDTH_ONLY));
}
dynapi.setPrototype('Statement', 'Shape');





/*
	EACH type of shape SHOULD override this function if it NEEDs to draw its own UI.
	If a type of shape EXTENDS another type of shape and it also want to use the parent's
	shape's UI then it DOES NOT need to override this function.
*/
Statement.prototype.setupUI = function () {
	this.setMinimumSize(100, 25);
	this.setMaximumSize(1000, 25);
	
	this.setBorder(1, "solid", "#D26900");
	this.setBgColor("#ffffff");


	
	this.addEventListener({
		ondblclick: function (e) {
			
			var statementShape = e.getSource();

			var statement = prompt("Please enter the statement", ((statementShape.statement != null)? statementShape.statement: ""));
			if (statement != null && statement.trim().length > 0) {
				statementShape.setStatement(statement);
			}
		}
	});

}










// OVERRIDE the showSelection and hideSelection functions.
Statement.prototype.showSelection = function () {
	this.setBgColor("#ffffaa");
}


Statement.prototype.hideSelection = function () {
	this.setBgColor("#ffffff");
}





Statement.prototype.setStatement = function (statement) {
	if (statement != null && statement.trim().length > 0) {
		this.statement = statement;
		
		// remove the previous statement first if it exist
		if (this.statementLayer != null) {
			this.statementLayer.removeFromParent();
		}

		this.statementLayer = this.addChild(new DynLayer("<table width='100%' height='100%' align='center' valign='middle'><tr><td><center>" + statement + "</center></td></tr></table>"));
		this.statementLayer.setAnchor({top:0, bottom:0, left:10, right:10, stretchH:"100%", strectV:"100%"});
	} else {
		alert("ERROR in Statement. Statement can not be null or empty.");
	}
}




Statement.prototype.generatePythonCode = function (levelNumber) {
	if (this.statement != null && this.statement.trim().length > 0) {
		var code = this.statement;
		code = code.padLeft("  ", levelNumber);		
		return code;
	} else {
		this.setSelected(true);
		throw "Statement can not be NULL or empty.";
	}

	return null;
}






Statement.prototype.serializeToXML = function (dom) {
	if (this.statement != null && this.statement.trim().length > 0) {
		var element = dom.createElement("Statement");
		element.setAttribute("x", this.getX());
		element.setAttribute("y", this.getY());
		element.setAttribute("w", this.getWidth());
		element.setAttribute("h", this.getHeight());

		element.setAttribute("statement", this.statement);
		return element;
	} else {
		this.setSelected(true);
		throw "Statement can not be NULL or empty.";
	}

	return null;
}








