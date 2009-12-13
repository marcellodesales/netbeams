# Copyright 2009 Marcello de Sales (marcello.sales@gmail.com).
# Converts the collected sensor data from the mongoDB to the OPEnDAP format

import pymongo
import sys

EXPORT_FILE_NAME="exported-by-python"

# Prints the header similar to the OPEnDAP format in the given file f, 
# with the first line identifying the dataset, and the second one the
# columns of the measurements.
def printHeader(f):
	f.write('Dataset: ntb_YSI_PUF.dsp\n')
	f.write('YSI_REALTIME_CSV.Month,YSI_REALTIME_CSV.Day,YSI_REALTIME_CSV.Year,YSI_REALTIME_CSV.Hour,YSI_REALTIME_CSV.Min,YSI_REALTIME_CSV.Sec,YSI_REALTIME_CSV.WaterTemperature,YSI_REALTIME_CSV.SpecificConductivity,YSI_REALTIME_CSV.Conductivity,YSI_REALTIME_CSV.Resistivity,YSI_REALTIME_CSV.Salinity,YSI_REALTIME_CSV.Pressure,YSI_REALTIME_CSV.Depth,YSI_REALTIME_CSV.pH,YSI_REALTIME_CSV.pHmV,YSI_REALTIME_CSV.Turbidity,YSI_REALTIME_CSV.ODOSaturation,YSI_REALTIME_CSV.ODO,YSI_REALTIME_CSV.Battery\n')

# Prints given ysiData returned from mongoDB, in a dict format, in to the file
# f, following the format of comma-separated values. 
def printRows(f, ysiData):
	f.write(ysiData["time"]["valid"].strftime("%m,%d,%Y,%H,%M,%S,"))
	f.write(str(ysiData["observation"]["WaterTemperature"]))
	f.write(",")
	f.write(str(ysiData["observation"]["SpecificConductivity"]))
	f.write(",")
	f.write(str(ysiData["observation"]["Conductivity"]))
	f.write(",")
	f.write(str(ysiData["observation"]["Resistivity"]))
	f.write(",")
	f.write(str(ysiData["observation"]["Salinity"]))
	f.write(",")
	f.write(str(ysiData["observation"]["Pressure"]))
	f.write(",")
	f.write(str(ysiData["observation"]["Depth"]))
	f.write(",")
	f.write(str(ysiData["observation"]["pH"]))
	f.write(",")
	f.write(str(ysiData["observation"]["pHmV"]))
	f.write(",")
	f.write(str(ysiData["observation"]["Turbidity"]))
	f.write(",")
	f.write(str(ysiData["observation"]["ODOSaturation"]))
	f.write(",")
	f.write(str(ysiData["observation"]["ODO"]))
	f.write(",")
	f.write(str(ysiData["observation"]["Battery"]))
	f.write("\n")

# processes the export request using the given ipAddress on the numerical
# portNumber, for the optional query supplied.
def processExport(ipAddress, portNumber, query):

	con = pymongo.Connection(ipAddress, portNumber)
	db = con.netbeams
	if (not query):
		collectedData = db.SondeDataContainer.find().limit(100)
	else:
		collectedData = db.SondeDataContainer.find(query).limit(100)
	if pymongo.cursor.Cursor.count(collectedData) > 0:
		f = open("/tmp/export-mongodb-to-sfbeams.csv", 'w')
		printHeader(f)
		for data in collectedData:
			printRows(f, data)
		f.close
	else:
		print "No records returned from NetBEAMS-mongoDB server"

# Main export method forwarded from the command-line execution with the
# given argument list. The first position must be the ip address of the
# NetBEAMS server, including the port number (ip:port). The second
# argument is an optional query in the JSON format for mongoDB to
# query the collection SondeDataContainer.
def exportOPEnDAP(argv):
	if len(argv) < 1:
		print "Please specify the server location (ip:port) and an optional query to export to OPEnDAP format"
	else:
		ipAddressPort = argv[0].split(':')
		query = None if len(argv) == 1 else argv[1]
		queryString = "" if not query else "Query supplied: ", query
		if len(ipAddressPort) == 2:
			print "Connecting to NetBEAMS database server at " , ipAddressPort[0] , " on port ", ipAddressPort[1] ,"... Query " , queryString
			processExport(ipAddressPort[0], int(ipAddressPort[1]), query)
		else:
			print "Please specify the server with the port number. E.g.: 10.1.25.13:20009, where 20009 is the port number"

if __name__ == "__main__":
    exportOPEnDAP(sys.argv[1:])
