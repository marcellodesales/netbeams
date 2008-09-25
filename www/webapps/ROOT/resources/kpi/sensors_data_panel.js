


function getMeasurementName (dom) {
	for (var i = 0; i < dom.childNodes.length; i++) {
		var child = dom.childNodes[i];
		if (child.nodeType == 1 && child.tagName == "metadata" && child.getAttribute("name").toLowerCase() == "name") {
			return child.childNodes[0].nodeValue;
		}
	}
	
	throw "Invalid SDML DOM tree. The measurement DOES NOT have a name.";
}





/*
	Because metadata CAN BE nested so we need to create this function to RECURSIVELY build a
	metadata element.
*/
function createMetadataElement (dom, metadataSource, description, code) {
	var metadata = dom.createElement("metadata");
	metadata.setAttribute("name", metadataSource.getAttribute("name"));
	
	var newDescription = description + " -> " + metadataSource.getAttribute("name");
	metadata.setAttribute("description", newDescription + " metadata.");

	var newCode = code + ".getMetadata(\"" + metadataSource.getAttribute("name") + "\")";
	metadata.setAttribute("code", newCode);
	
	for(var i = 0; i < metadataSource.childNodes.length; i++) {
		var child = metadataSource.childNodes[i];
		if (child.nodeType == 1 && child.tagName == "metadata") {
			var childMetadata = createMetadataElement(dom, child, newDescription, newCode);
			metadata.appendChild(childMetadata);
		}
	}
	
	if (metadata.childNodes.length <= 0) {
		metadata.setAttribute("description", metadata.getAttribute("description") + " The returned value will be a String object.");
		metadata.setAttribute("code", metadata.getAttribute("code") + ".getValueString()");
	} else {
		metadata.setAttribute("description", metadata.getAttribute("description") + " The returned value will be a Metadata object.");
	}

	return metadata;
}





/*
	Because Measurement Value CAN BE nested so we need to create this function to
	RECURSIVELY build a Measurement Value element.
*/
function createMeasurementValueElement (dom, valueSource, name, description, code, index) {
	var value = dom.createElement("value");
	
	var newName = name + "[" + index + "]";
	value.setAttribute("name", newName);
	
	var newDescription = description + "[" + index + "]";
	value.setAttribute("description", newDescription + ".");

	var newCode = code + ".getValues()[" + index + "]";
	value.setAttribute("code", newCode);
	
	var count = 0;
	for(var i = 0; i < valueSource.childNodes.length; i++) {
		var child = valueSource.childNodes[i];
		if (child.nodeType == 1 && child.tagName == "value") {
			var childValue = createMeasurementValueElement(dom, child, newName, newDescription, newCode, count);
			value.appendChild(childValue);
			count++;
		}
	}
	
	if (value.childNodes.length <= 0) {
		value.setAttribute("description", value.getAttribute("description") + " The returned value will be a String object.");
		value.setAttribute("code", value.getAttribute("code") + ".getValueString()");
	} else {
		value.setAttribute("description", value.getAttribute("description") + " The returned value will be a MeasurementValue object.");
	}

	return value;
}







