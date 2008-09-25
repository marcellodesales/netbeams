



function Branch (x, y, w, h, draggable, resizeOption) {
	this.Shape = Shape;
	this.Shape(x, y, w, h, draggable, (resizeOption || Shape.RESIZE_NEITHER));
}
dynapi.setPrototype('Branch', 'Shape');



Branch.prototype.setupUI = function () {

	// create the header for the branch shape
	this.header = this.addChild(new DynLayer("<table width='100%' height='100%' align='center' valign='middle'><tr><td><b><center>branch</center></b></td></tr></table>"));
	this.header.setSize(150, 20);
	this.header.setAnchor({centerH:0});

	
	// draw 4 corners for the branch shape
		this.line1 = this.addChild(new DynLayer(null, 0, 0, 25, 1, "#00D936"));
		this.line1.setAnchor({left:1, top:12});
	
		this.line2 = this.addChild(new DynLayer(null, 0, 0, 1, 25, "#00D936"));
		this.line2.setAnchor({left:1, top:12});
	
		this.line3 = this.addChild(new DynLayer(null, 0, 0, 25, 1, "#00D936"));
		this.line3.setAnchor({right:1, top:12});
	
		this.line4 = this.addChild(new DynLayer(null, 0, 0, 1, 25, "#00D936"));
		this.line4.setAnchor({right:1, top:12});
	
		this.line5 = this.addChild(new DynLayer(null, 0, 0, 25, 1, "#00D936"));
		this.line5.setAnchor({left:1, bottom:1});
	
		this.line6 = this.addChild(new DynLayer(null, 0, 0, 1, 25, "#00D936"));
		this.line6.setAnchor({left:1, bottom:1});
	
		this.line7 = this.addChild(new DynLayer(null, 0, 0, 25, 1, "#00D936"));
		this.line7.setAnchor({right:1, bottom:1});
	
		this.line8 = this.addChild(new DynLayer(null, 0, 0, 1, 25, "#00D936"));
		this.line8.setAnchor({right:1, bottom:1});

	








	// create a DropInList to contain all the code for the branch
	this.codeList = new DropInList (0, 0, 0, 0, DropInList.HORIZONTAL, DropInList.TOP_ALIGNMENT, 10, 15);
	this.codeList.setAnchor({left:5, top:20, right:5, bottom:5});
	


	// listen to the code list's size event. When the code list is resized, we need
	// to resize this branch also
	this.codeList.parentShape = this;		// add the reference to this branch so we can access it later
	this.codeList.addEventListener({
		onresize: function (e) {
						
			codeList = e.getSource();
			conditionsShape = codeList.parentShape;		// access the branch reference we set earlier
			
			// put the code list at location (5, 20)
			codeList.setAnchor({left:5, top:20});

			// since we ALWAYS want the code list to be at location (5, 20), we NEED to make the
			// branch's size at least big enough to show the WHOLE code list
			conditionsShape.setSize(codeList.getWidth() + 10, codeList.getHeight() + 25);
		}
	});

	
	

	this.codeList.setMinimumSize(190, 100);
	this.addChild(this.codeList);

	// listener to the code list's item event
	this.codeList.addItemListener(this);
	
	
	// create call back function to handle itemAdded event from the code list
	this.itemAdded = function (e) {
		var item = e.getItem();
		
		if (item != null && !item.typeOf("IfCondition")) {
			// A branch ONLY accept If Condition shapes. If the additem IS NOT a condition, remove it
			this.codeList.removeItem(item);
		} else {
			this.resetAllConditionType();
		}
	}
	
	
	// create call back function to handle itemRemoved event from the code list
	this.itemRemoved = function (e) {
		this.resetAllConditionType();
	}
	

	
	// create call back function to handle itemMoved event from the code list
	this.itemMoved = function (e) {
		this.resetAllConditionType();
	}
	

	
	
	/*	
		when an item, a condition, is moved added or removed, we have to reset the condition type of the condition
		if the condition is the FIRST condition, we will make it a IF_TYPE. If it is the second
		or after condition, we will make the condition of type ELSE_IF_TYPE.
		If it is the LAST condition AND if it DOES not have a condition text, it will become
		ELSE_TYPE condition
	*/
	this.resetAllConditionType = function () {
		// reset ALL the item's condition type depend on which INDEX it is at
		var n = this.codeList.getSize();
		for (var i = 0; i < n; i++) {
			var item = this.codeList.getItemAt(i);
		
		
			if (item != null) {
				var index = i;
				
				item.isLastCondition = false;
				
				if (index == 0) {
					item.setConditionType(IfCondition.IF_TYPE);
				} else if (index > 0) {
					
					// if the condition's index is more than 0, we change it to ELSE_IF or ELSE
					if (index < this.codeList.getSize() - 1) {
						item.setConditionType(IfCondition.ELSE_IF_TYPE);
					} else {
						// if the condition is the last condition we turn it to ELSE_IF or ELSE
						// depend on whether it have a condition text
						
						if (item.condition != null && item.condition.trim().length > 0) {
							// if the item have a condition text, we make it a ELSE_IF condition
							item.setConditionType(IfCondition.ELSE_IF_TYPE);
						} else {
							// we make it a ELSE condition
							item.setConditionType(IfCondition.ELSE_TYPE);
						}
						
						item.isLastCondition = true;
					}
					
				}
			}
		}
	}


	this.setMinimumSize(200, 125);
	
}



	
	

