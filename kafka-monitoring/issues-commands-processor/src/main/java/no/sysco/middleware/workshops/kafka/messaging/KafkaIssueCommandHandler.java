package no.sysco.middleware.workshops.kafka.messaging;

import no.sysco.middleware.workshops.kafka.repositories.KafkaIssueEventRepository;
import no.sysco.middleware.workshops.kafka.schema.issue.command.AddIssueCommandRecord;
import no.sysco.middleware.workshops.kafka.schema.issue.command.IssueCommandKeyRecord;
import no.sysco.middleware.workshops.kafka.schema.issue.event.EventEnum;
import no.sysco.middleware.workshops.kafka.schema.issue.event.IssueEventKeyRecord;
import no.sysco.middleware.workshops.kafka.schema.issue.event.IssueEventRecord;
import no.sysco.middleware.workshops.kafka.schemas.AvroSpecificDeserializer;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Logger;

/**
 *
 */
public class KafkaIssueCommandHandler {

  private static final Logger LOGGER = Logger.getLogger(KafkaIssueCommandHandler.class.getName());

  private static final String ISSUES_COMMANDS_TOPIC = "issues-commands";

  public static void main(String[] args) {
    final AvroSpecificDeserializer<IssueCommandKeyRecord> issueCommandKeyRecordDeserializer =
        new AvroSpecificDeserializer<>(IssueCommandKeyRecord.class);
    final AvroSpecificDeserializer<AddIssueCommandRecord> addIssueCommandRecordDeserializer =
        new AvroSpecificDeserializer<>(AddIssueCommandRecord.class);

    final KafkaIssueEventRepository issueEventRepository = new KafkaIssueEventRepository();

    final Properties config = new Properties();
    config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
    config.put(ConsumerConfig.GROUP_ID_CONFIG, "issues-commands-processor");
    config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());
    config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());
    //Read from earliest
    config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    //Disable auto-commit
    config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
    //Isolation level: read committed records
    config.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");

    try (Consumer<byte[], byte[]> consumer = new KafkaConsumer<>(config)) {
      consumer.subscribe(Collections.singletonList(ISSUES_COMMANDS_TOPIC));

      while (true) {
        ConsumerRecords<byte[], byte[]> records = consumer.poll(Long.MAX_VALUE);
        for (ConsumerRecord<byte[], byte[]> record : records) {
          Map<String, Object> data = new HashMap<>();
          data.put("topic", record.topic());
          data.put("partition", record.partition());
          data.put("offset", record.offset());
          data.put("key", record.key());
          data.put("value", record.value());
          LOGGER.info("Processing: " + data.toString());

          final IssueCommandKeyRecord keyRecord =
              issueCommandKeyRecordDeserializer.deserialize(record.key());

          switch (keyRecord.getCommand()) {
            case ADD:
              AddIssueCommandRecord addIssueCommandRecord =
                  addIssueCommandRecordDeserializer.deserialize(record.value());

              String id = String.format("issue-%s", UUID.randomUUID().toString());

              issueEventRepository.send(
                  IssueEventKeyRecord.newBuilder()
                      .setCorrelationId(keyRecord.getCorrelationId())
                      .setEvent(EventEnum.ADDED)
                      .setExecutedAt(keyRecord.getExecutedAt())
                      .setExecutedBy(keyRecord.getExecutedBy())
                      .build(),
                  IssueEventRecord.newBuilder()
                      .setId(id)
                      .setDescripcion(addIssueCommandRecord.getDescripcion())
                      .setTitle(addIssueCommandRecord.getTitle())
                      .setType(addIssueCommandRecord.getType())
                      .build());
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
