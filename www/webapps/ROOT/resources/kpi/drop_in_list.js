

function DropInEvent (source, item) {
	this.source = source;						// the reference to the DropInList
	this.item = item;							// the item that caused the event
	
	this.getSource = function () {
		return source;	
	}
	
	
	this.getItem = function () {
		return item;
	}
}






function DropInList (x, y, w, h, orient, alignment, padding, spacing) {
	this.Dynlayer = DynLayer;
	this.Dynlayer(null, x, y, w, h);
	
	// Set the orientation of the LIST ITEMS, not the list.
	// Default will be VERTICAL if orientation is not specified
	// If orientation is VERTICAL, items will be place from top to bottom.	
	// If orientation is HORIZONTAL, items will be place from left to right.
	this.orientation = (orient != null && orient == DropInList.HORIZONTAL)? DropInList.HORIZONTAL: DropInList.VERTICAL;
	
	
	// Set the alignment of the items in the list. The possible alignments are
	// DropInList.TOP_ALIGNMENT, DropInList.BOTTOM_ALIGNMENT, DropInList.MIDDLE_ALIGNMENT for HORIZONTAL orientation
	// DropInList.LEFT_ALIGNMENT, DropInList.RIGHT_ALIGNMENT, DropInList.CENTER_ALIGNMENT for VERTICAL orientation
	// DropInList.AUTO_STRETCH
	//
	// TOP and LEFT alignment are the same, BOTTOM and RIGHT alignment are the same and the MIDDLE and CENTER alignment
	// are the same. So it the user set the orientation to VERTICAL but the the alignment to TOP, the list items
	// will be aligned to the LEFT instead.
	// If the user does not specify the alignment, the default will be CENTER or MIDDLE
	// AUTO_STRETCH mean the items will be stretched VERTICALLY or HORIZONTALLY depend on the orientation.
	// If orientation is VERTICAL and alignment is AUTO_STRETCH then the items will be stretched HORZONTALLY
	// If orientation is HORIZONTAL and alignment is AUTO_STRETCH then the items will be stretched VERTICALLY
	if (alignment != null && (alignment == DropInList.TOP_ALIGNMENT || 
							  alignment == DropInList.BOTTOM_ALIGNMENT ||
							  alignment == DropInList.MIDDLE_ALIGNMENT ||
							  alignment == DropInList.LEFT_ALIGNMENT ||
							  alignment == DropInList.RIGHT_ALIGNMENT ||
							  alignment == DropInList.CENTER_ALIGNMENT ||
							  alignment == DropInList.AUTO_STRETCH)) {
		this.alignment = alignment;
	} else {
		this.alignment = DropInList.CENTER_ALIGNMENT;
	}
	
	
	
	// the top, bottom, left, right padding space of the list
	if (padding != null && padding >= 0) {
		this.padding = padding;
	} else {
		this.padding = DropInList.DEFAULT_PADDING;
	}

	

	// the space between the items in the list
	if (spacing != null && spacing >= 0) {
		this.itemSpacing = spacing;
	} else {
		this.itemSpacing = DropInList.DEFAULT_ITEM_SPACING;
	}
	
	
	
	// create a Vector to hold the items in the list
	this.items = new Vector();
	

	
	// create a vector to hold the items listeners
	this.itemListeners = new Vector();

	


	
	// this is the items' resize listener. When one of the items is resized, we need to update the list's UI
	this.itemResizeListener = {
		onresize: function (e) {
			e.getSource().parent.updateUI();
		}
	};





	/*
		Drag and Drop event for the items
	*/
	this.itemDragAndDropListener = {
	
		// When an item is starts to move, we will REMOVE it from the list right away and ADD it to
		// the top level document. This way, we can MOVE the item OUTSIDE the list. This will allow
		// the item to be moved around from one list to another.
		// If we DON'T remove the item, the user can ONLY drag it inside the list but NOT out side

		ondragmove: function (e) {
			var item = e.getSource();
			var dropInList = item.parent;
			
			if (dropInList != null) {
				var previousX = item.getPageX();
				var previousY = item.getPageY();
				
				dropInList.removeItem(item);					// remove the item from the list
				dynapi.document.addChild(item);					// add the item to the top level document
				item.setLocation(previousX, previousY);
				item.setZIndex({topmost:true});
				
				
				// We NEED to cancel the drag event and start the drag event again or else the
				// mouse location and the item location will get messed up. The reason this happened
				// because we CHANGED the item's parent from the drop in list to the dynapi.document
				e.cancelDrag();
				DragEvent.startDrag(e, item);

			}
		},
		
		


		/*
			Listen to the drop event ON an ITEM, not the LIST. Sometime the user will drop an item
			on top of one of the items in the list. If this happen, we will add the dropped item to
			the list also.
		*/
		ondrop: function (e, droppedItem) {
			e.preventBubble();
			
			// item = the item that IS ALREADY in the list which is having an object dropped on top
			// of it. The reason we can GUARANTEE this item is ALREADY in the list because ONLY item
			// that is in the list will have this LISTENER. When an item is REMOVED from the list,
			// this listener will be removed
			
			var item = e.getSource();
			var dropInList = item.parent;

						
			// IMPORTANT. 
			// Check "droppedItem.dropInListParent" to see if the item is already
			// BELONGED to a drop in list to prevent the item being added to more
			// than one list. This can happen when we have a drop in list INSIDE
			// another drop in list
			if (droppedItem.dropInListParent == null && droppedItem != dropInList && (! droppedItem.isParentOf(dropInList))) {
				dropInList.addItem(droppedItem, true);
			}
			
		}
	};




	/*
		Add drop event to the list so that the user can drop items
		to the list.
	*/
	this.addEventListener({
	
		ondrop: function (e, droppedItem) {
		
			e.preventBubble();

			var dropInList = e.getSource();


			// IMPORTANT. 
			// Check "droppedItem.dropInListParent" to see if the item is already
			// BELONGED to a drop in list to prevent the item being added to more
			// than one list. This can happen when we have a drop in list INSIDE
			// another drop in list
			if (droppedItem.dropInListParent == null && droppedItem != dropInList && (! droppedItem.isParentOf(dropInList))) {
				dropInList.addItem(droppedItem, true);
			}
			
		}
		
	});
	
	
	
	
	
	this.updateUI();
}
dynapi.setPrototype('DropInList', 'DynLayer');


