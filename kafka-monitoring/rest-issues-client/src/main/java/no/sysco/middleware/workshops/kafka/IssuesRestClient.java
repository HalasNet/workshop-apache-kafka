package no.sysco.middleware.workshops.kafka;

import io.opentracing.ActiveSpan;
import io.opentracing.Tracer;
import io.opentracing.contrib.apache.http.client.TracingHttpClientBuilder;
import io.opentracing.util.GlobalTracer;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 *
 */
public class IssuesRestClient {

  public static void main(String[] args) {
    try {
      Tracer tracer =
          new com.uber.jaeger.Configuration(
              "issues-http-client",
              new com.uber.jaeger.Configuration.SamplerConfiguration("const", 1),
              new com.uber.jaeger.Configuration.ReporterConfiguration(
                  true,  // logSpans
                  "localhost",
                  6831,
                  1000,   // flush interval in milliseconds
                  10000)  /*max buffered Spans*/)
              .getTracer();

      GlobalTracer.register(tracer);

      final HttpClient httpClient =
          new TracingHttpClientBuilder()
              .build();
      //Wihout tracing
      //HttpClientBuilder.create().build();

      try (ActiveSpan ignored = tracer.buildSpan("addIssue").startActive()) {
        HttpPost postRequest = new HttpPost("http://localhost:8901/commands/issues");

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
      }

      Thread.sleep(10000L);
    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
    }

  }
}

