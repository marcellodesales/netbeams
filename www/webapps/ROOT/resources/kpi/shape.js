


// define global variables and functions for

var SHAPE_RESIZE_BOX_SIZE = 7;





function typeOf (childClass, parentClass) {
	if (childClass == parentClass) {
		return true;
	} else {
		return true;
	}
	
	return false;
}






/*
	Define a generic Shape class which ALL other shapes will extend.
	This Shape class will implement all the common functionalities that all different types of
	shapes has such as drag, resize, etc.... If there are other functionalities that only exist
	for a specific shape, those functionalities should be implemented in that shape instead.
*/
function Shape (x, y, w, h, draggable, resizeOption) {
	this.Dynlayer = DynLayer;
	this.Dynlayer(null, x, y, w, h);

	

	if (this._className != "Shape") {
		Shape.addToInheritanceTree(this._className, this._pClassName);
	}




	/******************    Begin initilizing the shape during construction    ***************************/


		// a flag to indicate whether the code for this shape CAN be edited.
		this.editable = true;



		// a flag to indicate whether this shape is selected
		this.isSelected = false;
		
		
		
	

		// if the user want the shape to be draggable, add drag event to it. Default will be TRUE
		this.draggable = (draggable == false)? false: true;
		if (this.draggable) {
			// add drag event to the shape
			DragEvent.enableDragEvents(this);
			this.addEventListener({
				ondragmove: function (e) {
					srcParent = e.getSource();
					srcParent.setZIndex({topmost:true});
				}
			});
		}
	
	
	
		/*
			If the user want this shape to be resizable, we will create little resize boxes for the user
			to resize the shape.
			
			If resizeOption == Shape.RESIZE_WIDTH_ONLY, we will ONLY create 2 boxes on the left and right for the user to resize the shape's width
			If resizeOption == Shape.RESIZE_HEIGHT_ONLY, we will ONLY create 2 boxes on the top and bottom for the user to resize the shape's height
			If resizeOption == Shape.RESIZE_WIDTH_AND_HEIGHT, we will create 4 boxes on the left, right, top, and bottom for the user to resize the shape's width and height
			If resizeOption == Shape.RESIZE_NEITHER, we will NOT create any boxes for the user to resize the shape.
		
			If the user DOES NOT provide this option, the default option will be Shape.RESIZE_NEITHER and this shape's
			resizable will be set to false
			
		*/
		
		this.resizeBoxes = new Array();					// the are ONLY 4 resizes total (left, right, top and bottom)
		this.resizeBoxes[0] = null;						// top box			N
		this.resizeBoxes[1] = null;						// bottom box		S
		this.resizeBoxes[2] = null;						// right box		E
		this.resizeBoxes[3] = null;						// left box			W
		this.resizeOption = Shape.RESIZE_NEITHER;		// default will be neither
		this.resizable = false;							// default will be false
		


		if (resizeOption == Shape.RESIZE_HEIGHT_ONLY || resizeOption == Shape.RESIZE_WIDTH_AND_HEIGHT) {
			this.resizable = true;
			this.resizeOption = resizeOption;

			// create 2 boxes on the top and bottom for the user to resize the shape's height
			this.resizeBoxes[0] = this.addChild(new DynLayer(null, 0, 0, SHAPE_RESIZE_BOX_SIZE, SHAPE_RESIZE_BOX_SIZE, "#000000"));			// N
			this.resizeBoxes[0].setAnchor({centerH:0, top:0});
			this.resizeBoxes[0].setCursor("n-resize");
		
			this.resizeBoxes[1] = this.addChild(new DynLayer(null, 0, 0, SHAPE_RESIZE_BOX_SIZE, SHAPE_RESIZE_BOX_SIZE, "#000000"));			// S
			this.resizeBoxes[1].setAnchor({centerH:0, bottom:0});
			this.resizeBoxes[1].setCursor("s-resize");
			
		}

		if (resizeOption == Shape.RESIZE_WIDTH_ONLY || resizeOption == Shape.RESIZE_WIDTH_AND_HEIGHT) {
			this.resizable = true;
			this.resizeOption = resizeOption;

			// create 2 boxes on the left and right for the user to resize the shape's width
			this.resizeBoxes[2] = this.addChild(new DynLayer(null, 0, 0, SHAPE_RESIZE_BOX_SIZE, SHAPE_RESIZE_BOX_SIZE, "#000000"));			// E
			this.resizeBoxes[2].setAnchor({centerV:0, right:0});
			this.resizeBoxes[2].setCursor("e-resize");
		
			this.resizeBoxes[3] = this.addChild(new DynLayer(null, 0, 0, SHAPE_RESIZE_BOX_SIZE, SHAPE_RESIZE_BOX_SIZE, "#000000"));			// W
			this.resizeBoxes[3].setAnchor({centerV:0, left:0});
			this.resizeBoxes[3].setCursor("w-resize");
		}



		
		// enable drag events for all resize boxes
		for (var i = 0; i < this.resizeBoxes.length; i++) {
			// some resize boxes can be null depend on the resize option
			// box[0] and box[1] are for N and S so they will be NULL if resize option is WIDTH ONLY
			// box[2] and box[3] are for E and W so they will be NULL if resize option is HEIGHT ONLY
			if (this.resizeBoxes[i] != null) {
				DragEvent.enableDragEvents(this.resizeBoxes[i]);
				this.resizeBoxes[i].addEventListener({
					mouseX: 0,
					mouseY: 0,
		
					ondragstart: function (e) {
						this.mouseX = e.getPageX();
						this.mouseY = e.getPageY();
						
						
						// Remove the resize event to save bandwidth. We will ONLY update the UI
						// when the user STOPPED draging the resize box
						draggingBox = e.getSource();
						srcParent = draggingBox.parent;
						srcParent.removeEventListener(srcParent.shapeResizeListener);
					},
		
		
					// resize the shape when the user move one of the little boxes
					ondragmove: function (e) {
						
						diffX = e.getPageX() - this.mouseX;
						diffY = e.getPageY() - this.mouseY;
		
		
						draggingBox = e.getSource();
						srcParent = draggingBox.parent;
		
						// check which one of the boxes is being dragged and resize the shape accordingly
						if (draggingBox == draggingBox.parent.resizeBoxes[0]) {
							srcParent.setSize(srcParent.getWidth(), srcParent.getHeight() - diffY);
							srcParent.setLocation(srcParent.getX(), srcParent.getY() + diffY);
							draggingBox.setAnchor({centerH:0, top:0});
						} else if (draggingBox == draggingBox.parent.resizeBoxes[1]) {
							srcParent.setSize(srcParent.getWidth(), srcParent.getHeight() + diffY);
							draggingBox.setAnchor({centerH:0, bottom:0});
						} else if (draggingBox == draggingBox.parent.resizeBoxes[2]) {
							srcParent.setSize(srcParent.getWidth() + diffX, srcParent.getHeight());
							draggingBox.setAnchor({centerV:0, right:0});
						} else if (draggingBox == draggingBox.parent.resizeBoxes[3]) {
							srcParent.setSize(srcParent.getWidth() - diffX, srcParent.getHeight());
							srcParent.setLocation(srcParent.getX() + diffX, srcParent.getY());
							draggingBox.setAnchor({centerV:0, left:0});
						}
		
		
						this.mouseX = e.getPageX();
						this.mouseY = e.getPageY();
						
					},
		
		
		
					ondragend: function (e) {
		
						// add the resize listener back
						draggingBox = e.getSource();
						srcParent = draggingBox.parent;
						srcParent.addEventListener(srcParent.shapeResizeListener);
						
	
						// make sure the shape has size greater than 20x20
						srcParent = e.getSource().parent;
						srcParent.setSize(Math.max(srcParent.getWidth(), 20), Math.max(srcParent.getHeight(), 20));
						
					}
		
				});
			}
		}
		
		
		

		
		

		// add mouseover and mouseout listener. When the mouse move over the shape,
		// we need to set selected to true so the user can resize the shape
		this.addEventListener({
			isDragging: false,
			
			onmousedown: function (e) {
				e.preventBubble()
				this.isDragging = false;
			},
			
			
			onclick: function (e) {

				e.preventBubble()
				var shape = e.getSource();
				
				// Select the shape if it is NOT selected.
				if (! shape.isSelected) {
					shape.setSelected(true);
				} else if (shape.isSelected && !this.isDragging) {
					// if shape is selected and if shape is NOT dragging then unselect the shape
					shape.setSelected(false);
				}
			},
			
			
			ondragstart: function (e) {
				this.isDragging = true;
			},
			
			ondragmove: function (e) {
				this.isDragging = true;

				var shape = e.getSource();
				
				// Select the shape if it is NOT selected.
				if (! shape.isSelected) {
					shape.setSelected(true);
				}
			}
			
			
		});

		
	
	
		this.setupUI();
	


		this.setVisible(true);
		this.setSelected(false);
	

	/******************    Finish initilizing the shape    ******************************************/


	DragEvent.disableDragEvents(this);
}
dynapi.setPrototype('Shape', 'DynLayer');

