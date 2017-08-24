package no.sysco.middleware.workshops.kafka.repositories;

import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.contrib.kafka.TracingKafkaProducer;
import io.opentracing.contrib.kafka.TracingKafkaUtils;
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

  private final KafkaProducer<byte[], byte[]> producer;
  private final AvroSpecificSerializer<IssueEventKeyRecord> issueEventKeyRecordSerializer;
  private final AvroSpecificSerializer<IssueEventRecord> issueEventRecordSerializer;
  private final TracingKafkaProducer<byte[], byte[]> tracingKafkaProducer;
  private final Tracer tracer;

  public KafkaIssueEventRepository(Tracer tracer) {
    final Properties config = new Properties();
    config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
    config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());
    config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());

    producer = new KafkaProducer<>(config);
    tracingKafkaProducer = new TracingKafkaProducer<>(producer, tracer);
    this.tracer = tracer;
    issueEventKeyRecordSerializer = new AvroSpecificSerializer<>();
    issueEventRecordSerializer = new AvroSpecificSerializer<>();
  }

  public void send(SpanContext spanContext,
                   IssueEventKeyRecord keyRecord,
                   IssueEventRecord valueRecord) {
    final byte[] key = issueEventKeyRecordSerializer.serialize(keyRecord);
    final byte[] value = issueEventRecordSerializer.serialize(valueRecord);
    final ProducerRecord<byte[], byte[]> producerRecord =
        new ProducerRecord<>(
            ISSUES_EVENTS_TOPIC,
            key,
            value);
    TracingKafkaUtils.inject(spanContext, producerRecord.headers(), tracer);
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
