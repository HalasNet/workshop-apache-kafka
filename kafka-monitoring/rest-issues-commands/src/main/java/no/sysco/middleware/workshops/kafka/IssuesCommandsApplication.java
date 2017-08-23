package no.sysco.middleware.workshops.kafka;

import io.dropwizard.Application;
import io.dropwizard.setup.Environment;
import no.sysco.middleware.workshops.kafka.repositories.KafkaIssueCommandRepository;
import no.sysco.middleware.workshops.kafka.resources.IssueCommandsResource;

/**
 *
 */
public class IssuesCommandsApplication extends Application<IssuesCommandsConfiguration> {

  public static void main(String[] args) throws Exception {
    new IssuesCommandsApplication().run(args);
  }

  @Override
  public String getName() {
    return "rest-issues-commands-app";
  }

  @Override
  public void run(IssuesCommandsConfiguration restIssuesCommandsConfiguration,
                  Environment environment)
      throws Exception {

    final KafkaIssueCommandRepository issueRepository = new KafkaIssueCommandRepository();

    final IssueCommandsResource commandsResource = new IssueCommandsResource(issueRepository);

    environment.jersey().register(commandsResource);
  }
}
