package no.sysco.middleware.workshops.kafka.repositories;

import no.sysco.middleware.workshops.kafka.domain.model.Issue;
import no.sysco.middleware.workshops.kafka.domain.model.IssueRepository;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 *
 */
public class KafkaIssueTxRepository04 implements IssueRepository {

  private static final String TOPIC = "issue-events-04";
  private static final String LOGS_TOPIC = "events-logs";

  private final Producer<String, String> producer;

  public KafkaIssueTxRepository04() {
    try {
      final Properties config = new Properties();
      config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
      config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
      config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
      //Acknowledge to all replicas
      config.put(ProducerConfig.ACKS_CONFIG, "all");
      //Transaction
      config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
      config.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, "kafka-issue-repository");

      producer = new KafkaProducer<>(config);
      producer.initTransactions();
    } catch (Exception e) {
      e.printStackTrace();
      throw new IllegalStateException("Error creating Kafka Producer", e);
    }
  }

  @Override
  public void put(Issue issue) {
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

      //Disable commit to test isolation level.
      //producer.commitTransaction();
    } catch (Exception e) {
      e.printStackTrace();
      //producer.abortTransaction();
    }
  }
}
