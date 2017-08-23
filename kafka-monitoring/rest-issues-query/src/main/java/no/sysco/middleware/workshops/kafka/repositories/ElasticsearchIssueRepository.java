package no.sysco.middleware.workshops.kafka.repositories;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;

import java.io.InputStream;
import java.util.Collections;

/**
 *
 */
public class ElasticsearchIssueRepository {

  private static final ObjectMapper objectMapper = new ObjectMapper();

  private final RestClient elasticsearch;

  public ElasticsearchIssueRepository() {
    final RestClientBuilder restClientBuilder =
        RestClient.builder(
            new HttpHost("localhost", 9200));
    elasticsearch = restClientBuilder.build();
  }


  public String search(String filter) {
    try {
      final JsonNode queryObjectNode =
          objectMapper
              .createObjectNode()
              .put("version", true)
              .put("from", 0)
              .put("size", 1000)
              .set("query",
                  objectMapper.createObjectNode()
                      .set(
                          "simple_query_string",
                          objectMapper
                              .createObjectNode()
                              .put("query", filter)
                              .put("default_operator", "and")));

      final HttpEntity entity = new NStringEntity(queryObjectNode.toString(), ContentType.APPLICATION_JSON);
      final Response response =
          elasticsearch.performRequest("POST", "/issues/issue/_search", Collections.emptyMap(), entity);

      if (response.getStatusLine().getStatusCode() != 200) {
        throw new IllegalStateException("Error searching case");
      }

      final InputStream content = response.getEntity().getContent();
      final JsonNode jsonContent = objectMapper.readTree(content);
      final ObjectNode hits = (ObjectNode) jsonContent.get("hits");
      final ArrayNode hitsArray = (ArrayNode) hits.get("hits");
      return hitsArray.toString();
    } catch (Exception ex) {
      throw new IllegalStateException("Error", ex);
    }
  }
}
