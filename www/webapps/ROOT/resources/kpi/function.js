



function Function (x, y, w, h, draggable, resizeOption) {
	this.functionName = null;			// The name of the function (String)
	this.parameterNames = null;			// The names of the parameters (String Array)


	this.Shape = Shape;
	this.Shape(x || 0, y || 0, w || this.getDefaultWidth(), h || this.getDefaultHeight(), draggable, (resizeOption || Shape.RESIZE_NEITHER));	
}
dynapi.setPrototype('Function', 'Shape');



Function.prototype.setupUI = function () {	

	// create the header for the Function
	this.header = this.addChild(new DynLayer("<table width='100%' height='100%' align='center' valign='middle'><tr><td><b><center>Function</center></b></td></tr></table>"));
	this.header.setBgColor("#D7D7FF");
	this.header.setBorder(1, "solid", "#0000FF");
	this.header.setHeight(25);
	this.header.setAnchor({left:50, right:50, top:0, stretchH:"100%"});

	
	// draw 4 corners for the function
		this.line1 = this.addChild(new DynLayer(null, 0, 0, 25, 1, "#0000ff"));
		this.line1.setAnchor({left:1, top:12});
	
		this.line2 = this.addChild(new DynLayer(null, 0, 0, 1, 25, "#0000ff"));
		this.line2.setAnchor({left:1, top:12});
	
		this.line3 = this.addChild(new DynLayer(null, 0, 0, 25, 1, "#0000ff"));
		this.line3.setAnchor({right:1, top:12});
	
		this.line4 = this.addChild(new DynLayer(null, 0, 0, 1, 25, "#0000ff"));
		this.line4.setAnchor({right:1, top:12});
	
		this.line5 = this.addChild(new DynLayer(null, 0, 0, 25, 1, "#0000ff"));
		this.line5.setAnchor({left:1, bottom:1});
	
		this.line6 = this.addChild(new DynLayer(null, 0, 0, 1, 25, "#0000ff"));
		this.line6.setAnchor({left:1, bottom:1});
	
		this.line7 = this.addChild(new DynLayer(null, 0, 0, 25, 1, "#0000ff"));
		this.line7.setAnchor({right:1, bottom:1});
	
		this.line8 = this.addChild(new DynLayer(null, 0, 0, 1, 25, "#0000ff"));
		this.line8.setAnchor({right:1, bottom:1});

	
	
	

	
	// create a DropInList to contain all the code for the function
	this.codeList = new DropInList (0, 0, 0, 0, DropInList.VERTICAL, DropInList.CENTER_ALIGNMENT, 10, 15);
	this.codeList.setAnchor({left:5, top:30, right:5, bottom:5});





	// listen to the code list's size event. When the code list is resized, we need
	// to resize this function also
	this.codeList.parentShape = this;		// add the reference to this function so we can accessit later
	this.codeList.addEventListener({
		onresize: function (e) {
			
			codeList = e.getSource();
			functionShape = codeList.parentShape;		// access the function reference we set earlier
			
			// put the code list at location (5, 30)
			codeList.setAnchor({left:5, top:30});

			// since we ALWAYS want the code list to be at location (5, 30), we NEED to make the
			// function's size at least big enough to show the WHOLE code list
			functionShape.setSize(codeList.getWidth() + 10, codeList.getHeight() + 35);
		}
		
	});

	

	this.codeList.setMinimumSize(200, 100);
	this.addChild(this.codeList);


	// set the funciton's minimum size. The function's minimum size should
	// be biug enough to show the entire code list
	this.setMinimumSize(210, 135);





	// create call back function to handle itemAdded event from the code list
	
	//  ********    CHANGE THIS LATER. THIS WILL BE HARD CODED FOR NOW.   ****************
	this.codeList.addItemListener(this);
	this.itemAdded = function (e) {
		var item = e.getItem();
		
		if (item != null) {
			if (!item.typeOf("Branch") && !item.typeOf("Loop") && !item.typeOf("Statement") && !item.typeOf("CodeBlock")) {
				
				// A function ONLY accept Branch, Loop, Statement and CodeBlock. If the added item IS NOT a
				// one of the type above, remove it
				this.codeList.removeItem(item);
			}
		}
	}


	


	// Create a variable to hold the signature for this function shape
	// The signature will include the function name, and parameters
	// (e.g. 'min(x, y)'). This example is a function that return the
	// minimum number between x and y.
	this.header.addEventListener({
		ondblclick: function (e) {
		
			var functionShape = e.getSource().parent;
			
			if (functionShape.isEditable()) {
	
				try {
					var nameRegex = "^[a-zA-Z_]([a-zA-Z_0-9]{0,100})$";	// the rule for the function name and parameter names
					
					var functionName = null;
					var parameterNames = new Array(0);

					var functionName = prompt("Please enter the name of the function (e.g. squareRoot, power)", ((functionShape.functionName != null)? functionShape.functionName: ""));
					if (functionName != null && functionName.trim().length > 0) {
	
						functionName = functionName.trim();
						if ((new RegExp(nameRegex, "ig")).test(functionName)) {
							
							var parameterList = prompt("Please enter the name of the parameters seperated by COMMAS (e.g. 'x, y, z')", ((functionShape.parameterNames != null)? functionShape.parameterNames: ""));
							if (parameterList != null && parameterList.trim().length > 0) {
								
								parameterNames = parameterList.split(",");
								if (parameterNames.length > 0) {
								
									for (var i = 0; i < parameterNames.length; i++) {
										parameterNames[i] = parameterNames[i].trim();
										if (! (new RegExp(nameRegex, "ig")).test(parameterNames[i])) {
											throw "Invalid parameter name '" + parameterNames[i] + "'. Parameter name MUST contain a-z, A-Z, 0-9, and underscore characters only. And function name CAN NOT started with a digit.";
										}
									}
								}
							}

							
							functionShape.functionName = functionName;
							functionShape.parameterNames = parameterNames;
	
							functionShape.header.setHTML("<table width='100%' height='100%' align='center' valign='middle'><tr><td><center><b>" + functionShape.functionName + "</b>(" + parameterNames + ")</center></td></tr></table>");
						} else {
							throw "Invalid function name '" + functionName + "'. Function name MUST contain a-z, A-Z, 0-9, and underscore characters only. And function name CAN NOT started with a digit.";
						}
					} else {
						throw "Invalid function name. Function name CAN NOT be empty.";
					}
				} catch (e) {
					alert(e.message || e);
				}
			}
		}
	});



}




