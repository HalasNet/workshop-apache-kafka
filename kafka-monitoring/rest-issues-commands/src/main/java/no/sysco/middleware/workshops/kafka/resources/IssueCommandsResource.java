package no.sysco.middleware.workshops.kafka.resources;

import io.opentracing.Span;
import io.opentracing.contrib.dropwizard.DropWizardTracer;
import no.sysco.middleware.workshops.kafka.repositories.KafkaIssueCommandRepository;
import no.sysco.middleware.workshops.kafka.representations.AddIssueCommandRepresentation;
import no.sysco.middleware.workshops.kafka.schema.issue.command.AddIssueCommandRecord;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import java.util.UUID;

/**
 *
 */
@Path("commands/issues")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class IssueCommandsResource {

  private static final String USERNAME = "anonymous";

  private final KafkaIssueCommandRepository issueCommandRepository;
  private final DropWizardTracer tracer;

  @Context
  private Request request = null;

  public IssueCommandsResource(KafkaIssueCommandRepository issueCommandRepository,
                               DropWizardTracer tracer) {
    this.issueCommandRepository = issueCommandRepository;
    this.tracer = tracer;
  }

  @POST
  public Response addIssue(@Context Request request, AddIssueCommandRepresentation representation) {
    Span span = tracer.getSpan(request);
    final AddIssueCommandRecord addIssueCommandRecord =
        AddIssueCommandRecord.newBuilder()
            .setTitle(representation.getTitle())
            .setDescripcion(representation.getDescription())
            .setType(representation.getType())
            .build();
    issueCommandRepository.sendAddIssueCommand(
        span.context(),
        UUID.randomUUID().toString(),
        USERNAME,
        addIssueCommandRecord);
    return Response.ok().build();
  }
}
