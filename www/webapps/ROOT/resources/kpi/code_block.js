




// multiple statements shape
function CodeBlock (x, y, w, h, draggable, resizeOption) {
	this.Shape = Shape;
	this.Shape(x, y, w, h, draggable, (resizeOption || Shape.RESIZE_WIDTH_AND_HEIGHT));
	
	this.setBorder(1, "solid", "#D26900");
	this.setBgColor("#ffffff");


	this.setMinimumSize(100, 25);
	this.setMaximumSize(1000, 500);
	
	
	this.getDefaultWidth = function () {
		return 200;
	}
	
	
	this.getDefaultHeight = function () {
		return 50;
	}
	
	
	// OVERRIDE the showSelection and hideSelection functions.
	this.showSelection = function () {
		this.setBgColor("#ffffaa");
	}
	
	
	this.hideSelection = function () {
		this.setBgColor("#ffffff");
	}
}
dynapi.setPrototype('CodeBlock', 'Shape');


