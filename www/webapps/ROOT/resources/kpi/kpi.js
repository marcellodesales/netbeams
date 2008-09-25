


// Define some constant
KPI = function () {
	this.RESOURCE_PATH = "";
}
KPI = new KPI();




function getResourcesPath () {
	return KPI.RESOURCES_PATH;
}





String.prototype.padLeft = function (paddingString, count) {
	var s = this;
	
	var temp = "";
	if (paddingString != null) {
		for (var i = 0; i < count; i++) {
			temp = temp + paddingString;
		}
	}
	
	return temp + s;
}




/*
	Add a trim function to the String type. This function is for removing the starting and
	trailing spaces from the string
*/
String.prototype.trim = function () {
	var s = this;
	
	var re=/^(\s*)([\w\W]*[^\s])(\s*)$/;
	var rs=re.exec(String(s));
	return (rs != null)? rs[2] : "";
}








// this function return the HTML code for the Shape Background. Right now, we are using PNG images for the shape Background so we have to
// generate different code depend on different browser.
function getShapeBgImageHtmlCode (imageName, width, height) {

//	var randomNumber = Math.random() * 1000000;
	var randomNumber = "";

	if (dynapi.ua.ie == true && dynapi.ua.v >= 5) {
		return "<DIV STYLE=\"height:100%; width:100%; filter:progid:DXImageTransform.Microsoft.AlphaImageLoader(src='" + KPI.RESOURCES_PATH + "/" + imageName + ".jsp?width=" + width + "&height=" + height + "&" + randomNumber + "', sizingMethod='scale');\"></DIV>";
	} else {
		return "<img width='100%' height='100%' src='" + KPI.RESOURCES_PATH + imageName + ".jsp?width=" + width + "&height=" + height + "&" + randomNumber + "'>";
	}
}







/*
	Convert an Array to a string which each element is separated by a commas
*/
function arrayToString (array) {
	var result = "";
	
	if (array != null) {
		for (var i = 0; i < array.length; i++) {
			var item = array[i];
			if (item.toString)
				result = result + item.toString();
			else
				result = result + item;
				
			if (i < array.length - 1)
				result = result + ", ";
		}
	}
    
    return result;
}






