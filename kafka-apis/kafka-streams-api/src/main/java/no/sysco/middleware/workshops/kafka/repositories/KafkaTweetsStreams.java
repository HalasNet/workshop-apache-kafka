package no.sysco.middleware.workshops.kafka.repositories;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.connect.json.JsonDeserializer;
import org.apache.kafka.connect.json.JsonSerializer;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.errors.InvalidStateStoreException;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KStreamBuilder;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.QueryableStoreType;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.apache.kafka.streams.state.ReadOnlyWindowStore;
import org.apache.kafka.streams.state.WindowStoreIterator;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class KafkaTweetsStreams {
  private static final Serializer<JsonNode> jsonNodeSerializer = new JsonSerializer();
  private static final Deserializer<JsonNode> jsonNodeDeserializer = new JsonDeserializer();
  private static final Serde<JsonNode> jsonNodeSerde = Serdes.serdeFrom(jsonNodeSerializer, jsonNodeDeserializer);
  //Topics
  private static final String TWEETS = "tweets-01";
  private static final String TWEETS_HASHTAGS = "tweets-hashtags";
  //Queryable store names
  private static final String TWEETS_BY_USERNAME = "tweets-by-username-05";
  private static final String HASHTAGS_COUNT = "tweets-hashtags-count-05";
  private static final String HASHTAG_PER_MINUTE = "tweets-hashtag-per-minute-06";
  private final KafkaStreams tweetsPerUserStream;
  private final KafkaStreams hashtagsCountStream;
  private final KafkaStreams hashtagPerMinuteStream;

  public KafkaTweetsStreams() {
    tweetsPerUserStream = buildTweetsPerUser();
    tweetsPerUserStream.start();

    hashtagsCountStream = buildCountHashtags();
    hashtagsCountStream.start();

    hashtagPerMinuteStream = buildHashtagProgress();
    hashtagPerMinuteStream.start();
  }

  private static KafkaStreams buildTweetsPerUser() {
    final Properties config = new Properties();
    config.put(StreamsConfig.APPLICATION_ID_CONFIG, "tweets-streams-application-tweets-per-user");
    config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");

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

    return new KafkaStreams(builder, config);
  }

  private static KafkaStreams buildCountHashtags() {
    final Properties config = new Properties();
    config.put(StreamsConfig.APPLICATION_ID_CONFIG, "tweets-streams-application-hashtags-count");
    config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");

    final KStreamBuilder builder = new KStreamBuilder();

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

    return new KafkaStreams(builder, config);
  }

  private static KafkaStreams buildHashtagProgress() {
    final Properties config = new Properties();
    config.put(StreamsConfig.APPLICATION_ID_CONFIG, "tweets-streams-application-hashtag-progress");
    config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");

    final KStreamBuilder builder = new KStreamBuilder();

    builder.stream(jsonNodeSerde, Serdes.String(), TWEETS_HASHTAGS)
        .groupBy((key, value) -> value, Serdes.String(), Serdes.String())
        .count(TimeWindows.of(TimeUnit.MINUTES.toMillis(1)), HASHTAG_PER_MINUTE);

    return new KafkaStreams(builder, config);
  }

  private static <T> T waitUntilStoreIsQueryable(
      final String storeName,
      final QueryableStoreType<T> queryableStoreType,
      final KafkaStreams streams)
      throws InterruptedException {
    while (true) {
      try {
        return streams.store(storeName, queryableStoreType);
      } catch (InvalidStateStoreException ignored) {
        ignored.printStackTrace();
        // store not yet ready for querying
        Thread.sleep(1000);
      }
    }
  }

  public String getTweetsByUsername(String username) {
    try {
      final ReadOnlyKeyValueStore<String, String> store =
          waitUntilStoreIsQueryable(
              TWEETS_BY_USERNAME,
              QueryableStoreTypes.<String, String>keyValueStore(),
              tweetsPerUserStream);
      final String value = store.get(username);
      return Optional.ofNullable(value).orElse("No tweets");
    } catch (Exception e) {
      throw new IllegalStateException("Kafka State Store is not loaded", e);
    }
  }


  public String getHashtags() {
    try {
      final ReadOnlyKeyValueStore<String, Long> store =
          waitUntilStoreIsQueryable(
              HASHTAGS_COUNT,
              QueryableStoreTypes.<String, Long>keyValueStore(),
              hashtagsCountStream);
      Map<String, Long> counts =
          new HashMap<>();
      final KeyValueIterator<String, Long> all = store.all();
      while (all.hasNext()) {
        KeyValue<String, Long> next = all.next();
        counts.put(next.key, next.value);
      }
      return counts.toString();
    } catch (Exception e) {
      throw new IllegalStateException("Kafka State Store is not loaded", e);
    }
  }

  public String getHashtagProgress(String hashtag) {
    try {
      ReadOnlyWindowStore<String, Long> windowStore =
          waitUntilStoreIsQueryable(
              HASHTAG_PER_MINUTE,
              QueryableStoreTypes.windowStore(),
              hashtagPerMinuteStream);
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
    } catch (Exception e) {
      throw new IllegalStateException("Kafka State Store is not loaded", e);
    }
  }
}
