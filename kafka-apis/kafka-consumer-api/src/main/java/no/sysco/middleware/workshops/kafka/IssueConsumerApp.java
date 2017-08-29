package no.sysco.middleware.workshops.kafka;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class IssueConsumerApp {

  public static void main(String[] args) {
    createConsumerGroup();

    createTxConsumerGroup();
  }

  private static void createTxConsumerGroup() {
    final ExecutorService executor = Executors.newFixedThreadPool(1);
    final List<KafkaConsumerLoop> consumers = new ArrayList<>();

    //Start Consumer Threads
    for (int i = 0; i < 1; i++) {
      final KafkaConsumerLoop consumer =
          new KafkaConsumerLoop(
              i,
              "tx-group-01",
              Collections.singletonList("issue-events"),
              true);
      consumers.add(consumer);
      executor.submit(consumer);
    }

    //Close Consumers
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      for (KafkaConsumerLoop consumer : consumers) {
        consumer.shutdown();
      }
      executor.shutdown();
      try {
        executor.awaitTermination(5000, TimeUnit.MILLISECONDS);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }));
  }

  private static void createConsumerGroup() {
    final ExecutorService executor = Executors.newFixedThreadPool(3);
    final List<KafkaConsumerLoop> consumers = new ArrayList<>();

    //Start Consumer Threads
    for (int i = 0; i < 3; i++) {
      final KafkaConsumerLoop consumer =
          new KafkaConsumerLoop(
              i,
              "group-01",
              Collections.singletonList("issue-events"),
              false);
      consumers.add(consumer);
      executor.submit(consumer);
    }

    //Close Consumers
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      for (KafkaConsumerLoop consumer : consumers) {
        consumer.shutdown();
      }
      executor.shutdown();
      try {
        executor.awaitTermination(5000, TimeUnit.MILLISECONDS);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }));
  }
}
