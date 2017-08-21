package no.sysco.middleware.workshops.kafka.domain.model;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 */
public class IssueTest {
  @Test
  public void printJson() throws Exception {
    final Issue issue = new Issue("1", "BUG", "Bug 1", "desc");
    final String json = issue.printJson();
    System.out.println(json);
    assertEquals(
        "{\"id\":\"1\",\"type\":\"BUG\",\"title\":\"Bug 1\",\"description\":\"desc\"}",
        json);
  }

}