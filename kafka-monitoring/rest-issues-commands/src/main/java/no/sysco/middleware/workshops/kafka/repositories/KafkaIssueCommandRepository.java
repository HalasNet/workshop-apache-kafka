package no.sysco.middleware.workshops.kafka.repositories;

import no.sysco.middleware.workshops.kafka.schema.issue.command.AddIssueCommandRecord;
import no.sysco.middleware.workshops.kafka.schema.issue.command.CommandEnum;
import no.sysco.middleware.workshops.kafka.schema.issue.command.IssueCommandKeyRecord;
import no.sysco.middleware.workshops.kafka.schemas.AvroSpecificSerializer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.ByteArraySerializer;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

/**
 *
 */
public class KafkaIssueCommandRepository {

  private static final Logger LOGGER = Logger.getLogger(KafkaIssueCommandRepository.class.getName());

  private static final String ISSUES_COMMANDS_TOPIC = "issues-commands";

  private final Producer<byte[], byte[]> producer;
  private final AvroSpecificSerializer<IssueCommandKeyRecord> issueCommandKeyRecordSerializer;
  private final AvroSpecificSerializer<AddIssueCommandRecord> addIssueCommandRecordSerializer;

  public KafkaIssueCommandRepository() {
    final Properties config = new Properties();
    config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
    config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());
    config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());

    producer = new KafkaProducer<>(config);
    addIssueCommandRecordSerializer = new AvroSpecificSerializer<>();
    issueCommandKeyRecordSerializer = new AvroSpecificSerializer<>();
  }

  public void sendAddIssueCommand(String correlationId,
                                  String executedBy,
                                  AddIssueCommandRecord record) {
    final IssueCommandKeyRecord keyRecord =
        IssueCommandKeyRecord.newBuilder()
            .setCommand(CommandEnum.ADD)
            .setCorrelationId(correlationId)
            .setExecutedBy(executedBy)
            .setExecutedAt(Instant.now().toEpochMilli())
            .build();
    final byte[] key = issueCommandKeyRecordSerializer.serialize(keyRecord);
    final byte[] value = addIssueCommandRecordSerializer.serialize(record);
    final ProducerRecord<byte[], byte[]> producerRecord =
        new ProducerRecord<>(
            ISSUES_COMMANDS_TOPIC,
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
