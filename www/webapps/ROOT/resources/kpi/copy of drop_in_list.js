

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
	this.resizeListener = {
		onresize: function (e) {
			e.getSource().parent.updateUI();
		}
	};
	
	
	
	this.dropEvents = {
		ondragend: function (e) {

			var droppedItem = e.getSource();
			var dropInList = droppedItem.parent;
			var items = dropInList.items;
			
			
			
			// Index will be the index that the dropped item is to be placed in.
			// By default, index will be the first index of the list of items,
			// which mean the dropped item will be placed at the begining of the list.
			var index = 0;
			

			// check to see where the item is dropped. If it is dropped lower than one
			// of the items in the list, we will place the new item behind that item instead
			// of at the begining of the list
			
			var n = items.getSize();			// the size of the list of items
			
			// the vertical and horizontal location of the CENTER of the dropped item.
			// to find out which item is higher or lower, we will be comparing the Y
			// location of the CENTER of the items, NOT the Y location of the items.
			droppedItemVCenter = droppedItem.getPageY() + (droppedItem.getHeight() / 2);
			droppedItemHCenter = droppedItem.getPageX() + (droppedItem.getWidth() / 2);
			

			for (var i = 0; i < n; i++) {

				var item = items.getElementAt(i);			// the current item to check

				if (item != droppedItem) {

					// if the drop in list is VERTICALLY oriented, we will compare the items by
					// the Y location of the CENTER
					if (dropInList.orientation == DropInList.VERTICAL) {
						itemVCenter = item.getPageY() + (item.getHeight() / 2);

						// if the dropped item is higher (more to the top) than one of the items in the list,
						// we will place the dropped item before that item.
						if (itemVCenter >= droppedItemVCenter) {
							// we found an item that is lower than the dropped item, we can stop looking
							break;
						}
					} else if (dropInList.orientation == DropInList.HORIZONTAL) {
						// if the drop in list is HORIZONTALLY oriented, we will compare the items by their X location
					
						itemHCenter = item.getPageX() + (item.getWidth() / 2);
	
						// if the dropped item is higher (more to the left) than one of the items in the list,
						// we will place the dropped item before that item.
						if (itemHCenter >= droppedItemHCenter) {
							// we found an item that is lower than the dropped item, we can stop looking
							break;
						}
					}
				}
				
				index++;
			}
			
			
			// at this point, index is either the index of the item that is lower than the dropped item
			// OR the last index if NO item is found to be lower than the dropped item.
			
			
			// Assume the dropped item is already in the list, check which index it is in the list
			var currentIndex = items.indexOf(droppedItem);
			
			
			if (currentIndex != null && currentIndex >= 0 && currentIndex <= items.getSize()) {

				if (currentIndex == index - 1) {
					// if the dropped item's current index is ONE index smaller then where it is dropped
					// that mean it is dropped behind itself so we don't need to do anything
				} else if (currentIndex >= index) {
				
					items.removeElementAt(currentIndex);
					items.insertElementAt(droppedItem, index);
					
					// fire the item MOVED event. Notify ALL the item listeners that an item is MOVED to other index
					dropInList.fireItemMovedEvent(droppedItem);
				} else if (currentIndex < index - 1) {
				
					items.removeElementAt(currentIndex);			// because we removed the item, ALL the items will
					items.insertElementAt(droppedItem, index - 1);  // be shifted 1 index down so we need to insert it at the (index - 1)
					
					// fire the item MOVED event. Notify ALL the item listeners that an item is MOVED to other index
					dropInList.fireItemMovedEvent(droppedItem);
				}

			}
			 
			dropInList.updateUI();

		},
		
		
		
		
		// add drop event for the item also because sometime the user will drop some object
		// to the item instead of to the list
		ondrop: function (e, droppedItem) {
			
			var item = e.getSource();				// this is the item that the dropped item is dropped into
			var dropInList = item.parent;
			var items = dropInList.items;

			
			// This event WILL ALSO be called when then list is being dragged and dropped,
			// NOT just for the items. So we need to check what is being dropped before continue
			// SO if the dropped item is the list itself or the list's container, don't do anything
			if (droppedItem != dropInList && droppedItem != dropInList.parent) {

				// if the dropped item is NOT YET added to the drop in list, add drag & drop event to it
				// and also add it to the list
				if (droppedItem.parent != dropInList) {
				
					var droppedItemParent = droppedItem.parent;
					
					droppedItem.removeFromParent();
					droppedItem.addEventListener(dropInList.resizeListener);
					items.addElement(droppedItem);
					
					// NOTE: after the droppedItem is added to the dropInList, the location CHANGED.
					// So we need to keep track of the current location before add it to the list
					var currentX = droppedItem.getPageX();
					var currentY = droppedItem.getPageY();
					
					dropInList.addChild(droppedItem);
					droppedItem.setLocation(currentX - dropInList.getPageX(), currentY - dropInList.getPageY());
					droppedItem.addEventListener(dropInList.dropEvents);
					
					
					/*
						TODO - Add proper event to handle item being moved out of a DropInList
						Right now, we are just going to check if the item is being removed from some
						DropInList. If it is, we have to remove it from the items vector, NOT just
						from the layer
					*/					
					try {
						if (droppedItemParent._className == "DropInList") {
							droppedItemParent.removeItem(droppedItem);
						}
					} catch (ex) { alert(e.message || e); }


					// fire the item add event. Notify ALL the item listeners that an item is added
					dropInList.fireItemAddedEvent(droppedItem);
				}
				
			}
		}
		
	};
	
	
	
	
	
	this.addEventListener({
		ondrop: function (e, droppedItem) {
		
			var dropInList = e.getSource();
			var items = dropInList.items;
			

			// This event WILL ALSO be called when then list is being dragged and dropped,
			// NOT just for the items. So we need to check what is being dropped before continue
			// SO if the dropped item is the list itself or the list's container, don't do anything
			if (droppedItem != dropInList && droppedItem != dropInList.parent) {
				
				var droppedItemParent = droppedItem.parent;
					

				// if the dropped item is NOT YET added to the drop in list, add drag & drop event to it
				// and also add it to the list
				if (droppedItem.parent != dropInList) {
					droppedItem.removeFromParent();
					droppedItem.addEventListener(dropInList.resizeListener);
					items.addElement(droppedItem);
					
					// NOTE: after the droppedItem is added to the dropInList, the location CHANGED.
					// So we need to keep track of the current location before add it to the list
					var currentX = droppedItem.getPageX();
					var currentY = droppedItem.getPageY();
					
					dropInList.addChild(droppedItem);
					droppedItem.setLocation(currentX - dropInList.getPageX(), currentY - dropInList.getPageY());
					droppedItem.addEventListener(dropInList.dropEvents);
				}
				


				/*
					TODO - Add proper event to handle item being moved out of a DropInList
					Right now, we are just going to check if the item is being removed from some
					DropInList. If it is, we have to remove it from the items vector, NOT just
					from the layer
				*/
				
				try {
					if (droppedItemParent._className == "DropInList") {
						droppedItemParent.removeItem(droppedItem);
					}
				} catch (ex) { alert(e.message || e); }

				
				// fire the item add event. Notify ALL the item listeners that an item is added
				dropInList.fireItemAddedEvent(droppedItem);
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
	Programatically add a new item to the list
*/
DropInList.prototype.addItem = function (item) {
	if (item != null) {
		this.items.addElement(item);
		this.addChild(item);
		item.addEventListener(this.resizeListener);
		this.updateUI();
		
		this.fireItemAddedEvent(item);
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
			item.addEventListener(this.resizeListener);
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
		item.removeEventListener(this.resizeListener);
		item.removeEventListener(this.resizeListener);
		item.removeEventListener(this.dropEvents);
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
		item.removeEventListener(this.resizeListener);
		
		
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
		item.addEventListener(this.resizeListener);
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







