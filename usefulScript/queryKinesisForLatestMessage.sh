SHARD_ITERATOR=$(aws --endpoint-url=http://localhost:4566 kinesis get-shard-iterator --shard-id shardId-000000000000 --shard-iterator-type TRIM_HORIZON --stream-name produceOrder-out-0 | jq '.ShardIterator' | sed 's/\"//g')
echo "$SHARD_ITERATOR"
DATA=$(aws --endpoint-url=http://localhost:4566 kinesis get-records --shard-iterator $SHARD_ITERATOR | jq '.Records[0].Data' | sed 's/\"//g')
echo $(echo "$DATA" | base64 --decode)
#aws --endpoint-url=http://localhost:4566 kinesis list-streams
#aws --endpoint-url=http://localhost:4566 kinesis delete-stream --stream-name samplestream-out-0
#aws --endpoint-url=http://localhost:4566 kinesis list-stream-consumers --stream-arn arn:aws:kinesis:us-east-1:000000000000:stream/teamEvents-out-0
#aws --endpoint-url=http://localhost:4566 kinesis list-stream-producers --stream-arn arn:aws:kinesis:us-east-1:000000000000:stream/teamEvents-out-0
#aws --endpoint-url=http://localhost:4566 kinesis create-stream --stream-name teamEvents
#aws --endpoint-url=http://localhost:4566 kinesis delete-stream --stream-name teamEvents