Branch.prototype.getDefaultWidth = function () {
	return 200;
}


Branch.prototype.getDefaultHeight = function () {
	return 125;
}



// OVERRIDE the show and hide selection function. When a Branch is selected, we set
// the background to light purple instead of gray as the way the showSelection() and
// hideSelection() functions do in the shape class
Branch.prototype.showSelection = function () {

	this.line1.setHeight(3);
	this.line2.setWidth(3);
	this.line3.setHeight(3);
	this.line4.setWidth(3);
	this.line5.setHeight(3);
	this.line6.setWidth(3);
	this.line7.setHeight(3);
	this.line8.setWidth(3);

}

Branch.prototype.hideSelection = function () {

	this.line1.setHeight(1);
	this.line2.setWidth(1);
	this.line3.setHeight(1);
	this.line4.setWidth(1);
	this.line5.setHeight(1);
	this.line6.setWidth(1);
	this.line7.setHeight(1);
	this.line8.setWidth(1);

}





Branch.prototype.generatePythonCode = function (levelNumber) {
	// generate code for all the elements in the code list and add them to the code
	var items = this.codeList.getAllItems();
	
	var code = "";
	if (items.length > 0) {
		for (var i = 0; i < items.length; i++) {
			var childCode = items[i].generatePythonCode(levelNumber);
			if (childCode != null) {
				code = code + "\n" + childCode;
			}
		}
	} else {
		// return NULL if there is NO if/else conditions
		return null;
	}
	
	return code;
}




Branch.prototype.serializeToXML = function (dom) {
	// Serialize all the elements in the code list and add them to the element
	var items = this.codeList.getAllItems();
	
	var element = dom.createElement("Branch");
	element.setAttribute("x", this.getX());
	element.setAttribute("y", this.getY());
	element.setAttribute("w", this.getWidth());
	element.setAttribute("h", this.getHeight());
	
	if (items.length > 0) {
		for (var i = 0; i < items.length; i++) {
			var childElement = items[i].serializeToXML(dom);
			if (childElement != null) {
				element.appendChild(childElement);
			}
		}
	} else {
		// return NULL if there is NO if/else conditions
		return null;
	}
	
	return element;
}
















function IfCondition (x, y, w, h, draggable, resizeOption) {
	this.conditionType = null;
	this.condition = null;




	this.Shape = Shape;
	this.Shape(x || 0, y || 0, w || this.getDefaultWidth(), h || this.getDefaultHeight(), draggable, (resizeOption || Shape.RESIZE_NEITHER));
}
dynapi.setPrototype('IfCondition', 'Shape');

IfCondition.IF_TYPE = "if";
IfCondition.ELSE_IF_TYPE = "else if";
IfCondition.ELSE_TYPE = "else";