DropInList.VERTICAL = "vertical";
DropInList.HORIZONTAL = "horizontal";
DropInList.LEFT_ALIGNMENT = "left";
DropInList.RIGHT_ALIGNMENT = "right";
DropInList.TOP_ALIGNMENT = "top";
DropInList.BOTTOM_ALIGNMENT = "bottom";
DropInList.CENTER_ALIGNMENT = "center";
DropInList.MIDDLE_ALIGNMENT = "middle";
DropInList.AUTO_STRETCH = "auto stretch";
DropInList.DEFAULT_PADDING = 10;
DropInList.DEFAULT_ITEM_SPACING = 10;




/*
	When the list is set to auto size, there will be some problems with stack overflow and
	infinite loop. Therefore, we need to disable this function by OVERRIDING it and leave
	the function blank.
*/
DropInList.prototype.setAutoSize = function (w, h) {
	// do nothing
}





/*
	Set the orientation of the LIST ITEMS, not the list.
	If orientation is VERTICAL, items will be place from top to bottom.	
	If orientation is HORIZONTAL, items will be place from left to right.
	
	Paremeters:
	REQUIRED - orient - The new orientation. The ONLY posible values are DropInList.VERTICAL & DropInList.HORIZONTAL
*/
DropInList.prototype.setOrientation = function (orient) {
	if (orient != null) {
	
		// If the new orientation is NOT the same as the current orientation &
		// the new orientation is either DropInList.VERTICAL OR DropInList.HORIZONTAL
		// then change it and update the list UI. Else, don't do anything.
		if (orient != this.orientation && (orient == DropInList.VERTICAL || orient == DropInList.HORIZONTAL)) {
			this.orientation = orient;
			this.updateUI();			
		}
	}
}



