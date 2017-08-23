package no.sysco.middleware.workshops.kafka;

import io.dropwizard.Application;
import io.dropwizard.setup.Environment;
import no.sysco.middleware.workshops.kafka.repositories.KafkaTweetsStreams;
import no.sysco.middleware.workshops.kafka.resources.TweetsResource;

/**
 *
 */
public class TweetsStreamsApplication extends Application<TweetsStreamsConfiguration> {

  public static void main(String[] args) throws Exception {
    new TweetsStreamsApplication().run(args);
  }

  @Override
  public String getName() {
    return "tweets-streams-app";
  }

  public void run(TweetsStreamsConfiguration tweetsStreamsConfiguration, Environment environment) throws Exception {
    KafkaTweetsStreams kafkaTweetsStreams = new KafkaTweetsStreams();

    final TweetsResource resource = new TweetsResource(kafkaTweetsStreams);

    environment.jersey().register(resource);

  }
}
