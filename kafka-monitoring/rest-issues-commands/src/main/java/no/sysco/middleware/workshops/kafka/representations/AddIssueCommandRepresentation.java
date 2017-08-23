package no.sysco.middleware.workshops.kafka.representations;

/**
 *
 */
public class AddIssueCommandRepresentation {

  private String title;
  private String type;
  private String description;

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
