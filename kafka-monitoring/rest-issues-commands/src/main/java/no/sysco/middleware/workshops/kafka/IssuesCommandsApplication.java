package no.sysco.middleware.workshops.kafka;

import io.dropwizard.Application;
import io.dropwizard.setup.Environment;
import io.opentracing.Tracer;
import io.opentracing.contrib.dropwizard.DropWizardTracer;
import io.opentracing.contrib.dropwizard.ServerTracingFeature;
import io.opentracing.util.GlobalTracer;
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


    Tracer tracer =
        new com.uber.jaeger.Configuration(
            "rest-issues-command-app",
            new com.uber.jaeger.Configuration.SamplerConfiguration("const", 1),
            new com.uber.jaeger.Configuration.ReporterConfiguration(
                true,  // logSpans
                "docker-vm",
                6831,
                1000,   // flush interval in milliseconds
                10000)  /*max buffered Spans*/)
            .getTracer();

    final DropWizardTracer dropWizardTracer = new DropWizardTracer(tracer);
    ServerTracingFeature serverTracingFeature =
        new ServerTracingFeature
            .Builder(dropWizardTracer)
            //.withTraceAnnotations()
            //.withOperationName(someOperationName)
            //.withTracedAttributes(someSetOfServerAttributes)
            //.withTracedProperties(someSetOfStringPropertyNames)
            .build();
    environment.jersey().register(serverTracingFeature);

    final KafkaIssueCommandRepository issueRepository = new KafkaIssueCommandRepository(tracer);

    final IssueCommandsResource commandsResource = new IssueCommandsResource(issueRepository, dropWizardTracer);

    environment.jersey().register(commandsResource);
  }
}
