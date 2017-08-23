package no.sysco.middleware.workshops.kafka.repositories;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.connect.json.JsonDeserializer;
import org.apache.kafka.connect.json.JsonSerializer;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.errors.InvalidStateStoreException;
import org.apache.kafka.streams.kstream.KStreamBuilder;
import org.apache.kafka.streams.state.QueryableStoreType;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

import java.util.Optional;
import java.util.Properties;

/**
 *
 */
public class KafkaTweetsStreams {
  private static final Serializer<JsonNode> jsonNodeSerializer = new JsonSerializer();
  private static final Deserializer<JsonNode> jsonNodeDeserializer = new JsonDeserializer();
  private static final Serde<JsonNode> jsonNodeSerde = Serdes.serdeFrom(jsonNodeSerializer, jsonNodeDeserializer);
  //private static final ObjectMapper mapper = new ObjectMapper();

  //Queryable store names
  private static final String TWEETS_BY_USERNAME = "tweets-by-username-03";

  private final KafkaStreams tweetsPerUserStream;

  public KafkaTweetsStreams() {

    final Properties config = new Properties();
    config.put(StreamsConfig.APPLICATION_ID_CONFIG, "tweets-streams-application-03");
    config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
    //config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, jsonNodeSerde.getClass());
    //config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, jsonNodeSerde.getClass());

    tweetsPerUserStream = buildTweetsPerUser(config);

    tweetsPerUserStream.start();
  }

  private static KafkaStreams buildTweetsPerUser(Properties config) {
    KStreamBuilder builder = new KStreamBuilder();

    builder
        .stream(jsonNodeSerde, jsonNodeSerde, "tweets-01")
        .selectKey((key, value) -> {
          //return (JsonNode) mapper.createObjectNode().put("username", username);
          return value.get("payload").get("user").get("screen_name").textValue();
        })
        .mapValues(value ->
            value.get("payload").get("text").textValue())
        .groupByKey(new Serdes.StringSerde(), new Serdes.StringSerde())
        .reduce(
            (value1, value2) -> value1 + "\n\n" + value2,
            TWEETS_BY_USERNAME)
        .to(new Serdes.StringSerde(), new Serdes.StringSerde(), TWEETS_BY_USERNAME);

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
        Thread.sleep(100);
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
}
