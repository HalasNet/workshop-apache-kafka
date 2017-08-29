package no.sysco.middleware.workshops.kafka.repositories;

/**
 *
 */
public class ESIssueDocument {
  private String id;
  private String title;
  private String description;
  private String type;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  String printJson() {
    return "{\"id\": \"" + id + "\", \"title\": \"" + title + "\", \"description\": \"" + description + "\", \"type\": \"" + type + "\" }";
  }
}
