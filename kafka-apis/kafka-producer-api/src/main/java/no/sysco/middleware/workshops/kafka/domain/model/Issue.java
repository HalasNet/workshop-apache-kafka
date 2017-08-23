package no.sysco.middleware.workshops.kafka.domain.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 *
 */
public class Issue {
  private static final ObjectMapper MAPPER = new ObjectMapper();

  private final String id;
  private final IssueType type;
  private final String title;
  private final String description;

  public Issue(String id,
               String type,
               String title,
               String description) {
    this.id = id;
    this.type = IssueType.valueOf(type);
    this.title = title;
    this.description = description;
  }

  public String id(){
    return id;
  }

  public String printJson() {
    final ObjectNode issueObjectNode =
        MAPPER.createObjectNode()
            .put("id", id)
            .put("type", type.name())
            .put("title", title)
            .put("description", description);
    return issueObjectNode.toString();
  }

  public enum IssueType {
    BUG, IMPROVEMENT, TASK
  }
}