/*
	Set the alignment of the items in the list. The possible alignments are
	DropInList.TOP_ALIGNMENT, DropInList.BOTTOM_ALIGNMENT, DropInList.MIDDLE_ALIGNMENT for VERTICAL orientation
	DropInList.LEFT_ALIGNMENT, DropInList.RIGHT_ALIGNMENT, DropInList.CENTER_ALIGNMENT for HORIZONTAL orientation
	TOP and LEFT alignment are the same, BOTTOM and RIGHT alignment are the same and the MIDDLE and CENTER alignment
	are the same. So it the user set the orientation to VERTICAL but the the alignment to LEFT, the list items
	will be aligned to the TOP instead.
*/
DropInList.prototype.setAlignment = function (alignment) {
	if (alignment != null && (alignment == DropInList.TOP_ALIGNMENT || 
							  alignment == DropInList.BOTTOM_ALIGNMENT ||
							  alignment == DropInList.MIDDLE_ALIGNMENT ||
							  alignment == DropInList.LEFT_ALIGNMENT ||
							  alignment == DropInList.RIGHT_ALIGNMENT ||
							  alignment == DropInList.CENTER_ALIGNMENT)) {
		this.alignment = alignment;
		this.updateUI();
	}	
}






/*
	Set the left, right, top, and bottom padding of the list. The padding MUST be greater than 0
*/
DropInList.prototype.setPadding = function (padding) {
	if (padding != null && padding >= 0) {
		this.padding = padding;
		this.updateUI();
	}
}






/*
	Set the space between the items in the list. The space MUST be greater than 0
*/
DropInList.prototype.setItemSpacing = function (spacing) {
	if (spacing != null && spacing >= 0) {
		this.itemSpacing = spacing;
		this.updateUI();
	}
}





/*
	Get the number of items in the list
*/
DropInList.prototype.getSize = function () {
	return this.items.getSize();
}






