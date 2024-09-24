package ua.kpi.ipze.ontology.dto.openai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * A representation of a chat message as received in a response.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public final class ChatResponseMessage {

  /**
   * The chat role associated with the message.
   */
  @JsonProperty(value = "role")
  private ChatRole role;

  /**
   * The content of the message.
   */
  @JsonProperty(value = "content")
  private String content;
}