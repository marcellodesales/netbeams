//Select the database NetBEAMS to be used
use netbeams

//Each collected data from the sersor devices are
//stored on separate collections. Show all of them 
print("Show all the collections of collected data (sensor devices' data)")
show collections

print("Show the statistics of the collection for the collected data for the YSI Sonde device")
db.SondeDataContainer.stats()

print("Verify some of the values using command abstraction shortcuts")
db.SondeDataContainer.count()
db.SondeDataContainer.dataSize()

print("An example of a random Show one random sample from the database using the findOne method
db.SondeDataContainer.findOne()

//db.SondeDataContainern.find({'$where' => 'this.time.valid.getTime() != this.time.transaction.getTime()'},{:limit => 50}).sort(:time.valid, Mongo::DESCENDING)

//the month variable of date class in javascript is defined as [0-11]. Therefore, 10 is November.

print("Execute Scenario R1) search all the documents in a given date range. Count and Limit retrieval result")
db.SondeDataContainer.find( {"time.valid" : { $gte:new Date(2009,11,8) , $lt:new Date(2009,11,15) }} ).count()
db.SondeDataContainer.find( {"time.valid" : { $gte:new Date(2009,11,8) , $lt:new Date(2009,11,15) }} ).limit(3)
print ("Explain the search over the shards for R1)")
db.SondeDataContainer.find( {"time.valid" : { $gte:new Date(2009,11,8) , $lt:new Date(2009,11,15) }} ).explain()

print("Execute Scenario R2) search all the documents for a given sensor device by IP address. Count and Limit retrieval result")
db.SondeDataContainer.find( {"sensor.ip_address":"192.168.0.102"} ).count()
db.SondeDataContainer.find( {"sensor.ip_address":"192.168.0.102"} ).limit(3)
print("Explain the search over the shards for R2")
db.SondeDataContainer.find( {"sensor.ip_address":"192.168.0.102"} ).explain()

print("Execute Scenario R3) search all the documents that has some observation values. Salinity = .01, Water Temperature = 46.47")
db.SondeDataContainer.find( {"observation.Salinity":0.01, "observation.WaterTemperature":46.47} ).count()
db.SondeDataContainer.find( {"observation.Salinity":0.01, "observation.WaterTemperature":46.47} ).limit(3)
print ("Explain the search over the shards for R3")
db.SondeDataContainer.find( {"observation.Salinity":0.01, "observation.WaterTemperature":46.47} ).explain()

print("Execute Scenario U1) update all the documents for a given date December 12, by annotating them using tags")
db.SondeDataContainer.ensureIndex( { "tag":1 } )
print ("Number of Observations from December 12")
db.SondeDataContainer.find( {"time.valid" : { "$gte":new Date(2009,11,12) , "$lt":new Date(2009,11,13) }}).count()
print ("How many with the tag 'oil spill'")
db.SondeDataContainer.find( { "tag": "oil spill" }  ).count()
db.SondeDataContainer.update( {"time.valid" : { "$gte":new Date(2009,11,12) , "$lt":new Date(2009,11,13) }} ,  {"$set": {"tag": "oil spill"}} , false, true)
print ("After annotating, how many documents have the tag?")
db.SondeDataContainer.find( { "tag": "oil spill" }  ).count()
print ("Show an annotated document with the tag 'oil spil'")
db.SondeDataContainer.find( { "tag": "oil spill" }  ).limit(1)

print("Execute Scenario D1) deleting all documents with for a given day (say Dec 13)")
db.SondeDataContainer.find( {"time.valid" : { $gte:new Date(2009,11,13) , $lt:new Date(2009,11,14) }} ).count()
db.SondeDataContainer.remove( {"time.valid" : { $gte:new Date(2009,11,13) , $lt:new Date(2009,11,14) }} )
print("How many documents are there after deleting?")
db.SondeDataContainer.find( {"time.valid" : { $gte:new Date(2009,11,13) , $lt:new Date(2009,11,14) }} ).count()
