


function Location (lon, lat, alt, des) {
	this.longitude = lon;
	this.latitude = lat;
	this.altitude = alt;
	this.description = des;
}



function Value (value, timestamp) {
	this.value = value;
	this.timestamp = timestamp;
}




function Measurement (id, name, description, nrssURL, size, unit, url, startTime, stopTime, current, max, min) {
	this.id = id;
	this.name = name;
	this.description = description;
	this.nrssURL = nrssURL;
	this.size = size;
	this.unit = unit;
	this.url = url;
	this.startTime = startTime;
	this.stopTime = stopTime;
	this.current = current;
	this.max = max;
	this.min = min;
}



function Sensor (id, type, location, startTime, stopTime, age, measurements) {
	this.id = id;
	this.type = type;
	this.location = location;
	this.startTime = startTime;
	this.stopTime = stopTime;
	this.age = age;
	this.measurements = measurements;
	
	
	this.getMeasurement = function (name) {
	
		for (var i = 0; i < measurements.length; i++) {
			var m = measurements[i];
			if (m.name == name)
				return m;
		}
		
		return null;
	}
}




function dateToString (date) {
	var weekDay = new Array("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat");
	var month = new Array("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec");
	
//	return weekDay[date.getDay()] + " " + month[date.getMonth()] + " " + date.getDate() + " " + date.getHours() + ":" + date.getMinutes() + ":" + date.getSeconds() + " PDT " + date.getYear();
	return month[date.getMonth()] + " " + date.getDate() + " " + date.getHours() + ":" + date.getMinutes() + ":" + date.getSeconds() + " PDT " + date.getYear();
}