/*
	Add a new item to the list
*/
DropInList.prototype.addItem = function (newItem, compareLocation) {

	// If compareLocation is NOT required, add the new item to the end of the list
	if (newItem != null && compareLocation != true) {
	
		// make sure the newItem is NOT already in the list. If it is, don't add it to the list anymore
		if (this.items.indexOf(newItem) < 0) {
			this.items.addElement(newItem);						// add new item to the list
			this.addChild(newItem);								// add new item to the layer



			// IMPORTANT
			// add this property to the item so that we know this item is already
			// belonged to a drop in list. This will be used to prevent item being
			// added to multiple drop in list. This can happen when a drop in list
			// contain another drop in list. When the user drop an item to the INTERNAL
			// drop in list, the item WILL ALSO be added to the EXTERNAL drop in list
			// after it is being added to the internal drop in list. In this case, the
			// item IS ONLY supposed to be added to the INTERNAL drop in list so we
			// have to set this property AFTER the item is ADDED to the INTERNAL drop in
			// When the item is trying to be added to the EXTERNAL drop in list, we will
			// use this property to check if an item is already in a drop in list. If yes,
			// we will stop trying to add it to the EXTERNAL drop inlist.
			newItem.dropInListParent = this;
			
			

			newItem.addEventListener(this.itemResizeListener);
			newItem.addEventListener(this.itemDragAndDropListener);

			this.updateUI();			
			this.fireItemAddedEvent(newItem);
		}
		
	} else if (newItem != null) {

		// make sure the newItem is NOT already in the list. If it is, don't add it to the list anymore
		if (this.items.indexOf(newItem) >= 0) {
			return;
		}
		
		
		// If compareLocation is required, add the new item to the location where it is belonged.
		// We need to compare the new item's location with ALL the other items in the list and see
		// where to place the new item.


		// Index will be the index that the new item is to be placed in.
		// By default, index will be the first index of the list of items,
		// which mean the new item will be placed at the begining of the list.
		var index = 0;
		

		// Now check to see where the location of the item is. If it is lower than one
		// of the items in the list, we will place the new item behind that item instead
		// of at the begining of the list



			var n = this.items.getSize();			// the size of the list of items
			
			// the vertical and horizontal location of the CENTER of the new item.
			// to find out which item is higher or lower, we will be comparing the Y
			// location of the CENTER of the items, NOT the Y location of the items.
			newItemVCenter = newItem.getPageY() + (newItem.getHeight() / 2);
			newItemHCenter = newItem.getPageX() + (newItem.getWidth() / 2);
			
	
			for (var i = 0; i < n; i++) {
	
				var item = this.items.getElementAt(i);			// the current item to compare with
	
				// if the drop in list is VERTICALLY oriented, we will compare the items by
				// the Y location of the CENTER
				if (this.orientation == DropInList.VERTICAL) {
					itemVCenter = item.getPageY() + (item.getHeight() / 2);

					// if the new item is higher (more to the top) than one of the items in the list,
					// we will place the new item before the item just BELOW the new item.
					if (itemVCenter >= newItemVCenter) {
						// we found an item that is lower than the new item, we can stop looking
						break;
					}
				} else if (this.orientation == DropInList.HORIZONTAL) {
					// if the drop in list is HORIZONTALLY oriented, we will compare the items by their X location
				
					itemHCenter = item.getPageX() + (item.getWidth() / 2);

					// if the new item is higher (more to the left) than one of the items in the list,
					// we will place the new item before the item just TO THE RIGHT of the new item.
					if (itemHCenter >= newItemHCenter) {
						// we found an item that is lower than the new item, we can stop looking
						break;
					}
				}
				
				index++;
			}
			
			
			// at this point, index is either the index of the item that is lower than the new item
			// OR the last index if NO item is found to be lower than the new item.
			 
			this.insertItemAt(newItem, index);				// insert the item at the specified index
	}
}




 



/*
	Programmatically insert an item to the list. The item MUST NOT be null and the index must
	be greater than 0 and less than or equal to the list's size
*/
DropInList.prototype.insertItemAt = function (item, index) {
	if (item != null && index != null) {
		if (index >= 0 && index <= this.items.getSize()) {
			this.items.insertElementAt(item, index);
			this.addChild(item);


			// IMPORTANT
			// add this property to the item so that we know this item is already
			// belonged to a drop in list. This will be used to prevent item being
			// added to multiple drop in list. This can happen when a drop in list
			// contain another drop in list. When the user drop an item to the INTERNAL
			// drop in list, the item WILL ALSO be added to the EXTERNAL drop in list
			// after it is being added to the internal drop in list. In this case, the
			// item IS ONLY supposed to be added to the INTERNAL drop in list so we
			// have to set this property AFTER the item is ADDED to the INTERNAL drop in
			// When the item is trying to be added to the EXTERNAL drop in list, we will
			// use this property to check if an item is already in a drop in list. If yes,
			// we will stop trying to add it to the EXTERNAL drop inlist.
			item.dropInListParent = this;
			
			

			item.addEventListener(this.itemResizeListener);
			item.addEventListener(this.itemDragAndDropListener);

			this.updateUI();			
			this.fireItemAddedEvent(item);
		}
	}
}



/*
	Return the index of the specified item. Return -1 if item is NOT in the list or item is NULL
*/
DropInList.prototype.indexOf = function (item) {
	if (item != null) {
		return this.items.indexOf(item);
	} else return -1;
}




/*
	Get the item at the specified index. Return NULL if index is out of range (index < 0 && index >= list's size).
*/
DropInList.prototype.getItemAt = function (index) {
	if (index >= 0 && index < this.items.getSize()) {
		return this.items.getElementAt(index);
	} else return null;
}





