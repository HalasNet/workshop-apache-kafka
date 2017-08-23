package no.sysco.middleware.workshops.kafka.resources;

import no.sysco.middleware.workshops.kafka.repositories.ElasticsearchIssueRepository;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 */
@Path("queries/issues")
@Produces(MediaType.APPLICATION_JSON)
public class IssuesQueriesResource {

  private final ElasticsearchIssueRepository issueRepository;

  public IssuesQueriesResource(ElasticsearchIssueRepository issueRepository) {
    this.issueRepository = issueRepository;
  }

  @GET
  public Response getIssues(@QueryParam("q") String q) {
    String result = issueRepository.search(q);
    return Response.ok(result).build();
  }

}
