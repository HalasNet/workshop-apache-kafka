package no.sysco.middleware.workshops.kafka;

import io.dropwizard.Application;
import io.dropwizard.setup.Environment;
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
    final IssuesResource issuesResource = new IssuesResource();

    environment.jersey().register(issuesResource);
  }
}
