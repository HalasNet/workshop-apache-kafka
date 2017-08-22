# Lab: Kafka APIs

## Lab 01: Kafka Producer API

**Use Case:**

A `Issues` service requires to store information of `issues` created on
Kafka.

Kafka will work as a repository to store `issues`.

Current Service implementation has an HTTP Resource `/issues`
exposed on port `8801`.

Run it with the following command:

```bash
java -jar target/kafka-producer-api-1.0-SNAPSHOT.jar server config.yml
```

To test it, execute `curl` commands on `kafka-producer-api/add-issues-*.sh`:

```
TODO: send Issue {"id":"1","type":"BUG","title":"Bug 1","description":"..."} to Kafka
```

### Implement Kafka Producer Repository

To implement the Repository with a Kafka Producer API we should start
defining the Producer properties:

```java
    final Properties config = new Properties();
    config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
    config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
```

First, defining where is the `bootstrap.servers` to get the list of Kafka brokers.
Then, defining `Serializer` implementation class for `key` and `value`. In this case
we will use `StringSerializer` for both.

After defining properties, we can instantiate `KafkaProducer` to send
records to Kafka.

```java
  private final Producer<String, String> producer;


  public KafkaIssueRepository(){
    final Properties config = new Properties();
    ...
    producer = new KafkaProducer<>(config);
  }
```

Finally prepare a `ProducerRecord` to send it to Kafka:

```java
    final String key = issue.id();
    final String value = issue.printJson();
    ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC, key, value);
    producer.send(
        record,
        (metadata, exception) -> {
          if (exception == null) {
            Map<String, Object> data = new HashMap<>();
            data.put("topic", metadata.topic());
            data.put("partition", metadata.partition());
            data.put("offset", metadata.offset());
            data.put("timestamp", metadata.timestamp());
            System.out.println(data);
          } else {
            exception.printStackTrace();
          }
        });
```

Here we are defining first `key` and `value` variables. Then instantiating a
`ProducerRecord` and finally defining a `Callback` to act when we receive
`acknowledgement` from the broker.

### Add Acknowledgement to all replicas

By default, Kafka Producer uses `1` as acknowledge level.

Let's first create a topic with 3 `replicas` and then change acknowledge level:

```
bin/kafka-topics.sh --zookeeper localhost:2181 \
                    --create \
                    --partitions 1 \
                    --replication-factor 3 \
                    --config min.insync.replicas=3
                    --topic issue-events-02
```

This will force broker to validate that a record should be stored on 3
replicas.

And update topic name and add `acks` value:

```java
config.put(ProducerConfig.ACKS_CONFIG, "all");
```

### Add Batch support to increase throughput

To make `send` more efficient, we will add `batch` support to
join a set of records until we achieve a defined size to send them
together to Kafka cluster.

To do this there is a couple of `ProducerConfig` properties that
can be configured:

```java
      config.put(ProducerConfig.BATCH_SIZE_CONFIG, 10000); //10KB
      config.put(ProducerConfig.LINGER_MS_CONFIG, 30000); //30secs
```

Batch define a size of set of records that will be send out to Kafka,
but `linger` defines how much time we will wait for that size. If not,
we will send it after `linger` period is over.

On `broker` side there is a property: `max.message.bytes` that is around
`1MB`. If Batch Size is longer, should be updated on the broker side also.

### Add Transactions to send records to different topics

To finish with Producer API we can add `transactions` now.

Let's create an additional topic, `log-topic`, where we will send
the occurrence of an event.

```bash
bin/kafka-topics.sh --zookeeper localhost:2181 \
                    --create \
                    --partitions 1 \
                    --replication-factor 3 \
                    --topic events-logs
```

Then adding a `transaction.id` and `enable.idempotency` to enable transactions:

```java
      config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
      config.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, "kafka-issue-repository");
```

init transactions on the constructor:

```java
      producer = new KafkaProducer<>(config);
      producer.initTransactions();
```

invoke `#send` method as part of the same transaction:

```java
    try {
      producer.beginTransaction();

      final String key = issue.id();
      final String value = issue.printJson();
      final ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC, key, value);
      producer.send(
          record,
          (metadata, exception) -> {
            if (exception == null) {
              Map<String, Object> data = new HashMap<>();
              data.put("topic", metadata.topic());
              data.put("partition", metadata.partition());
              data.put("offset", metadata.offset());
              data.put("timestamp", metadata.timestamp());
              System.out.println(data);
            } else {
              exception.printStackTrace();
            }
          });

      final ProducerRecord<String, String> logRecord =
          new ProducerRecord<>(LOGS_TOPIC, String.format("issue %s added", issue.id()));
      producer.send(logRecord);

      producer.commitTransaction();
    } catch (Exception e) {
      e.printStackTrace();
      producer.abortTransaction();
    }
```

## Lab 02: Kafka Consumer API



### Implement Kafka Consumer using `KafkaConsumerLoop`

### Run 3 Consumer Group instances

## Lab 03: Kafka Connector API

### Configure and start Twitter Source Connector

### Configure and start File Sink Connector

### Implement Transformation to modify header on Source Connector

## Lab 04: Kafka Streams API

### Implement an application to count hashtags

### Query tweets by Username

### Find hashtag ranking by minute