IfCondition.prototype.setupUI = function () {

	this.setMinimumSize(210, 85);



	// create the header for the Condition
		this.header = this.addChild(new DynLayer());
		this.header.setBgColor("#EBD7FF");
		this.header.setBorder(1, "solid", "#9B00FF");
		this.header.setHeight(25);
		this.header.setAnchor({left:50, right:50, top:0, stretchH:"100%"});


	
	// draw the border and the arrow for the condition
		this.line1 = this.addChild(new DynLayer(null, 0, 0, 25, 1, "#9B00FF"));
		this.line1.setAnchor({left:1, top:12});

		this.line2 = this.addChild(new DynLayer(null, 0, 0, 1, null, "#9B00FF"));
		this.line2.setAnchor({left:1, top:12, bottom:1, stretchV:"100%"});

		this.line3 = this.addChild(new DynLayer(null, 0, 0, 25, 1, "#9B00FF"));
		this.line3.setAnchor({right:1, top:12});

		this.line4 = this.addChild(new DynLayer(null, 0, 0, 1, null, "#9B00FF"));
		this.line4.setAnchor({right:1, top:12, bottom:1, stretchV:"100%"});

		this.line5 = this.addChild(new DynLayer(null, 0, 0, 0, 1, "#9B00FF"));
		this.line5.setAnchor({right:1, left:1, bottom:1, stretchH:"100%"});
	


	


	
	// create a DropInList to contain all the code for the CONDITION
	this.codeList = new DropInList (0, 0, 0, 0, DropInList.VERTICAL, DropInList.CENTER_ALIGNMENT, 10, 15);
	this.codeList.setAnchor({left:5, top:30, right:5, bottom:5});


	// listen to the code list's size event. When the code list is resized, we need
	// to resize this CONDITION also
	this.codeList.parentShape = this;		// add the reference to this CONDITION so we can access it later
	this.codeList.addEventListener({
		onresize: function (e) {
			
			codeList = e.getSource();
			conditionShape = codeList.parentShape;		// access the CONDITION reference we set earlier
			
			// put the code list at location (5, 30)
			codeList.setAnchor({left:5, top:30});

			// since we ALWAYS want the code list to be at location (5, 30), we NEED to make the
			// CONDITION's size at least big enough to show the WHOLE code list
			conditionShape.setSize(codeList.getWidth() + 10, codeList.getHeight() + 35);
		}
	});

	

	this.codeList.setMinimumSize(200, 50);
	this.addChild(this.codeList);
	



	// set the type of condition. The 3 possible types are IF, ELSE_IF and ELSE
	this.setConditionType(IfCondition.IF_TYPE);
	
	// set the condition text. initlially will be empty
	this.setCondition("");
	
	// add event to the HEADER so that when the user double click on it, we can ask the user to enter a condition text
	this.header.addEventListener({
		ondblclick: function (e) {
			
			var conditionShape = e.getSource().parent;

			var condition = prompt("Please enter the condition (e.g. 'sum <= n + 10')", ((conditionShape.condition != null)? conditionShape.condition: ""));
			if (condition != null) {
				conditionShape.setCondition(condition);
			}
		}
	});
	
	
	// this flag to indicate whether this condition is the last condition of the branch
	// this flag is ONLY for changing the condition from ELSE_IF to ELSE.
	// If this condition is the LAST condition, it can change from ELSE_IF to ELSE or vise versa.
	// This can happen when the user entered a condition text or remove the condition text
	// from the condition.
	this.isLastCondition = false;
	
	

	
	// create call back function to handle itemAdded event from the code list
	
	//  ********    CHANGE THIS LATER. THIS WILL BE HARD CODED FOR NOW.   ****************
	this.codeList.addItemListener(this);
	this.itemAdded = function (e) {
		var item = e.getItem();
		
		if (item != null) {
			if (!item.typeOf("Branch") && !item.typeOf("Loop") && !item.typeOf("Statement") && !item.typeOf("CodeBlock")) {
				
				// A condition ONLY accept Branch, Loop, Statement and CodeBlock. If the added item IS NOT a
				// one of the type above, remove it
				this.codeList.removeItem(item);
			}
		}
	}
}



// since code list minimum width is 200 AND it is ALWAYS at location 5 to the left,
// the condition's default width SHOULD be 210 so that the WHOLE code list be be visible
IfCondition.prototype.getDefaultWidth = function () {
	return 210;
}



// since code list minimum height is 50 AND it is ALWAYS at location 30 to the bottom,
// the condition's default height SHOULD be 85 so that the WHOLE code list be be visible
IfCondition.prototype.getDefaultHeight = function () {
	return 85;
}




// OVERRIDE the show and hide selection function. When a Condition is selected, we set
// the background to light purple instead of gray as the way the showSelection() and
// hideSelection() functions do in the shape class
IfCondition.prototype.showSelection = function () {
	this.line1.setHeight(3);
	this.line2.setWidth(3);
	this.line3.setHeight(3);
	this.line4.setWidth(3);
	this.line5.setHeight(3);
}



IfCondition.prototype.hideSelection = function () {	
	this.line1.setHeight(1);
	this.line2.setWidth(1);
	this.line3.setHeight(1);
	this.line4.setWidth(1);
	this.line5.setHeight(1);
}