// since code list minimum width is 200 AND it is ALWAYS at location 5 to the left,
// the function's default width SHOULD be 210 so that the WHOLE code list be be visible
Function.prototype.getDefaultWidth = function () {
	return 210;
}



// since code list minimum height is 100 AND it is ALWAYS at location 30 to the bottom,
// the function's default height SHOULD be 135 so that the WHOLE code list be be visible
Function.prototype.getDefaultHeight = function () {
	return 135;
}




// OVERRIDE the show and hide selection function. When a Function is selected, we set
// the background to light blue instead of gray as the way the showSelection() and
// hideSelection() functions do in the shape class
Function.prototype.showSelection = function () {

	this.line1.setHeight(3);
	this.line2.setWidth(3);
	this.line3.setHeight(3);
	this.line4.setWidth(3);
	this.line5.setHeight(3);
	this.line6.setWidth(3);
	this.line7.setHeight(3);
	this.line8.setWidth(3);

}

Function.prototype.hideSelection = function () {

	this.line1.setHeight(1);
	this.line2.setWidth(1);
	this.line3.setHeight(1);
	this.line4.setWidth(1);
	this.line5.setHeight(1);
	this.line6.setWidth(1);
	this.line7.setHeight(1);
	this.line8.setWidth(1);

}




/*
	functionname - The name of the function.
	parameterNames - The array of names of the parameters.
*/
Function.prototype.setSignature = function (functionName, parameterNames) {

	try {
		var nameRegex = "^[a-zA-Z_]([a-zA-Z_0-9]{0,100})$";	// the rule for the function name and parameter names

		if (functionName != null && functionName.trim().length > 0) {

			functionName = functionName.trim();
			if ((new RegExp(nameRegex, "ig")).test(functionName)) {

				if (parameterNames != null && parameterNames.length > 0) {
					
					for (var i = 0; i < parameterNames.length; i++) {
						parameterNames[i] = parameterNames[i].trim();
						if (! (new RegExp(nameRegex, "ig")).test(parameterNames[i])) {
							throw "Invalid parameter name '" + parameterNames[i] + "'. Parameter name MUST contain a-z, A-Z, 0-9, and underscore characters only. And function name CAN NOT started with a digit.";
						}
					}
				}
				

				this.functionName = functionName;
				this.parameterNames = parameterNames;				
				
				this.header.setHTML("<table height='100%' align='center' valign='middle'><tr><td><center><b>" + this.functionName + "</b>(" + arrayToString(this.parameterNames) + ")</center></td></tr></table>");
			} else {
				throw "Invalid function name '" + functionName + "'. Function name MUST contain a-z, A-Z, 0-9, and underscore characters only. And function name CAN NOT started with a digit.";
			}
		} else {
			throw "Invalid function name. Function name CAN NOT be empty.";
		}
	} catch (e) {
		alert(e.message || e);
	}

}






Function.prototype.generatePythonCode = function (levelNumber) {

	try {
		if (this.functionName == null || this.functionName.trim().length <= 0)
			throw "Function name can not be NULL or empty.";
		
		
		var parameterNames;
		if (this.parameterNames != null) {
			parameterNames = this.parameterNames;
		} else {
			parameterNames = new Array(0);
		}
		
		var code = "def " + this.functionName + " (" + parameterNames + "):";
		code = code.padLeft("  ", levelNumber);
		
		// generate code for all the elements in the code list and add them to the code
		var items = this.codeList.getAllItems();
		for (var i = 0; i < items.length; i++) {
			var childCode = items[i].generatePythonCode(levelNumber + 1);
			if (childCode != null) {
				code = code + "\n" + childCode;
			}
		}
	
		return code;
		
	} catch (e) {
		this.setSelected(true);
		throw e;
	}
	
	return null;
}





Function.prototype.serializeToXML = function (dom) {

	try {
		if (this.functionName == null || this.functionName.trim().length <= 0)
			throw "Function name can not be NULL or empty.";
		

		// Use the class name as the tag name
		var element = dom.createElement("Function");
		element.setAttribute("x", this.getX());
		element.setAttribute("y", this.getY());
		element.setAttribute("w", this.getWidth());
		element.setAttribute("h", this.getHeight());
		element.setAttribute("name", this.functionName);
		
		if (this.parameterNames != null && this.parameterNames.length > 0) {
			element.setAttribute("parameters", this.parameterNames.toString());
		}


		// serialize all the elements in the code list and add them to the element
		var items = this.codeList.getAllItems();
		for (var i = 0; i < items.length; i++) {
			var childElement = items[i].serializeToXML(dom);
			if (childElement != null) {
				element.appendChild(childElement);
			}
		}

		
		return element;
	

	} catch (e) {
		this.setSelected(true);
		throw e;
	}
	
	return null;

}









