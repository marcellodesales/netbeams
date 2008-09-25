




function Loop (x, y, w, h, draggable, resizeOption) {
	this.condition = null;			// the condition of the loop


	this.Shape = Shape;
	this.Shape(x || 0, y || 0, w || this.getDefaultWidth(), h || this.getDefaultHeight(), draggable, (resizeOption || Shape.RESIZE_NEITHER));	
}
dynapi.setPrototype('Loop', 'Shape');






Loop.prototype.setupUI = function () {

	// create the header for the LOOP
		this.header = this.addChild(new DynLayer("<table width='100%' height='100%' align='center' valign='middle'><tr><td><b><center>while </b>(condition)</center></b></td></tr></table>"));
		this.header.setBgColor("#FFE1E1");
		this.header.setBorder(1, "solid", "#FF0000");
		this.header.setHeight(25);
		this.header.setAnchor({left:50, right:50, top:0, stretchH:"100%"});


	
	// draw the border and the arrow for the loop
		this.line1 = this.addChild(new DynLayer(null, 0, 0, 25, 1, "#FF0000"));
		this.line1.setAnchor({left:1, top:12});

		this.line2 = this.addChild(new DynLayer(null, 0, 0, 1, null, "#FF0000"));
		this.line2.setAnchor({left:1, top:12, bottom:1, stretchV:"100%"});

		this.line3 = this.addChild(new DynLayer(null, 0, 0, 25, 1, "#FF0000"));
		this.line3.setAnchor({right:1, top:12});

		this.line4 = this.addChild(new DynLayer(null, 0, 0, 1, null, "#FF0000"));
		this.line4.setAnchor({right:1, top:12, bottom:1, stretchV:"100%"});

		this.line5 = this.addChild(new DynLayer(null, 0, 0, 0, 1, "#FF0000"));
		this.line5.setAnchor({right:1, left:1, bottom:1, stretchH:"100%"});

		this.arrow = this.addChild(new DynLayer(), 0, 0, 10, 25);
		this.arrow.setHTML("<img src='" + getResourcesPath() + "right_arrow.gif" + "'>");
		this.arrow.setAnchor({left:26, top:0});
	





	this.setMinimumSize(210, 85);

	
	// create a DropInList to contain all the code for the function
	this.codeList = new DropInList (0, 0, 0, 0, DropInList.VERTICAL, DropInList.CENTER_ALIGNMENT, 10, 15);
	this.codeList.setAnchor({left:5, top:30, right:5, bottom:5});
	this.codeList.setMinimumSize(200, 50);
	this.addChild(this.codeList);





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






	// create a variable to hold the codition for this loop shape
	this.header.addEventListener({
		ondblclick: function (e) {
			
			var conditionShape = e.getSource().parent;

			var condition = prompt("Please enter the condition (e.g. 'sum <= n + 10')", ((conditionShape.condition != null)? conditionShape.condition: ""));
			if (condition != null) {
				conditionShape.condition = condition;
				conditionShape.header.setHTML("<table width='100%' height='100%' align='center' valign='middle'><tr><td><center><b>while </b>(" + conditionShape.condition + ")</center></td></tr></table>");
			}
		}
	});

}

	











	



// since code list minimum width is 200 AND it is ALWAYS at location 5 to the left,
// the loop's default width SHOULD be 210 so that the WHOLE code list be be visible
Loop.prototype.getDefaultWidth = function () {
	return 210;
}



// since code list minimum height is 50 AND it is ALWAYS at location 30 to the bottom,
// the loop's default height SHOULD be 85 so that the WHOLE code list be be visible
Loop.prototype.getDefaultHeight = function () {
	return 85;
}




// OVERRIDE the show and hide selection function. When a Loop is selected, we set
// the background to light red instead of gray as the way the showSelection() and
// hideSelection() functions do in the shape class
Loop.prototype.showSelection = function () {
	this.line1.setHeight(3);
	this.line2.setWidth(3);
	this.line3.setHeight(3);
	this.line4.setWidth(3);
	this.line5.setHeight(3);
}



Loop.prototype.hideSelection = function () {
	this.line1.setHeight(1);
	this.line2.setWidth(1);
	this.line3.setHeight(1);
	this.line4.setWidth(1);
	this.line5.setHeight(1);
}


	





Loop.prototype.generatePythonCode = function (levelNumber) {
	if (this.condition != null && this.condition.trim().length > 0) {
		var code = "while " + this.condition + ":";
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
	} else {
		this.setSelected(true);
		throw "While loop's condition can not be NULL or empty.";
	}

	return null;
}





Loop.prototype.serializeToXML = function (dom) {
	if (this.condition != null && this.condition.trim().length > 0) {
	
		var element = dom.createElement("Loop");
		element.setAttribute("condition", this.condition);
		element.setAttribute("x", this.getX());
		element.setAttribute("y", this.getY());
		element.setAttribute("w", this.getWidth());
		element.setAttribute("h", this.getHeight());
		
		
		// Serialize all the elements in the code list and add them to the element
		var items = this.codeList.getAllItems();
		for (var i = 0; i < items.length; i++) {
			var childElement = items[i].serializeToXML(dom);
			if (childElement != null) {
				element.appendChild(childElement);
			}
		}
		
		return element;
	} else {
		this.setSelected(true);
		throw "While loop's condition can not be NULL or empty.";
	}

	return null;
}












