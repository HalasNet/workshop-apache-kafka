package no.sysco.middleware.workshops.kafka.repositories;

import no.sysco.middleware.workshops.kafka.domain.model.Issue;
import no.sysco.middleware.workshops.kafka.domain.model.IssueRepository;

/**
 *
 */
public class KafkaIssueRepository implements IssueRepository {

  @Override
  public void put(Issue issue) {
    //TODO implement method using Kafka Producer
    System.out.println("TODO: send Issue " + issue.printJson() + " to Kafka");
  }
}
