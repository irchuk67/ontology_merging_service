package ua.kpi.ipze.ontology.dto.openai;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;

/**
 * A request chat message representing user input to the assistant.
 */
@Getter
@JsonTypeName("user")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "role")
public final class ChatRequestUserMessage extends ChatRequestMessage {

  /**
   * The contents of the user message, with available input types varying by selected model.
   */
  @JsonProperty(value = "content")
  private String content;

  /**
   * An optional name for the participant.
   */
  @JsonProperty(value = "name")
  private String name;

  public ChatRequestUserMessage(String content) {
    this.content = content;
  }

}