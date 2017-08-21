package no.sysco.middleware.workshops.kafka;

import io.dropwizard.Application;
import io.dropwizard.setup.Environment;
import no.sysco.middleware.workshops.kafka.domain.model.IssueRepository;
import no.sysco.middleware.workshops.kafka.repositories.KafkaIssueRepository;
import no.sysco.middleware.workshops.kafka.resources.IssuesResource;

/**
 *
 */
public class IssueApplication extends Application<IssueConfiguration> {

  public static void main(String[] args) throws Exception {
    new IssueApplication().run(args);
  }

  @Override
  public String getName() {
    return "producer-app";
  }

  @Override
  public void run(IssueConfiguration configuration,
                  Environment environment)
      throws Exception {
    final IssueRepository issueRepository = new KafkaIssueRepository();

    final IssuesResource issuesResource = new IssuesResource(issueRepository);

    environment.jersey().register(issuesResource);
  }
}