/*
	Change the type of the condition. The possible type of condition are
	
	IfCondition.IF_TYPE, IfCondition.ELSE_IF_TYPE or IfCondition.ELSE_TYPE
*/
IfCondition.prototype.setConditionType = function (type) {
	
	// ONLY allow the type to be one of the valid type IF_TYPE, ELSE_IF_TYPE or ELSE_TYPE
	if (type == IfCondition.IF_TYPE || type == IfCondition.ELSE_IF_TYPE || type == IfCondition.ELSE_TYPE) {
		this.conditionType = type;

		// redisplay the condition
		if (this.conditionType != IfCondition.ELSE_TYPE) {
			this.header.setHTML("<table width='100%' height='100%' align='center' valign='middle'><tr><td><center><b>" + this.conditionType + "</b> (" + ((this.condition)? this.condition: "")  + ")</center></td></tr></table>");
		} else {
			this.header.setHTML("<table width='100%' height='100%' align='center' valign='middle'><tr><td><center><b>" + this.conditionType + "</b></center></td></tr></table>");
		}
	}
	
}



/*
	Set a new condition. When a new condition is set, we have to change a condition from
	ELSE IF to ELSE or vise versa depending on the condition. 
*/
IfCondition.prototype.setCondition = function (condition) {
	if (condition != null) {
	
		// set the condition
		this.condition = condition.trim();


		// if the condition became empty we MIGHT have to change the condition to ELSE type
		if (this.condition.length <= 0) {
			// if there is no more condition text left AND this condition is the
			// last condition, we can change it to ELSE type
			if (this.isLastCondition) {
				this.conditionType = IfCondition.ELSE_TYPE;
			}
		} else {
			// if there is some condition text and if this condition is
			// the last condition, we change it to ELSE_IF
			if (this.isLastCondition) {
				this.conditionType = IfCondition.ELSE_IF_TYPE;
			}
		}
 		
				
		
		// redisplay the condition
		if (this.conditionType != IfCondition.ELSE_TYPE) {
			this.header.setHTML("<table width='100%' height='100%' align='center' valign='middle'><tr><td><center><b>" + this.conditionType + "</b> (" + ((this.condition)? this.condition: "")  + ")</center></td></tr></table>");
		} else {
			this.header.setHTML("<table width='100%' height='100%' align='center' valign='middle'><tr><td><center><b>" + this.conditionType + "</b></center></td></tr></table>");
		}
	}
}







IfCondition.prototype.generatePythonCode = function (levelNumber) {

	try {
		if (this.conditionType != IfCondition.IF_TYPE && 
			this.conditionType != IfCondition.ELSE_IF_TYPE &&
			this.conditionType != IfCondition.ELSE_TYPE) {
			
			throw "Unexpected error. If Condition's type is NOT one of the following, 'if', 'else if', or 'else'.";
		}
		
		
		// The condition is REQUIRED for 'if' and 'else if' so if the condition is missing,
		// throw exception.
		if (this.conditionType != IfCondition.ELSE_TYPE && (this.condition == null || this.condition.trim().length <= 0)) {
			throw "If condition can not be NULL or empty";
		}
		
		
		var code = "";
		if (this.conditionType == IfCondition.IF_TYPE)
			code = "if " + this.condition + ":";
		else if (this.conditionType == IfCondition.ELSE_IF_TYPE)
			code = "elif " + this.condition + ":";
		else
			code = "else:";

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






IfCondition.prototype.serializeToXML = function (dom) {

	try {
		if (this.conditionType != IfCondition.IF_TYPE && 
			this.conditionType != IfCondition.ELSE_IF_TYPE &&
			this.conditionType != IfCondition.ELSE_TYPE) {
			
			throw "Unexpected error. If Condition's type is NOT one of the following, 'if', 'else if', or 'else'.";
		}
		
		
		// The condition is REQUIRED for 'if' and 'else if' so if the condition is missing,
		// throw exception.
		if (this.conditionType != IfCondition.ELSE_TYPE && (this.condition == null || this.condition.trim().length <= 0)) {
			throw "If condition can not be NULL or empty";
		}
		
		
		var element = dom.createElement("IfCondition");
		element.setAttribute("x", this.getX());
		element.setAttribute("y", this.getY());
		element.setAttribute("w", this.getWidth());
		element.setAttribute("h", this.getHeight());

		element.setAttribute("type", this.conditionType);
		
		if (this.condition != null) {
			element.setAttribute("condition", this.condition);
		}
		
		
		// generate code for all the elements in the code list and add them to the code
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


