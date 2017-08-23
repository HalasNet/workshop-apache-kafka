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

**Use case:**

Consume and print out issue records produced from Issue REST Service.

### Run 3 Consumer Group instances

A Consumer Thread is provided called `KafkaConsumerLoop`, and an
application class called `IssueConsumerApp`. Currently is spanning one
instance only:

By default, Kafka Consumer will be polling from the latest record since
it gets connected, and it also has auto-commit enabled.

Let's change the number of instances to test consume instances:

```java
  private static final int NUM_CONSUMER_INSTANCES = 3;
```

And run the class `IssueConsumerApp` again.

As topic only have 1 partition, only one thread is consuming records.

Let's create a new topic, and update its name on consumer side:

```bash
bin/kafka-topics.sh --zookeeper localhost:2181 \
                    --create \
                    --partitions 3 \
                    --replication-factor 3 \
                    --topic issue-events-03
```

And execute producer with Repository `KafkaIssueRepository03`

After executing the producer application, you will see that
records with different keys will be stored in different partitions.

And that each consumer instance will receive records from one
partition each.

### Get records from earliest

To get messages from scratch we should add the following configuration:

```java
    config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
```

### Enable At-Least-Once

As mentioned, auto-commit is enabled by default.

This means that if we fail to process one records, it would potentially
lost messages, achieving `at-most-once` semantics.

To move it to `al-least-once` we should first disable `auto-commit`
and then commit manually:

```java
    config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
```

```java
      while (true) {
        ConsumerRecords<String, String> records = consumer.poll(Long.MAX_VALUE);
        for (ConsumerRecord<String, String> record : records) {
          Map<String, Object> data = new HashMap<>();
          data.put("partition", record.partition());
          data.put("offset", record.offset());
          data.put("key", record.key());
          data.put("value", record.value());
          System.out.printf("id => %d -- metadata => %s%n", this.id, data);
          consumer.commitSync();
        }
      }
```

### Manage isolation level

With the addition of transactions on the producer side, we now have to
be aware of isolation levels on the consumer side.

There are 2 levels: `READ_COMMITED` and `READ_UNCOMMITED` (default).

First let's create a new Repository where transaction is not committed.

This means that messages will be received by broker.

On the consumer side let's test with the default value. Record will be received.

Then try with `read_commited`:

```java
      config.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
```

Records won't be received with this configuration.

## Lab 03: Kafka Connector API

**Use case:**

Bring tweets with specified #hashtags into Kafka `tweets` topic.

Then move those records into a `tweets.log` file.

### Configure and start Twitter Source Connector

Go here and create a new app: https://apps.twitter.com/

Put your keys on `kafka-connect-api/twitter-source-connector/twitter-source.properties`

```
twitter.consumerkey=nzU213sfdge...
twitter.consumersecret=dfgsdfg324235Tiv....
twitter.token=4523456234295304-...
twitter.secret=2342fEEZa93LImOv523534is....
```

And define your Kafka installation directory on `run.sh` script.

Execute it and you should be able to receive tweets on `tweets` topics.

```
bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 \
                              --topic tweets \
                              --from-beginning
```

### Configure and start File Sink Connector

Go to `kafka-connect-api/file-sink-connector` and execute `run.sh` script.

You should have a file on `/tmp/twitter-sink-file.txt` with tweets stored.

### Implement Transformation to modify header on Source Connector

//TODO

## Lab 04: Kafka Streams API

Kafka Streams allow `near` real-time processing on event streams.

**Use case:**

Tweets are flowing into Kafka using Connect API.

We need to implement an HTTP service that return the following:

* Tweets per user
* Hashtags count
* Progress of Hashtag per minute (Group counts per minute)

### Query tweets by Username

First, let's use the Kafka Streams DSL (High Level API) to process
tweets.

We need the following process:

1. Map username as `key`
2. Map tweet text to `value`
3. Group tweets with the same `key`
4. Join tweets together
5. Save result in a data store and a topic

