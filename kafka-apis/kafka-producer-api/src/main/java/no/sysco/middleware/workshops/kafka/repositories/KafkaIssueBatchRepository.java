package no.sysco.middleware.workshops.kafka.repositories;

import no.sysco.middleware.workshops.kafka.domain.model.Issue;
import no.sysco.middleware.workshops.kafka.domain.model.IssueRepository;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 *
 */
public class KafkaIssueBatchRepository implements IssueRepository {

  private static final String TOPIC = "issue-events";

  private final Producer<Integer, String> producer;

  public KafkaIssueBatchRepository() {
    try {
      final Properties config = new Properties();
      config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
      config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class.getName());
      config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
      //Acknowledge to all replicas
      config.put(ProducerConfig.ACKS_CONFIG, "all");
      //Batch support
      config.put(ProducerConfig.BATCH_SIZE_CONFIG, 10000); //10KB
      config.put(ProducerConfig.LINGER_MS_CONFIG, 30000); //30secs

      producer = new KafkaProducer<>(config);
    } catch (Exception e) {
      e.printStackTrace();
      throw new IllegalStateException("Error creating Kafka Producer", e);
    }
  }

  @Override
  public void put(Issue issue) {

    final Integer key = issue.id();
    final String value = issue.printJson();
    final ProducerRecord<Integer, String> record = new ProducerRecord<>(TOPIC, key, value);
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
  }
}
