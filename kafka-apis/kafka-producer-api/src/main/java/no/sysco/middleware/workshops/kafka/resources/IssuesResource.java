package no.sysco.middleware.workshops.kafka.resources;

import no.sysco.middleware.workshops.kafka.domain.model.Issue;
import no.sysco.middleware.workshops.kafka.domain.model.IssueRepository;
import no.sysco.middleware.workshops.kafka.representations.IssueRepresentation;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 */

@Path("issues")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class IssuesResource {

  private final IssueRepository issueRepository;

  public IssuesResource(IssueRepository issueRepository) {
    this.issueRepository = issueRepository;
  }

  @POST
  public Response addIssue(IssueRepresentation representation) {
    final Issue issue =
        new Issue(
            representation.getId(),
            representation.getType(),
            representation.getTitle(),
            representation.getDescription());
    issueRepository.put(issue);
    return Response.ok().build();
  }
}
