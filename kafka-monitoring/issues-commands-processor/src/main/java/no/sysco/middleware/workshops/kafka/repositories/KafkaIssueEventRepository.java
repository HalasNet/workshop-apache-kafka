package no.sysco.middleware.workshops.kafka.repositories;

import no.sysco.middleware.workshops.kafka.schema.issue.event.IssueEventKeyRecord;
import no.sysco.middleware.workshops.kafka.schema.issue.event.IssueEventRecord;
import no.sysco.middleware.workshops.kafka.schemas.AvroSpecificSerializer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.ByteArraySerializer;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

/**
 *
 */
public class KafkaIssueEventRepository {
  private static final Logger LOGGER = Logger.getLogger(KafkaIssueEventRepository.class.getName());

  private static final String ISSUES_EVENTS_TOPIC = "issues-events";

  private final Producer<byte[], byte[]> producer;
  private final AvroSpecificSerializer<IssueEventKeyRecord> issueEventKeyRecordSerializer;
  private final AvroSpecificSerializer<IssueEventRecord> issueEventRecordSerializer;

  public KafkaIssueEventRepository() {
    final Properties config = new Properties();
    config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
    config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());
    config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());

    producer = new KafkaProducer<>(config);
    issueEventKeyRecordSerializer = new AvroSpecificSerializer<>();
    issueEventRecordSerializer = new AvroSpecificSerializer<>();
  }

  public void send(IssueEventKeyRecord keyRecord,
                   IssueEventRecord valueRecord) {
    final byte[] key = issueEventKeyRecordSerializer.serialize(keyRecord);
    final byte[] value = issueEventRecordSerializer.serialize(valueRecord);
    final ProducerRecord<byte[], byte[]> producerRecord =
        new ProducerRecord<>(
            ISSUES_EVENTS_TOPIC,
            key,
            value);
    producer.send(
        producerRecord,
        ((metadata, exception) -> {
          if (exception == null) {
            Map<String, Object> data =
                new HashMap<>();
            data.put("topic", metadata.topic());
            data.put("partition", metadata.partition());
            data.put("offset", metadata.offset());
            LOGGER.info(data.toString());
          }
        }));

  }
}