function SensorsDataPanel (sensorNames, sensorDataURLs) {

	this.Dynlayer = DynLayer;
	this.Dynlayer();


	this.sensorNames = sensorNames;
	this.sensorDataURLs = sensorDataURLs;
	this.sdmlDOMs = null;
	this.sensorsDataDOM = null;
	this.rootDIV = null;						// the reference to the DIV element at the TOP (Sensors element)
	this.targetObject = null;
	
	
	
	this.titleLayer = new DynLayer("<table width='100%' height='100%' valign='middle' align='center' ><tr><td><b><font color='#000000'><center>Select the data that you want to return</center></font></b></td></tr></table>", 0, 0, null, 25, "transparent");
	this.titleLayer.setBgImage(getResourcesPath() + "panel_title_bg.jpg");
	this.titleLayer.setAnchor({left:0, right:0, stretchH:"100%"});
	this.titleLayer.setCursor("pointer");
	this.addChild(this.titleLayer);
	DragEvent.enableDragEvents(this.titleLayer);
	this.titleLayer.addEventListener({
		mouseX: 0,
		mouseY: 0,

		ondragstart: function (e) {
			this.mouseX = e.getPageX();
			this.mouseY = e.getPageY();
		},
		
		ondragmove: function (e) {
			var titleLayer = e.getSource();

			var diffX = e.getPageX() - this.mouseX;
			var diffY = e.getPageY() - this.mouseY;
			
			var parent = titleLayer.parent;
			titleLayer.setLocation(0, 0);
			parent.setLocation(parent.getX() + diffX, parent.getY() + diffY);
			
			this.mouseX = e.getPageX();
			this.mouseY = e.getPageY();
		}		
	});
	
	
	
	this.closeButton = new DynLayer("<img src='" + getResourcesPath() + "win_close.gif' border='0px'>", 0, 0, 15, 15, "transparent");
	this.closeButton.setAnchor({right:6, top:4});
	this.closeButton.setCursor("pointer");
	this.titleLayer.addChild(this.closeButton);
	this.closeButton.onclick = function () {
		this.parent.parent.setVisible(false);
		this.parent.parent.targetObject = null;
		this.parent.parent.setLocation(-1000, -1000);
	}

	this.closeButton.onmouseover = function () {
		this.setBorder(1, "solid", "#000000");
	}

	this.closeButton.onmouseout = function () {
		this.setBorder(0, "solid", "transparent");
	}

	this.collapseButton = new DynLayer("<img src='" + getResourcesPath() + "win_min.gif' border='0px'>", 0, 0, 15, 15, "transparent");
	this.collapseButton.setAnchor({right:48, top:4});
	this.collapseButton.setCursor("pointer");
	this.titleLayer.addChild(this.collapseButton);
	this.collapseButton.onclick = function () {
		this.parent.parent.rootDIV._collapseAll();
	}
	
	this.collapseButton.onmouseover = function () {
		this.setBorder(1, "solid", "#000000");
	}

	this.collapseButton.onmouseout = function () {
		this.setBorder(0, "solid", "transparent");
	}



	this.expandButton = new DynLayer("<img src='" + getResourcesPath() + "win_max.gif' border='0px'>", 0, 0, 15, 15, "transparent");
	this.expandButton.setAnchor({right:27, top:4});
	this.expandButton.setCursor("pointer");
	this.titleLayer.addChild(this.expandButton);
	this.expandButton.onclick = function () {
		this.parent.parent.rootDIV._expandAll();
	}

	this.expandButton.onmouseover = function () {
		this.setBorder(1, "solid", "#000000");
	}

	this.expandButton.onmouseout = function () {
		this.setBorder(0, "solid", "transparent");
	}

	
	

	this.contentLayer = new DynLayer();
	this.contentLayer.setHeight(10);
	this.contentLayer.setBorder(1, "solid", "#000000");
	this.contentLayer.setOverflow("auto");
	this.contentLayer.setAnchor({left:0, right:0, top:25, bottom:0, stretchV:"100%", stretchH:"100%"});
	this.addChild(this.contentLayer);

	

	try {
	
		this.sdmlDOMs = new Array(sensorNames.length);
		this.sensorsDataDOM = Sarissa.getDomDocument();		// this is the DOM tree that contain data for ALL sensors

		this.initialize();
	
	} catch (e) {
		alert(e.message || e);
	}

}
dynapi.setPrototype('SensorsDataPanel', 'DynLayer');





/*
	targetObject is the object that called this SensorsData panel. When the user select one of the
	sensors' data, the targetObject will be notified though the "setSourceCode" function. The SensorsData
	will pass the source code, which is for getting the sensors' data the user selected, back to the
	targetObject through the "setSourceCode" function. The targetObject MUST implement this function or
	else it won't be notified.
*/
SensorsDataPanel.prototype.show = function (x, y, targetObject) {
	this.targetObject = targetObject;
	
//	this.setLocation(this.parent.getWidth() / 2 + document.scrollLeft, this.parent.getHeight() / 2 + document.scrollTop);
	this.setLocation(x, y);
	this.setVisible(true);
	this.setZIndex({topmost:true});
}






SensorsDataPanel.prototype.initialize = function () {	

	this.loadSensorsDataSDML();
	this.combineSensorsDataSDML();

}









SensorsDataPanel.prototype.loadSensorsDataSDML = function () {
	// LOAD the SDML data for EACH of the sensors
	for (var i = 0; i < this.sensorDataURLs.length; i++) {
		var url = this.sensorDataURLs[i];
		
		var xmlhttp = new XMLHttpRequest();
		xmlhttp.open("GET", url , false);
		xmlhttp.send(null);
	
	
		if (xmlhttp.responseText != null && xmlhttp.responseText.indexOf("sdml") >= 0) {
			this.sdmlDOMs[i] = xmlhttp.responseXML;
		} else {
			throw "ERROR: Can not load sensor data from '" + this.sensorDataURLs[i] + "'.";
		}
	}
}




