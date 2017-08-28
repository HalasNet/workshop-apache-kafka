package no.sysco.middleware.workshops.kafka.repositories;

import io.opentracing.Tracer;
import io.opentracing.contrib.elasticsearch.TracingHttpClientConfigCallback;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;

import java.io.IOException;
import java.util.Collections;

/**
 *
 */
public class ElasticsearchIssueRepository {

  private final RestClient elasticsearch;

  public ElasticsearchIssueRepository(Tracer tracer) {
    final RestClientBuilder restClientBuilder =
        RestClient.builder(
            new HttpHost("localhost", 9200));
    elasticsearch =
        restClientBuilder
            .setHttpClientConfigCallback(new TracingHttpClientConfigCallback(tracer))
            .build();
  }


  public void put(ESIssueDocument issueDocument) {
    try {
      final String json = issueDocument.printJson();
      final HttpEntity entity = new NStringEntity(json, ContentType.APPLICATION_JSON);
      final String endpoint = String.format("issues/issue/%s", issueDocument.getId());

      final Response response =
          elasticsearch.performRequest("PUT", endpoint, Collections.emptyMap(), entity);
      if (response.getStatusLine().getStatusCode() != 200
          && response.getStatusLine().getStatusCode() != 201) {
        throw new IllegalStateException(response.getStatusLine().getReasonPhrase());
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
