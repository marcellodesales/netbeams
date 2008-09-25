






function ToolBarItem (imageFile, text, x, y, w, h) {
	this.Dynlayer = DynLayer;
	this.Dynlayer(null, x, y, w, h);

	this.setCursor("hand");
	
	this.imageFile = imageFile;
	var image = "<img src='" + this.imageFile + "' />";
	this.setHTML("<table width='100%' height='100%' valign='middle'><tr><td align='center' valign='middle' width='" + h + "px'>" + image + "</td><td align='left' valign='middle'>" + text + "</td></tr></table>");
	
	
	// add mouse listener for the tool bar item. When the mouse move over the
	// tool bar item, we draw a border around it
	this.addEventListener({
		onmouseover: function (e) {
			toolBarItem = e.getSource();
			toolBarItem.setBgColor("#C1D5E8");
		},
		
		onmouseout: function (e) {
			toolBarItem = e.getSource();
			toolBarItem.setBgColor("#ffffff");
		}
	});
}
dynapi.setPrototype('ToolBarItem', 'DynLayer');

ToolBarItem.DEFAULT_TEXT_HEIGHT = 20;







function ToolBar (x, y, w, h) {
	this.Dynlayer = DynLayer;
	this.Dynlayer(null, x, y, w || ToolBar.DEFAULT_WIDTH, h || ToolBar.DEFAULT_HEIGHT);
	this.setBorder(1, "solid", "#000000");
	this.setBgColor("#ffffff");


	this.toolBarWidth = this.getWidth();
	this.toolBarHeight = this.getHeight();
	




	// setup title bar
	title = this.addChild(new DynLayer(null, null, null, ToolBar.DEFAULT_TITLE_WIDTH, this.toolBarHeight, "#25425F"));
	title.setBorder(1, "solid", "#000000");
	title.setAnchor({top:0, right:0, bottom:0, stretchV:'100%'})

	titleText = title.addChild(new DynLayer("<div align='center' style='font-size:12px; font-weight:bold; color:#ffffff; line-height:2.0'>T<br/>o<br/>o<br/>l<br/>b<br/>a<br/>r</div>", 0, 50, ToolBar.DEFAULT_TITLE_WIDTH, 200));
	titleText.setAnchor({left:0, stretchV:'100%', stretchH:'100%'})


	this.openButton = title.addChild(new DynLayer("<img src='" + KPI.RESOURCES_PATH + "/ToolBarOpenButton.gif' alt='Open the toolbar'/>", null, null, 10, 19));
	this.openButton.setAnchor({top:5, centerH:0});
	this.openButton.setVisible(false);
	this.openButton.addEventListener({
		onclick:function(e){
			openButton = e.getSource();
			closeButton.parent.parent.setWidth(openButton.parent.parent.toolBarWidth);
			openButton.setVisible(false);
			openButton.parent.parent.closeButton.setVisible(true);
		},

		onmouseover:function(e){
			openButton = e.getSource();
			openButton.setCursor("hand");
		}
	});


	this.closeButton = title.addChild(new DynLayer("<img src='" + KPI.RESOURCES_PATH + "/ToolBarCloseButton.gif' alt='Close the toolbar'/>", null, null, 10, 19));
	this.closeButton.setAnchor({top: 5, centerH:0});
	this.closeButton.addEventListener({
		onclick:function(e){
			closeButton = e.getSource();
			closeButton.parent.parent.setWidth(ToolBar.DEFAULT_TITLE_WIDTH);
			closeButton.setVisible(false);
			closeButton.parent.parent.openButton.setVisible(true);
		},

		onmouseover:function(e){
			closeButton = e.getSource();
			closeButton.setCursor("hand");
		}
	});



	// create a layer for the tools
	this.contentLayer = new DynLayer();
	this.contentLayer.setAnchor({left:0, top:0, bottom:0, right:ToolBar.DEFAULT_TITLE_WIDTH, stretchV:"00%", stretchH:"100%"});
	this.contentLayer.setBgColor("#ffffff");
	this.contentLayer.setAutoSize(false, true);
	this.addChild(this.contentLayer);
	
	
	// create a vector to hold all the tool bar items
	this.toolBarItems = new Vector(100);
	this.itemSpacing = ToolBar.DEFAULT_ITEM_SPACING;
}
dynapi.setPrototype('ToolBar', 'DynLayer');
ToolBar.DEFAULT_TITLE_WIDTH = 20;
ToolBar.DEFAULT_WIDTH = 130;
ToolBar.DEFAULT_HEIGHT = 450;
ToolBar.DEFAULT_ITEM_SPACING = 10;




ToolBar.prototype.setItemSpacing = function (size) {
	if (size != null && size >= 0) {
		this.itemSpacing = size;
	}
}



ToolBar.prototype.addItem = function (item) {
	if (item != null) {
		this.toolBarItems.addElement(item);
		this.updateUI();
	}
}




ToolBar.prototype.updateUI = function () {
	var n = this.toolBarItems.getSize();
	var currentY = this.itemSpacing;
	
	for (var i = 0; i < n; i++) {
		var item = this.toolBarItems.getElementAt(i);
		item.setAnchor({centerH:0});
		item.setY(currentY);
		this.contentLayer.addChild(item);
		
		currentY = currentY + item.getHeight() + this.itemSpacing;
	}
}