SensorsDataPanel.prototype.combineSensorsDataSDML = function () {



	// COMBINE ALL the SDML data into ONE DOM Tree.

	// Create a ROOT node for all the Sensor
	var rootElement = this.sensorsDataDOM.createElement("sensors");
	this.sensorsDataDOM.appendChild(rootElement);
	
	for (var i = 0; i < this.sdmlDOMs.length; i++) {
		var sdmlDOM = this.sdmlDOMs[i];
		var sdmlRoot = sdmlDOM.documentElement;

		
		// Create a Node for EACH sensor
		var sensorDOM = this.sensorsDataDOM.createElement("sensor");
		sensorDOM.setAttribute("name", sensorNames[i]);
		sensorDOM.setAttribute("description", "This will return the reference to the Sensor with variable name '" + sensorNames[i] + "'.");
		sensorDOM.setAttribute("code", sensorNames[i]);
		rootElement.appendChild(sensorDOM);
		
		
			// Create a node for CURRENT DATA
			var sensorData = this.sensorsDataDOM.createElement("data");
			sensorData.setAttribute("name", "current data");
			sensorData.setAttribute("description", "This will return " + sensorNames[i] + "'s data which is a type of SDMLDocument, a DOM tree.");
			sensorData.setAttribute("code", sensorNames[i] + ".getData()");
			sensorDOM.appendChild(sensorData);
			
			
				// Create a node for ALL the measurements
				var sensorMeasurements = this.sensorsDataDOM.createElement("measurements");
				sensorMeasurements.setAttribute("name", "measurements");
				sensorMeasurements.setAttribute("description", "This will return ALL of " + sensorNames[i] + "'s measurements. The returned value will be an ARRAY of Measurement objects.");
				sensorMeasurements.setAttribute("code", sensorNames[i] + ".getData().getSDML().getAllMeasurements()");
				sensorData.appendChild(sensorMeasurements);
				

					var measurements = sdmlRoot.getElementsByTagName("measurement");
					for (var j = 0; j < sdmlRoot.childNodes.length; j++) {
						var measurement = sdmlRoot.childNodes[j];
						if (measurement.nodeType == 1 && measurement.tagName == "measurement") {
							var sensorMeasurement = this.sensorsDataDOM.createElement("measurement");
							var measurementName = getMeasurementName(measurement);
							sensorMeasurement.setAttribute("name", measurementName);
							sensorMeasurement.setAttribute("description", "This will return " + sensorNames[i] + "'s " + measurementName + " measurement. The returned value will be a Measurement object.");
							sensorMeasurement.setAttribute("code", sensorNames[i] + ".getData().getSDML().getMeasurementsByName(\"" + measurementName + "\")[0]");
							sensorMeasurements.appendChild(sensorMeasurement);
							
							
								// Create a node for ALL the metadata
								var allMetadata = this.sensorsDataDOM.createElement("metadataList");
								allMetadata.setAttribute("name", "metadata");
								allMetadata.setAttribute("description", "This will return ALL of the metadata for the " + sensorNames[i] + "' " + measurementName + " measurement. The returned value will be an ARRAY of Metadata objects.");
								allMetadata.setAttribute("code", sensorNames[i] + ".getData().getSDML().getMeasurementsByName(\"" + measurementName + "\")" + "[0].getAllMetadata()");
								sensorMeasurement.appendChild(allMetadata);

								// Create Metadata nodes for EACH Metadata
								for (var k = 0; k < measurement.childNodes.length; k++) {
									var child = measurement.childNodes[k];
									if (child.nodeType == 1 && child.tagName == "metadata") {
										var metadata = createMetadataElement(this.sensorsDataDOM, child, "This will return " + sensorNames[i] + "' " + measurementName + " measurement", sensorNames[i] + ".getData().getSDML().getMeasurementsByName(\"" + measurementName + "\")[0]");
										allMetadata.appendChild(metadata);
									}
								}

							
							
								// Create a node for ALL the values
								var allValues = this.sensorsDataDOM.createElement("data");
								allValues.setAttribute("name", "data");
								allValues.setAttribute("description", "This will return ALL of the data for the " + sensorNames[i] + "' " + measurementName + " measurement. The returned value will be an ARRAY of MeasurementValue objects.");
								allValues.setAttribute("code", sensorNames[i] + ".getData().getSDML().getMeasurementsByName(\"" + measurementName + "\")[0]" + ".getValues()");
								sensorMeasurement.appendChild(allValues);

								// Create Value nodes for EACH value
								var count = 0;
								for (var k = 0; k < measurement.childNodes.length; k++) {
									var child = measurement.childNodes[k];
									if (child.nodeType == 1 && child.tagName == "value") {
										var value = createMeasurementValueElement(this.sensorsDataDOM, child, "data", "This will return " + sensorNames[i] + "' " + measurementName + " data", sensorNames[i] + ".getData().getSDML().getMeasurementsByName(\"" + measurementName + "\")[0]", count);
										allValues.appendChild(value);
										count++;
									}
								}
						}
					}
					
				
				
					
		// TODO Create a node for ARCHIVED DATA
		
	}
}











