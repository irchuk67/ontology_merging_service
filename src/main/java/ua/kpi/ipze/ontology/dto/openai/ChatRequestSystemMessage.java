package ua.kpi.ipze.ontology.dto.openai;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;

/**
 * A request chat message containing system instructions that influence how the model will generate a chat completions
 * response.
 */
@Getter
@JsonTypeName("system")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "role")
public final class ChatRequestSystemMessage extends ChatRequestMessage {

  /**
   * The contents of the system message.
   */
  @JsonProperty(value = "content")
  private String content;

  /**
   * An optional name for the participant.
   */
  @JsonProperty(value = "name")
  private String name;

  public ChatRequestSystemMessage(String content) {
    this.content = content;
  }

}