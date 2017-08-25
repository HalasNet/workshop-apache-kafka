# Lab: Kafka Architecture and Deployment

## Lab 01: Creating a Kafka Cluster

Download Apache Kafka sources from [here](https://www.apache.org/dyn/closer.cgi?path=/kafka/0.11.0.0/kafka-0.11.0.0-src.tgz)
or check out its GitHub repository [here](https://github.com/apache/kafka/)
and binaries from [here](https://www.apache.org/dyn/closer.cgi?path=/kafka/0.11.0.0/kafka_2.11-0.11.0.0.tgz)

Create a directory to extract Kafka (e.g. `/opt/apache/kafka/`)

Start Zookeeper:

```bash
bin/zookeeper-server-start.sh config/zookeeper.properties
```

Zookeeper will be running on port `2181`

Once it is started, run a Kafka broker to prepare your cluster:

```bash
bin/kafka-server-start.sh config/server.properties
```

This will create a Kafka Server identified with a `broker.id` = `0` and
attached to port `9092`.

To add more brokers to the cluster we have to create new `server.properties` files.

Create a couple more files `server1.properties` and `server2.properties`
and update the following parameters:

* `broker.id`
* `listeners`
* `log.dirs`

## Lab 02: Navigating Zookeeper directories

Once you have a cluster with 3 Kafka brokers you can check `broker`
information like `zookeeper` `znodes`.

Connect to Zookeeper:

```bash
bin/zookeeper-shell.sh localhost:2181
```

### Check Kafka Brokers

First you can check which brokers are connected:

```bash
ls /brokers/ids
[0, 1, 2]
```

If you kill your third node, you can see that broker's list gets reduced:

```bash
ls /brokers/ids
[0, 1]
```

If you want to check connection properties for each broker, try:

```bash
get /brokers/ids/0
{"listener_security_protocol_map":{"PLAINTEXT":"PLAINTEXT"},"endpoints":["PLAINTEXT://jeqo-Oryx-Pro:9092"],"jmx_port":-1,"host":"jeqo-Oryx-Pro","timestamp":"1503304433384","port":9092,"version":4}
cZxid = 0x1c
...
```

Start your third broker again.

### Check ISR

Create a `topic` with 3 `partitions` and `replication-factor` of 3:

```bash
bin/kafka-topics.sh --zookeeper localhost:2181 \
                    --create \
                    --topic topic1 \
                    --partitions 3 \
                    --replication-factor 3 \
                    --config min.insync.replicas=3
```

Connect to `zookeeper` and list `topics`:

```bash
ls /brokers/topics
[topic1]
```

And check connection properties:

```shell
get /brokers/topics/topic1
{"version":1,"partitions":{"2":[2,0,1],"1":[1,2,0],"0":[0,1,2]}}
cZxid = 0x37
```

In partitions, you have a `partition` id, and in which broker each `partition` is.

Check it with a more readable format using `kafka-topics` command tool:

```bash
bin/kafka-topics.sh --zookeeper localhost:2181 \
                    --describe
```

```
Topic:topic1	PartitionCount:3	ReplicationFactor:3	Configs:
	Topic: topic1	Partition: 0	Leader: 0	Replicas: 0,1,2	Isr: 0,1,2
	Topic: topic1	Partition: 1	Leader: 1	Replicas: 1,2,0	Isr: 1,2,0
	Topic: topic1	Partition: 2	Leader: 2	Replicas: 2,0,1	Isr: 2,0,1
```

Kill again `broker.id` 2 and check again:

```
Topic:topic1	PartitionCount:3	ReplicationFactor:3	Configs:
	Topic: topic1	Partition: 0	Leader: 0	Replicas: 0,1,2	Isr: 0,1
	Topic: topic1	Partition: 1	Leader: 1	Replicas: 1,2,0	Isr: 1,0
	Topic: topic1	Partition: 2	Leader: 0	Replicas: 2,0,1	Isr: 0,1
```

`ISR` gets reduced to the number of replicas that are in sync and connected to
the cluster.

## Lab 03: Log Cleaner

To test Log Cleaner first we need to create `topics` that have a small
`segment` rotation.

### Retention

Create a topic with retention enabled:

```bash
bin/kafka-topics.sh --zookeeper localhost:2181 \
                    --create \
                    --partitions 1 \
                    --replication-factor 1 \
                    --topic retention \
                    --config cleanup.policy=delete \
                    --config segment.bytes=10000 \
                    --config retention.ms=30000
```

This topic has a segment size of `10 KB` and it will retain records from
the last 30 seconds.

Let's create 100 records and wait for Kafka broker to roll out a
new segment and delete old index:

Run `RetentionConsumer01App.java` to consume messages from scratch.

Then execute `RetentionProducerApp.java` to produce messages and
start creating segments.

Check broker logs:

```
[2017-08-21 14:19:55,253] INFO Rolled new log segment for 'retention-0' in 1 ms. (kafka.log.Log)
[2017-08-21 14:20:35,824] INFO Deleting segment 0 from log retention-0. (kafka.log.Log)
[2017-08-21 14:20:35,824] INFO Deleting segment 18 from log retention-0. (kafka.log.Log)
[2017-08-21 14:20:35,824] INFO Deleting segment 9 from log retention-0. (kafka.log.Log)
[2017-08-21 14:20:35,826] INFO Deleting index /tmp/kafka-cluster/logs/server2/retention-0/00000000000000000018.index.deleted (kafka.log.OffsetIndex)
[2017-08-21 14:20:35,826] INFO Deleting index /tmp/kafka-cluster/logs/server2/retention-0/00000000000000000009.index.deleted (kafka.log.OffsetIndex)
[2017-08-21 14:20:35,826] INFO Deleting index /tmp/kafka-cluster/logs/server2/retention-0/00000000000000000000.index.deleted (kafka.log.OffsetIndex)
[2017-08-21 14:20:35,829] INFO Deleting index /tmp/kafka-cluster/logs/server2/retention-0/00000000000000000000.timeindex.deleted (kafka.log.TimeIndex)
[2017-08-21 14:20:35,829] INFO Deleting index /tmp/kafka-cluster/logs/server2/retention-0/00000000000000000018.timeindex.deleted (kafka.log.TimeIndex)
[2017-08-21 14:20:35,829] INFO Deleting index /tmp/kafka-cluster/logs/server2/retention-0/00000000000000000009.timeindex.deleted (kafka.log.TimeIndex)
```

Now execute `RetentionConsumer01App.java` to validate how many messages stay at the topic.

Some initial messages has been removed as part of cleaning process.

### Compaction

Let's use the same approach to test compaction.

Let's create first a compacted topic:

```bash
bin/kafka-topics.sh --zookeeper localhost:2181 \
                    --create \
                    --partitions 1 \
                    --replication-factor 1 \
                    --topic compaction \
                    --config segment.bytes=10000 \
                    --config cleanup.policy=compact
```

Then execute `CompactionConsumer01App.java` to consume messages from scratch.

And produce messages with `CompactionProducerApp.java` to generate messages
that will have keys from 0 to 9, so can be compacted by the broker.

As you can see from Kafka broker, there is not deletion of segments:

```
[2017-08-21 14:41:19,812] INFO Rolled new log segment for 'compaction-0' in 1 ms. (kafka.log.Log)
[2017-08-21 14:41:19,828] INFO Rolled new log segment for 'compaction-0' in 1 ms. (kafka.log.Log)
[2017-08-21 14:41:19,847] INFO Rolled new log segment for 'compaction-0' in 1 ms. (kafka.log.Log)
[2017-08-21 14:41:19,866] INFO Rolled new log segment for 'compaction-0' in 1 ms. (kafka.log.Log)
[2017-08-21 14:41:19,883] INFO Rolled new log segment for 'compaction-0' in 1 ms. (kafka.log.Log)
[2017-08-21 14:41:19,905] INFO Rolled new log segment for 'compaction-0' in 1 ms. (kafka.log.Log)
[2017-08-21 14:41:19,922] INFO Rolled new log segment for 'compaction-0' in 1 ms. (kafka.log.Log)
[2017-08-21 14:41:19,945] INFO Rolled new log segment for 'compaction-0' in 1 ms. (kafka.log.Log)
[2017-08-21 14:41:19,962] INFO Rolled new log segment for 'compaction-0' in 1 ms. (kafka.log.Log)
[2017-08-21 14:41:19,993] INFO Rolled new log segment for 'compaction-0' in 1 ms. (kafka.log.Log)
[2017-08-21 14:41:20,014] INFO Rolled new log segment for 'compaction-0' in 1 ms. (kafka.log.Log)
```

If you execute `CompactionConsumer02App.java` you will see that few
records are not compacted:

```
{partition=0, offset=64, topic=compaction, key=5}
{partition=0, offset=79, topic=compaction, key=0}
{partition=0, offset=88, topic=compaction, key=6}
{partition=0, offset=89, topic=compaction, key=4}
{partition=0, offset=90, topic=compaction, key=8}
{partition=0, offset=91, topic=compaction, key=7}
{partition=0, offset=92, topic=compaction, key=3}
{partition=0, offset=95, topic=compaction, key=9}
{partition=0, offset=97, topic=compaction, key=1}
{partition=0, offset=98, topic=compaction, key=2}
{partition=0, offset=99, topic=compaction, key=8}
```

## Lab 04: Kafka Command Tools

### Replay Log Producer

First we will use `kafka-replay-log-producer` tool to recreate a new
topic copying values from another topic:

```bash
bin/kafka-replay-log-producer.sh --broker-list localhost:9092 \
                                 --inputtopic compaction \
                                 --outputtopic compaction-01
```

Ignore the exception, means that no message has been received since 10 seconds ago.

Check that a new topic is created:

```bash
bin/kafka-topics.sh --zookeeper localhost:2181 \
                    --list
```

A new topic is create and can be consumed with the same values from topic `compaction`.

### Console Consumer

```
bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 \
                              --topic compaction-01 \
                              --from-beginning \
                              --property print.key=true \
                              --consumer-property group.id=console-consumer-01 \
                              --key-deserializer org.apache.kafka.common.serialization.IntegerDeserializer
```


This consumer is using a consumer group to identify it.

If you execute the same command, you won't see values, as all are consumed so far.

### Check Consumer Groups

To validate how far your consumer groups has move along the topic partitions, execute:

To list `consumer-groups`:

```bash
bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 \
                             --list
```

And to check its consumption:

```bash
bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 \
                             --describe \
                             --group console-consumer-01
```

### Reset offsets

If you want to back in the log records and re-process some of them it is
possible using the same tool:

Let's go back 3 records:

```bash
 bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 \
                              --reset-offsets \
                              --group console-consumer-01 \
                              --topic compaction-01 \
                              --shift-by -3 \
                              --execute
```

If you execute the consumer it will re-consume this values.

### Delete records

If you want to remove records stored at the topic, use the following command:

First produce some messages with `kafka-console-producer` tool:

```bash
bin/kafka-console-producer.sh --broker-list localhost:9092 \
                              --topic simple-topic-01
> error 1
> error 2
> message 1
```



```bash
bin/kafka-delete-records.sh --bootstrap-server localhost:9092 \
                            --offset-json-file ~/dev/sysco/workshop-apache-kafka/kafka-architecture-deployment/delete-records.json
```

And execute a new console consumer to check:

```
bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 \
                              --topic simple-topic-01 \
                              --from-beginning
```
