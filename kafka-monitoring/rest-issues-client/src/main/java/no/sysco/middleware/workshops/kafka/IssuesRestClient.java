package no.sysco.middleware.workshops.kafka;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 *
 */
public class IssuesRestClient {

  public static void main(String[] args) {
    try {

      final HttpClient httpClient =
          HttpClientBuilder.create().build();
      HttpPost postRequest = new HttpPost(
          "http://localhost:8901/commands/issues");

      StringEntity input = new StringEntity("{\"type\": \"BUG\", \"title\": \"Bug 2\", \"description\": \"...\"}");
      input.setContentType("application/json");
      postRequest.setEntity(input);

      HttpResponse response = httpClient.execute(postRequest);

      if (response.getStatusLine().getStatusCode() != 200) {
        throw new RuntimeException("Failed : HTTP error code : "
            + response.getStatusLine().getStatusCode());
      }

      BufferedReader br = new BufferedReader(
          new InputStreamReader((response.getEntity().getContent())));

      String output;
      System.out.println("Sent. \n");
      while ((output = br.readLine()) != null) {
        System.out.println(output);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

  }
}

