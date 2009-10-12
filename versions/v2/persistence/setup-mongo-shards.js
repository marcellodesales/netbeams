admin = db.getSisterDB("admin")
config = db.getSisterDB("config")

admin.runCommand({addshard:"192.168.1.10:10000"})
admin.runCommand({addshard:"192.168.1.11:10001"})

admin.runCommand({listshards:1})

admin.runCommand({enablesharding:"netbeams"})
admin.runCommand({shardcollection:"netbeams.SondeDataContainer", key:{latitude:1}})

config.chunks.find()
