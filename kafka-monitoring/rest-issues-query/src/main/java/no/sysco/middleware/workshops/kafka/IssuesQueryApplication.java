package no.sysco.middleware.workshops.kafka;

import io.dropwizard.Application;
import io.dropwizard.setup.Environment;
import no.sysco.middleware.workshops.kafka.repositories.ElasticsearchIssueRepository;
import no.sysco.middleware.workshops.kafka.resources.IssuesQueriesResource;

/**
 *
 */
public class IssuesQueryApplication extends Application<IssuesQueryConfiguration> {

  public static void main(String[] args) throws Exception {
    new IssuesQueryApplication().run(args);
  }

  @Override
  public String getName() {
    return "rest-issues-commands-app";
  }


  @Override
  public void run(IssuesQueryConfiguration issuesQueryConfiguration,
                  Environment environment)
      throws Exception {

    final ElasticsearchIssueRepository issueRepository = new ElasticsearchIssueRepository();

    final IssuesQueriesResource queriesResource = new IssuesQueriesResource(issueRepository);

    environment.jersey().register(queriesResource);

  }
}
