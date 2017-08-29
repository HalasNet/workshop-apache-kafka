package no.sysco.middleware.workshops.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Created by jeqo on 13.02.17.
 */
public class KafkaConsumerLoop implements Runnable {
  private final KafkaConsumer<String, String> consumer;
  private final List<String> topics;
  private final String groupId;
  private final int id;

  KafkaConsumerLoop(int id,
                    String groupId,
                    List<String> topics,
                    boolean readCommitted) {
    Properties config = new Properties();
    config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
    config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
    config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, IntegerDeserializer.class.getName());
    config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    //Read from earliest
    config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    //Disable auto-commit
    config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
    if (readCommitted)
    //Isolation level: read committed records
    {
      config.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
    }

    this.id = id;
    this.groupId = groupId;
    this.topics = topics;
    this.consumer = new KafkaConsumer<>(config);
  }

  @Override
  public void run() {
    try {
      consumer.subscribe(topics);

      while (true) {
        ConsumerRecords<String, String> records = consumer.poll(Long.MAX_VALUE);
        for (ConsumerRecord<String, String> record : records) {
          Map<String, Object> data = new HashMap<>();
          data.put("topic", record.topic());
          data.put("partition", record.partition());
          data.put("offset", record.offset());
          data.put("key", record.key());
          data.put("value", record.value());
          System.out.printf("group => %s instance => %d -- metadata => %s%n", this.groupId, this.id, data);
          consumer.commitAsync();
        }
      }
    } catch (WakeupException e) {
      System.out.println("Shutting down");
    } finally {
      consumer.close();
    }
  }

  void shutdown() {
    consumer.wakeup();
  }
}