/*
	Remove an item at the spcified index. Do nothing if index is out of range.
*/
DropInList.prototype.removeItemAt = function (index) {
	if (index >= 0 && index < this.items.getSize()) {
		var item = this.items.removeElementAt(index);
		item.removeEventListener(this.itemResizeListener);
		item.removeEventListener(this.itemDragAndDropListener);
		

		// IMPORTANT: set this property to NULL so that the item can be added to a drop in list again
		item.dropInListParent = null;
		
		
		// THIS IS VERY IMPORTANT. WE HAVE TO REMOVE ANCHOR BECAUSE WHEN THE ITEM'S ANCHOR IS SET,
		// THE ITEM WILL BE LISTENING TO THE PARENT LAYER'S RESIZE EVENT. SO IF WE DON'T REMOVE ANCHOR
		// WHEN THE LIST RESIZE, THIS ITEM WILL BE MOVED ALSO EVEN THOUGH IT DOES NOT BELONG TO THE
		// LIST ANYMORE
		item.setAnchor(null);
		
		this.removeChild(item);
		this.updateUI();
		
		this.fireItemRemovedEvent(item);
	}
}





/*
	Remove an item. Do nothing if item is not in the list.
*/
DropInList.prototype.removeItem = function (item) {
	this.removeItemAt(this.indexOf(item));
}






/*
	Return the vector of items in the list
*/
DropInList.prototype.getItems = function () {
	return this.items;
}





/*
	Get ALL the items. The returened value will be an Array of items
*/
DropInList.prototype.getAllItems = function () {
	return this.items.getAllElements();
}








/*
	Redraw the items in the list. Reset their location base on their orientation, their alignment and their
	order in the list.
*/
DropInList.prototype.updateUI = function () {

	var n = this.items.getSize();
	var maxWidth = -1000000;						// the width of the BIGGEST item
	var maxHeight = -1000000;						// the height of the BIGGEST item
	
	var currentLocation = this.padding;
	
	for (var i = 0; i < n; i++) {
		var item = this.items.getElementAt(i);


		// to prevent from infinite loop, remove the resize event for the item first before setting the items' size
		item.removeEventListener(this.itemResizeListener);
		
		
		if (this.orientation == DropInList.VERTICAL) {
		
			item.setY(currentLocation);

			if (this.alignment == DropInList.TOP_ALIGNMENT || this.alignment == DropInList.LEFT_ALIGNMENT) {
				item.setAnchor({left:this.padding});
			} if (this.alignment == DropInList.BOTTOM_ALIGNMENT || this.alignment == DropInList.RIGHT_ALIGNMENT) {
				item.setAnchor({right:this.padding});
			} if (this.alignment == DropInList.CENTER_ALIGNMENT || this.alignment == DropInList.MIDDLE_ALIGNMENT) {
				item.setAnchor({centerH:0});
			} if (this.alignment == DropInList.AUTO_STRETCH) {
				item.setAnchor({left:this.padding, right:this.padding, stretchH:"100%"});
			}
			
			currentLocation = currentLocation + item.getHeight() + this.itemSpacing;
			
		} else if (this.orientation == DropInList.HORIZONTAL) {
		
			item.setX(currentLocation);
			
			if (this.alignment == DropInList.TOP_ALIGNMENT || this.alignment == DropInList.LEFT_ALIGNMENT) {
				item.setAnchor({top:this.padding});
			} if (this.alignment == DropInList.BOTTOM_ALIGNMENT || this.alignment == DropInList.RIGHT_ALIGNMENT) {
				item.setAnchor({bottom:this.padding});
			} if (this.alignment == DropInList.CENTER_ALIGNMENT || this.alignment == DropInList.MIDDLE_ALIGNMENT) {
				item.setAnchor({centerV:0});
			} if (this.alignment == DropInList.AUTO_STRETCH) {
				item.setAnchor({top:this.padding, bottom:this.padding, stretchV:"100%"});
			}
		
			currentLocation = currentLocation + item.getWidth() + this.itemSpacing;
		}



		maxWidth = Math.max(item.getWidth(), maxWidth);
		maxHeight = Math.max(item.getHeight(), maxHeight);


		// restore the event listener
		item.addEventListener(this.itemResizeListener);
	}


	// resize the list so that it fit EXACTLY the items including the padding
	// if there is no item in the list, the list will be as big as the padding

	if (this.orientation == DropInList.VERTICAL) {
		var item = this.items.getLastElement();
		var listHeight = ((item != null)? item.getY() + item.getHeight(): 0) + this.padding;
		var listWidth = ((maxWidth > 0)? maxWidth: 0) + (this.padding * 2);
		this.setSize(listWidth, listHeight);
	} else if (this.orientation == DropInList.HORIZONTAL) {
		var item = this.items.getLastElement();
		var listWidth = ((item != null)? item.getX() + item.getWidth(): 0) + this.padding;		
		var listHeight = ((maxHeight > 0)? maxHeight: 0) + (this.padding * 2);
		this.setSize(listWidth, listHeight);
	}
	


}






	
/*
	Add a new item listener to the listener list. When an item is added, removed, or moved from this list,
	ALL the item listeners will be notified through the "itemAdded", "itemRemoved", "itemMoved" function.
	The listener MUST implement the callback function "itemAdded", "itemRemoved", or "itemMoved" or else
	it won't be notified
*/
DropInList.prototype.addItemListener = function (listener) {
	if (listener != null) {
		this.itemListeners.addElement(listener);
	}
}





