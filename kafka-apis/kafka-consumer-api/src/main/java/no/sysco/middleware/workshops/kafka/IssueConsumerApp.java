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
  private static final String GROUP_ID ="consumer-group-04";
  private static final String TOPIC = "issue-events-04";
  private static final int NUM_CONSUMER_INSTANCES = 1;

  public static void main(String[] args) {
    final ExecutorService executor = Executors.newFixedThreadPool(NUM_CONSUMER_INSTANCES);
    final List<KafkaConsumerLoop> consumers = new ArrayList<>();

    //Start Consumer Threads
    for (int i = 0; i < NUM_CONSUMER_INSTANCES; i++) {
      final KafkaConsumerLoop consumer =
          new KafkaConsumerLoop(
              i,
              GROUP_ID,
              Collections.singletonList(TOPIC));
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