```java
    final KStreamBuilder builder = new KStreamBuilder();
    builder
        .stream(jsonNodeSerde, jsonNodeSerde, TWEETS)
        //Defining a key = screen_name
        .selectKey((key, value) ->
            value.get("payload").get("user").get("screen_name").textValue())
        //Define new value = tweets
        .mapValues(value ->
            value.get("payload").get("text").textValue())
        //Join tweets by key (i.e. screen_name)
        .groupByKey(new Serdes.StringSerde(), new Serdes.StringSerde())
        //Reducing to concatenation of tweets, and keep it on TWEETS_BY_USERNAME store
        .reduce(
            (value1, value2) -> value1 + "\n\n" + value2,
            TWEETS_BY_USERNAME)
        //Save values on TWEETS_BY_USERNAME topic
        .to(Serdes.String(), Serdes.String(), TWEETS_BY_USERNAME);
```

Once we have this KafkaStream definition started, we can query the state
from `TWEETS_BY_USERNAME` store:

```java
      final ReadOnlyKeyValueStore<String, String> store = ...;
      final String value = store.get(username);
      return Optional.ofNullable(value).orElse("No tweets");
```

### Count hashtags

To count hashtags we can follow these steps:

1. Map payload to `hashtags` element
2. FlatMap `hashtags` to list elements
3. Map `hashtag` elements to text value

We can store this value on a topic `TWEETS_HASHTAGS` (we will use
this `topic` later)

After creating a new stream of hashtags, we can do the following to count hashtags:

1. Group hashtags together
2. Count how many repetitions are
3. Save results in store and topic `HASHTAGS_COUNT`

```java
    KStream<JsonNode, String> hashtagsStream = builder
        .stream(jsonNodeSerde, jsonNodeSerde, TWEETS)
        .mapValues(value -> value.get("payload").get("entities").withArray("hashtags"))
        .flatMapValues(ArrayNode.class::cast)
        .mapValues(value -> value.get("text").textValue());

    hashtagsStream.to(jsonNodeSerde, Serdes.String(), TWEETS_HASHTAGS);

    hashtagsStream
        .groupBy((key, value) -> value, Serdes.String(), Serdes.String())
        .count(HASHTAGS_COUNT)
        .to(Serdes.String(), Serdes.Long(), HASHTAGS_COUNT);
```

Once KafkaStream is started we can query its state:

```java
      final ReadOnlyKeyValueStore<String, String> store = ...;
      Map<String, Long> counts = new HashMap<>();
      final KeyValueIterator<String, Long> all = store.all();
      while (all.hasNext()) {
          KeyValue<String, Long> next = all.next();
          counts.put(next.key, next.value);
      }
      return counts.toString();
```

This will return a list of all hashtags with its occurrences.

### Find hashtag count by minute

If we want to know how a hashtag has progress to end up with its total count
we can use Window processing with Kafka Streams.

1. Read stream from `TWEETS_HASHTAGS`
2. Group hashtags
3. Count using a Time Window of 1 minute
4. Save result on `HASHTAG_PER_MINUTE` store

```java
    builder.stream(jsonNodeSerde, Serdes.String(), TWEETS_HASHTAGS)
        .groupBy((key, value) -> value, Serdes.String(), Serdes.String())
        .count(TimeWindows.of(TimeUnit.MINUTES.toMillis(1)), HASHTAG_PER_MINUTE);
```

Once KafkaStream is started we can query its state:

```java
      ReadOnlyWindowStore<String, Long> windowStore = ...;
      long timeFrom = 0; // beginning of time = oldest available
      long timeTo = System.currentTimeMillis(); // now (in processing-time)
      WindowStoreIterator<Long> iterator = windowStore.fetch(hashtag, timeFrom, timeTo);
      StringBuilder result = new StringBuilder();
      while (iterator.hasNext()) {
        KeyValue<Long, Long> next = iterator.next();
        long windowTimestamp = next.key;
        result.append(String.format("\nHashtag '%s' @ time %s is %d%n", hashtag, new Date(windowTimestamp).toString(), next.value));
      }
      return result.toString();
```

This will return a list of minutes recorded with the # of occurrences of a hashtag.