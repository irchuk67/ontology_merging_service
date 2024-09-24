package ua.kpi.ipze.ontology.dto.openai;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;

/**
 * A request chat message representing response or action from the assistant.
 */
@Getter
@JsonTypeName("assistant")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "role")
public final class ChatRequestAssistantMessage extends ChatRequestMessage {

  /**
   * The content of the message.
   */
  @JsonProperty(value = "content")
  private String content;

  /**
   * An optional name for the participant.
   */
  @JsonProperty(value = "name")
  private String name;
}
