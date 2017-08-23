package no.sysco.middleware.workshops.kafka.resources;

import no.sysco.middleware.workshops.kafka.repositories.KafkaTweetsStreams;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 */
@Path("tweets")
@Produces(MediaType.TEXT_PLAIN + ";charset=utf-8")
public class TweetsResource {

  private final KafkaTweetsStreams kafkaTweetsStreams;

  public TweetsResource(KafkaTweetsStreams kafkaTweetsStreams) {
    this.kafkaTweetsStreams = kafkaTweetsStreams;
  }

  @GET
  @Path("{username}")
  public Response getTweetsByUsername(@PathParam("username") String username) {
    String tweets = kafkaTweetsStreams.getTweetsByUsername(username);
    return Response.ok(tweets).build();
  }

  @GET
  @Path("hashtags")
  public Response getHashtagsCount() {
    String hashtagsCount = kafkaTweetsStreams.getHashtags();
    return Response.ok(hashtagsCount).build();
  }

  @GET
  @Path("hashtags/{hashtag}")
  public Response getHashtagProgress(@PathParam("hashtag") String hashtag) {
    String hashtagProgress = kafkaTweetsStreams.getHashtagProgress(hashtag);
    return Response.ok(hashtagProgress).build();
  }
}