Shape.RESIZE_WIDTH_ONLY = "width";
Shape.RESIZE_HEIGHT_ONLY = "height";
Shape.RESIZE_WIDTH_AND_HEIGHT = "both";
Shape.RESIZE_NEITHER = "neither";					// DEFAULT value for any type of shape
Shape.CURRENT_SELECTED_SHAPE = null;





Shape.INHERITANCE_TREE = Sarissa.getDomDocument();
Shape.INHERITANCE_TREE.appendChild(Shape.INHERITANCE_TREE.createElement("Shape"));		// Make Shape the FIRST child in the inheritance tree



Shape.addToInheritanceTree = function (childClassName, parentClassName) {

	// Look for the node for the child. If the node already existed,
	// don't add it into the tree.
	var child = Shape.INHERITANCE_TREE.getElementsByTagName(childClassName);
	if (child.length <= 0) {
		
		// Look for the node that parent class is in
		var parent = Shape.INHERITANCE_TREE.getElementsByTagName(parentClassName);		
		
		if (parent.length == 1) {
			parent[0].appendChild(Shape.INHERITANCE_TREE.createElement(childClassName));			
		} else {
			throw "Error: There are MORE than one class with name '" + parentClassName + "'.";
		}
	}
}




/*
	Check to see this Shape is a type of a specific Shape. Return true if this class Name is the same
	as the parent class name or if this class extends from a class with the name 'parentClassName'.
	Return false otherwise.
*/
Shape.prototype.typeOf = function (parentClassName) {
	try {
	
		if (this._className == parentClassName) return true;
		
		
		var parents = Shape.INHERITANCE_TREE.getElementsByTagName(parentClassName);

		if (parents.length == 1) {
			var parent = parents[0];
			var subClasses = parent.getElementsByTagName(this._className);
			
			if (subClasses.length == 1) {
				return true;
			} else {
				return false;
			}			
		} else {
			return false;
		}

	} catch (e) {
		return false;
	}
}