/*
	Remove a listener from the item listener list.
*/
DropInList.prototype.removeItemListener = function (listener) {
	if (listener != null) {
		// look for the listener
		var index = this.itemListeners.indexOf(listener);
		
		// if the listener is in the list, remove it
		if (index != null && index >= 0) {
			this.itemListemers.removeElementAt(index);
		}
	}
}




/*
	Define the fireItemAddedEvent function. This function will be called when an item is added
	to the list. The user DOES NOT have to call this method. This method will be called
	by one of the functions of this list.
*/
DropInList.prototype.fireItemAddedEvent = function (item) {
	
	var event = new DropInEvent(this, item);

	var n = this.itemListeners.getSize();
	
	for (var i = 0; i < n; i++) {
		try {
			var listener = this.itemListeners.getElementAt(i);
			listener.itemAdded(event);
		} catch (ex) {}		// ignore exception if the listener DID NOT implement the itemAdded callback function
	}
}





/*
	Define the fireItemRemovedEvent function. This function will be called when an item is removed
	from the list. The user DOES NOT have to call this method. This method will be called
	by one of the functions of this list.
*/
DropInList.prototype.fireItemRemovedEvent = function (item) {
	
	var event = new DropInEvent(this, item);

	var n = this.itemListeners.getSize();
	for (var i = 0; i < n; i++) {
		try {
			var listener = this.itemListeners.getElementAt(i);
			listener.itemRemoved(event);
		} catch (ex) {}		// ignore exception if the listener DID NOT implement the itemRemoved callback function
	}
}






/*
	Define the fireItemMovedEvent function. This function will be called when an item is MOVED
	from the list. The user DOES NOT have to call this method. This method will be called
	by one of the functions of this list.
	
	Item moved mean the item is ALREADY in the list but is moved to another index
*/
DropInList.prototype.fireItemMovedEvent = function (item) {
	
	var event = new DropInEvent(this, item);

	var n = this.itemListeners.getSize();
	for (var i = 0; i < n; i++) {
		try {
			var listener = this.itemListeners.getElementAt(i);
			listener.itemMoved(event);
		} catch (ex) {}		// ignore exception if the listener DID NOT implement the itemRemoved callback function
	}
}







