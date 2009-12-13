#!/bin/bash
# Using "seq"

echo "############## Setting up shards for experiment..."
if [ -z "$1" ]; then 
    echo "Usage: $0 x, where x is the number of shards to be created"
    exit
fi

NUMBER_SHARDS=$1
SHARDS_DIR=shards
SHARD_DIR_PREFIX=shard

rm -rf $SHARDS_DIR
mkdir $SHARDS_DIR

mkdir data/$SHARDS_DIR
for shard_number in $(seq $NUMBER_SHARDS)
do
	mkdir data/$SHARDS_DIR/$SHARD_DIR_PREFIX-$shard_number
	mongod --dbpath data/$SHARDS_DIR/$SHARD_DIR_PREFIX-$shard_number/ --port 2000$shard_number | tee logs/shard-$shard_number.log &
done

mkdir data/$SHARDS_DIR/config
mongod --dbpath data/$SHARDS_DIR/config --port 10000 | tee logs/shard-config.log &

ps aux | grep mongod

mongos -vvv --configdb localhost:10000 | tee logs/mongos-cluster-head.log & 

sleep 1

mongo < setup-mongo-shards.js | tee logs/setup-mongo-shards-for-netbeams.log &