/*
	EACH type of shape SHOULD override this function if it NEEDs to draw its own UI.
	If a type of shape EXTENDS another type of shape and it also want to use the parent's
	shape's UI then it DOES NOT need to override this function.
*/
Shape.prototype.setupUI = function () {
}









/*
	Get the default width for the shape. If a specific shape has different default shape,
	it should override this method and return a different value.
*/
Shape.prototype.getDefaultWidth = function () {
	return 200;
}




/*
	Get the default height for the shape. If a specific shape has different default shape,
	it should override this method and return a different value.
*/
Shape.prototype.getDefaultHeight = function () {
	return 25;
}





/*
	Select or deselect the shape. When the shaped is selected, 6 little boxed will
	be drawn around the shape so the user can resize the shape.
*/
Shape.prototype.setSelected = function (value) {

	if(value == true) {
		// deselect the current selected shape
		if (Shape.CURRENT_SELECTED_SHAPE != null) {
			Shape.CURRENT_SELECTED_SHAPE.setSelected(false);
		}
		
		// make this shape the current selected shape
		Shape.CURRENT_SELECTED_SHAPE = this;
	} else {
		if (Shape.CURRENT_SELECTED_SHAPE == this) {
			Shape.CURRENT_SELECTED_SHAPE = null;
		}
	}

	
	// if the shape is resizable, then we show the resize boxes when the shape is selected
	if (this.resizable) {
		if (value == true) {
	
			for (var i = 0; i < this.resizeBoxes.length; i++) {
				if (this.resizeBoxes[i] != null)
					this.resizeBoxes[i].setVisible(true);
			}
	
		} else if (value == false) {
	
			for (var i = 0; i < this.resizeBoxes.length; i++) {
				if (this.resizeBoxes[i] != null)
					this.resizeBoxes[i].setVisible(false);
			}

		}
	}
	
	
	// Set the isSelected to the correct value AND call the object to show or hide the selection UI
	if (value == true) {
		this.isSelected = true;
		this.showSelection();
		DragEvent.enableDragEvents(this);
	} else if (value == false) {
		this.isSelected = false;
		this.hideSelection();
		DragEvent.disableDragEvents(this);
	}
}




/*
	By DEFAULT, when a shape is selected, the background color will be changed.
	If a specific shape want a different way to show that the shape is selected,
	it has to OVERRIDE this function
*/
Shape.prototype.showSelection = function () {
	this.previousBgColor = this.getBgColor();
	this.setBgColor("#dadada");
}





/*
	By DEFAULT, when a shape is deselected, the background color will be changed.
	If a specific shape want a different way to show that the shape is unselected,
	it has to OVERRIDE this function
*/
Shape.prototype.hideSelection = function () {
	var color = this.previousBgColor;
	this.setBgColor(color);
}






/*
	A basic doesn't have any UI to update but if a specific type of shape needs to
	update its' UI, it needs to override this function.
*/
Shape.prototype.updateUI = function () {}







Shape.prototype.setEditable = function (editable) {
	this.editable = editable;
}






Shape.prototype.isEditable = function () {
	return this.editable;
}



Shape.prototype.generatePythonCode = function (levelNumber) {
	return null;
}




/*
	Serialize this shape into an XML DOM element so that it can be saved and restored later on
	Return a DOM element for the shape
*/
Shape.prototype.serializeToXML = function (levelNumber) {
	return null;
}







