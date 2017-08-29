package no.sysco.middleware.workshops.kafka.resources;

import no.sysco.middleware.workshops.kafka.domain.model.Issue;
import no.sysco.middleware.workshops.kafka.domain.model.IssueRepository;
import no.sysco.middleware.workshops.kafka.repositories.KafkaIssueBatchRepository;
import no.sysco.middleware.workshops.kafka.repositories.KafkaIssueRepository;
import no.sysco.middleware.workshops.kafka.repositories.KafkaIssueTxRepository;
import no.sysco.middleware.workshops.kafka.repositories.KafkaIssueTxWithoutCommitRepository;
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
  private final IssueRepository issueRepositoryTx;
  private final IssueRepository issueRepositoryTxWithoutCommit;
  private final IssueRepository issueRepositoryBatch;

  public IssuesResource() {
    this.issueRepository = new KafkaIssueRepository();
    this.issueRepositoryTx = new KafkaIssueTxRepository();
    this.issueRepositoryTxWithoutCommit = new KafkaIssueTxWithoutCommitRepository();
    this.issueRepositoryBatch = new KafkaIssueBatchRepository();
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

  @POST
  @Path("_tx")
  public Response addIssueTx(IssueRepresentation representation) {
    final Issue issue =
        new Issue(
            representation.getId(),
            representation.getType(),
            representation.getTitle(),
            representation.getDescription());
    issueRepositoryTx.put(issue);
    return Response.ok().build();
  }

  @POST
  @Path("_tx_uncommitted")
  public Response addIssueTxUncommitted(IssueRepresentation representation) {
    final Issue issue =
        new Issue(
            representation.getId(),
            representation.getType(),
            representation.getTitle(),
            representation.getDescription());
    issueRepositoryTxWithoutCommit.put(issue);
    return Response.ok().build();
  }

  @POST
  @Path("_batch")
  public Response addIssueBatch(IssueRepresentation representation) {
    final Issue issue =
        new Issue(
            representation.getId(),
            representation.getType(),
            representation.getTitle(),
            representation.getDescription());
    issueRepositoryBatch.put(issue);
    return Response.ok().build();
  }

}