SensorsDataPanel.prototype.buildUI = function () {
	var mainDIV = document.getElementById(this.contentLayer.id);
	this.rootDIV = createForm(mainDIV, this.sensorsDataDOM.documentElement, 0, this);
}


SensorsDataPanel.prototype.returnSourceCodeToTargetObject = function (sourceCode, replace) {
	if (this.targetObject != null) {
		try {
			this.targetObject.setExpression(sourceCode, replace);
			this.setVisible(false);
			this.setLocation(-1000, -1000);
		} catch (e) {
		}
	}
}





function createForm (mainDIV, domElement, levelNumber, sensorsDataPanel) {
	var name = domElement.getAttribute("name");
	if (name == null) name = domElement.tagName;


	var newDIV = document.createElement("DIV");
	newDIV.style.paddingLeft = (levelNumber * 30) + "px";		
	newDIV.style.textAlign = "left";
	newDIV.style.cursor = "pointer";
	newDIV.style.backgroundColor = "transparent";
	mainDIV.appendChild(newDIV);


	newDIV._sensorsDataPanel = sensorsDataPanel;
	newDIV._childrenDIV = new Array();
	newDIV._collapsed = false;
	newDIV._description = domElement.getAttribute("description");
	newDIV._code = domElement.getAttribute("code");
	newDIV.title = newDIV._description || "";
	newDIV._mainDIV = mainDIV;
	

	newDIV._expand = function () {
		// hide all the children
		for (var i = 0; i < this._childrenDIV.length; i++) {
			this._childrenDIV[i]._show();
		}

		this._collapsed = false;
		
		if (this._childrenDIV.length > 0)
			this._collapseImage.src = getResourcesPath() + "collapse.gif";
	}
	
	
	// Collapse this DIV element
	newDIV._collapse = function () {
		// hide all the children
		for (var i = 0; i < this._childrenDIV.length; i++) {
			this._childrenDIV[i]._hide();
		}

		this._collapsed = true;

		if (this._childrenDIV.length > 0)
			this._collapseImage.src = getResourcesPath() + "expand.gif";			
	}


	// collapse EVERYTHING including CHILDREN
	newDIV._collapseAll = function () {
		this._collapse();
		for (var i = 0; i < this._childrenDIV.length; i++) {
			this._childrenDIV[i]._collapseAll();
		}
	}
	

	// expand EVERYTHING including CHILDREN
	newDIV._expandAll = function () {
		this._expand();
		for (var i = 0; i < this._childrenDIV.length; i++) {
			this._childrenDIV[i]._expandAll();
		}
	}


	
	newDIV._hide = function () {
		this.style.display = "none";
		
		// hide all the children
		for (var i = 0; i < this._childrenDIV.length; i++) {
			this._childrenDIV[i]._hide();
		}
	}
	
	
	newDIV._show = function () {
		this.style.display = "block";
		
		// if this DIV is NOT being collapsed then show all the children
		if (! this._collapsed) {
			for (var i = 0; i < this._childrenDIV.length; i++) {
				this._childrenDIV[i]._show();
			}
		}
	}
	



	newDIV.onmouseover = function () {
		this.style.backgroundColor = "#C0BFF2";
		
		if (this._infoButton)
			this._infoButton.style.display = "inline";
			
		if (this._returnAndAppendCodeButton)
			this._returnAndAppendCodeButton.style.display = "inline";

		if (this._returnAndReplaceCodeButton)
			this._returnAndReplaceCodeButton.style.display = "inline";
	}
	
	newDIV.onmouseout = function () {
		this.style.backgroundColor = "transparent";

		if (this._infoButton)
			this._infoButton.style.display = "none";

		if (this._returnAndAppendCodeButton)
			this._returnAndAppendCodeButton.style.display = "none";

		if (this._returnAndReplaceCodeButton)
			this._returnAndReplaceCodeButton.style.display = "none";
	}
	
	newDIV.onclick = function () {
		if (this._collapsed)
			this._expand();
		else
			this._collapse();
	}
	
	

	

	

	var childCount = 0;
	for (var i = 0; i < domElement.childNodes.length; i++) {
		if (domElement.childNodes[i].nodeType == 1) {
			var childDIV = createForm(mainDIV, domElement.childNodes[i], levelNumber + 1, sensorsDataPanel);
			if (childDIV != null) {
				newDIV._childrenDIV[childCount++] = childDIV;
			}
		}
	}



	var collapseImage = document.createElement("IMG");
	collapseImage.align = "absmiddle";
	collapseImage.style.border = "0px solid #ffffff";
	collapseImage.style.cursor = "pointer";
	collapseImage.style.width = "auto";
	collapseImage.style.height = "auto";
	newDIV._collapseImage = collapseImage;
	newDIV.appendChild(collapseImage);
/*	
	collapseImage.onclick = function () {
		if (this.parentNode._collapsed)
			this.parentNode._expand();
		else
			this.parentNode._collapse();
	}
*/	

	if (newDIV._childrenDIV.length > 0) {
		newDIV.style.fontWeight = "bold";
		collapseImage.src = getResourcesPath() + "collapse.gif";
	} else {
		collapseImage.src = getResourcesPath() + "blank.gif";
	}
	
	
	var displayText = document.createElement("SPAN");
	displayText.style.paddingRight = "50px";
	newDIV._displayText = displayText;
	newDIV.appendChild(displayText);
	displayText.appendChild(document.createTextNode(name));	
/*	
	displayText.onclick = function () {
		if (this.parentNode._collapsed)
			this.parentNode._expand();
		else
			this.parentNode._collapse();
	}
*/

	if (newDIV._description) {
		var infoButton = document.createElement("IMG");
		infoButton.src = getResourcesPath() + "info.gif";
		infoButton.align = "absmiddle";
		infoButton.style.border = "0px solid #ffffff";
		infoButton.style.cursor = "pointer";
		infoButton.style.width = "auto";
		infoButton.style.height = "auto";
		infoButton.style.display = "none";		
		infoButton.onclick = function (event) {
			if (!event) event = window.event;			
			event.cancelBubble=true;
			
			alert(this.parentNode._description);
		}
		newDIV._infoButton = infoButton;
		newDIV.appendChild(infoButton);
	}



	if (newDIV._code) {
		// create return and append code button
		var returnAndAppendCodeButton = document.createElement("IMG");
		returnAndAppendCodeButton.src = getResourcesPath() + "return_and_append.gif";
		returnAndAppendCodeButton.align = "absmiddle";
		returnAndAppendCodeButton.style.border = "0px solid #ffffff";
		returnAndAppendCodeButton.style.cursor = "pointer";
		returnAndAppendCodeButton.style.width = "auto";
		returnAndAppendCodeButton.style.height = "auto";
		returnAndAppendCodeButton.style.display = "none";
		returnAndAppendCodeButton.onclick = function (event) {
			if (!event) event = window.event;
			event.cancelBubble=true;

			this.parentNode._sensorsDataPanel.returnSourceCodeToTargetObject(this.parentNode._code, false);
		}
		newDIV._returnAndAppendCodeButton = returnAndAppendCodeButton;
		newDIV.appendChild(returnAndAppendCodeButton);


		// create return and replace code button
		var returnAndReplaceCodeButton = document.createElement("IMG");
		returnAndReplaceCodeButton.src = getResourcesPath() + "return_and_replace.gif";
		returnAndReplaceCodeButton.align = "absmiddle";
		returnAndReplaceCodeButton.style.border = "0px solid #ffffff";
		returnAndReplaceCodeButton.style.cursor = "pointer";
		returnAndReplaceCodeButton.style.width = "auto";
		returnAndReplaceCodeButton.style.height = "auto";
		returnAndReplaceCodeButton.style.display = "none";
		returnAndReplaceCodeButton.onclick = function (event) {
			if (!event) event = window.event;
			event.cancelBubble=true;

			this.parentNode._sensorsDataPanel.returnSourceCodeToTargetObject(this.parentNode._code, true);
		}
		newDIV._returnAndReplaceCodeButton = returnAndReplaceCodeButton;
		newDIV.appendChild(returnAndReplaceCodeButton);

	}








	
	newDIV._collapse();

	return newDIV;
}
	























	