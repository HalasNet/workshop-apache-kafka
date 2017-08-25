package no.sysco.middleware.workshops.kafka.repositories;

import io.opentracing.contrib.kafka.TracingKafkaProducer;
import io.opentracing.util.GlobalTracer;
import no.sysco.middleware.workshops.kafka.schema.issue.event.IssueEventKeyRecord;
import no.sysco.middleware.workshops.kafka.schema.issue.event.IssueEventRecord;
import no.sysco.middleware.workshops.kafka.schemas.AvroSpecificSerializer;
import org.apache.kafka.clients.producer.KafkaProducer;
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

  private final AvroSpecificSerializer<IssueEventKeyRecord> issueEventKeyRecordSerializer;
  private final AvroSpecificSerializer<IssueEventRecord> issueEventRecordSerializer;
  private final TracingKafkaProducer<byte[], byte[]> tracingKafkaProducer;

  public KafkaIssueEventRepository() {
    final Properties config = new Properties();
    config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
    config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());
    config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());

    KafkaProducer<byte[], byte[]> producer = new KafkaProducer<>(config);
    tracingKafkaProducer = new TracingKafkaProducer<>(producer, GlobalTracer.get());
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
    tracingKafkaProducer.send(
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
