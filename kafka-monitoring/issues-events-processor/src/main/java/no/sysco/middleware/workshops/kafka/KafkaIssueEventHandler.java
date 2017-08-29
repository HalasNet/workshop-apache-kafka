package no.sysco.middleware.workshops.kafka;

import io.opentracing.ActiveSpan;
import io.opentracing.References;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.contrib.kafka.TracingKafkaConsumer;
import io.opentracing.contrib.kafka.TracingKafkaUtils;
import no.sysco.middleware.workshops.kafka.repositories.ESIssueDocument;
import no.sysco.middleware.workshops.kafka.repositories.ElasticsearchIssueRepository;
import no.sysco.middleware.workshops.kafka.schema.issue.event.IssueEventKeyRecord;
import no.sysco.middleware.workshops.kafka.schema.issue.event.IssueEventRecord;
import no.sysco.middleware.workshops.kafka.schemas.AvroSpecificDeserializer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.Logger;

/**
 *
 */
public class KafkaIssueEventHandler {

  private static final Logger LOGGER = Logger.getLogger(KafkaIssueEventHandler.class.getName());

  private static final String ISSUES_EVENTS_TOPIC = "issues-events";

  public static void main(String[] args) {
    final AvroSpecificDeserializer<IssueEventKeyRecord> issueEventKeyRecordDeserializer =
        new AvroSpecificDeserializer<>(IssueEventKeyRecord.class);
    final AvroSpecificDeserializer<IssueEventRecord> issueEventRecordDeserializer =
        new AvroSpecificDeserializer<>(IssueEventRecord.class);

    final Tracer tracer =
        new com.uber.jaeger.Configuration(
            "issues-events-processor",
            new com.uber.jaeger.Configuration.SamplerConfiguration("const", 1),
            new com.uber.jaeger.Configuration.ReporterConfiguration(
                true,  // logSpans
                "localhost",
                6831,
                1000,   // flush interval in milliseconds
                10000)  /*max buffered Spans*/)
            .getTracer();

    final ElasticsearchIssueRepository issueRepository = new ElasticsearchIssueRepository(tracer);

    final Properties config = new Properties();
    config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
    config.put(ConsumerConfig.GROUP_ID_CONFIG, "issues-events-processor");
    config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());
    config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());
    //Read from earliest
    config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    //Disable auto-commit
    config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
    //Isolation level: read committed records
    config.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");

    try (KafkaConsumer<byte[], byte[]> consumer = new KafkaConsumer<>(config)) {
      final TracingKafkaConsumer<byte[], byte[]> tracingKafkaConsumer =
          new TracingKafkaConsumer<>(consumer, tracer);

      tracingKafkaConsumer.subscribe(Collections.singletonList(ISSUES_EVENTS_TOPIC));

      while (true) {
        ConsumerRecords<byte[], byte[]> records = tracingKafkaConsumer.poll(Long.MAX_VALUE);
        for (ConsumerRecord<byte[], byte[]> record : records) {
          Map<String, Object> data = new HashMap<>();
          data.put("topic", record.topic());
          data.put("partition", record.partition());
          data.put("offset", record.offset());
          data.put("key", record.key());
          data.put("value", record.value());
          LOGGER.info("Processing: " + data.toString());

          final SpanContext spanContext =
              TracingKafkaUtils.extractSpanContext(record.headers(), tracer);

          final IssueEventKeyRecord keyRecord =
              issueEventKeyRecordDeserializer.deserialize(record.key());

          switch (keyRecord.getEvent()) {
            case ADDED:
              try (ActiveSpan ignored =
                       tracer.buildSpan("processIssueAddedEvent")
                           .addReference(References.FOLLOWS_FROM, spanContext)
                           .startActive()) {

                IssueEventRecord issueEventRecord =
                    issueEventRecordDeserializer.deserialize(record.value());

                final ESIssueDocument issueDocument = new ESIssueDocument();
                issueDocument.setId(issueEventRecord.getId().toString());
                issueDocument.setTitle(issueEventRecord.getTitle().toString());
                issueDocument.setDescription(
                    Optional.ofNullable(issueEventRecord.getDescripcion())
                        .map(CharSequence::toString)
                        .orElse(null));
                issueDocument.setType(issueEventRecord.getType().toString());

                issueRepository.put(issueDocument);
              }
              break;
            default:
              LOGGER.warning("Command not supported");
          }

          consumer.commitSync();
        }
      }
    } catch (WakeupException e) {
      System.out.println("Shutting down");
    }
  }
}
