package ua.kpi.ipze.ontology.dto.openai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * The representation of a single prompt completion as part of an overall chat completions request.
 * Generally, `n` choices are generated per provided prompt with a default value of 1.
 * Token limits and other settings may limit the number of choices generated.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public final class ChatChoice {

  /**
   * The chat message for a given chat completions prompt.
   */
  @JsonProperty(value = "message")
  private ChatResponseMessage message;

  /**
   * The ordered index associated with this chat completions choice.
   */
  @JsonProperty(value = "index")
  private int index;

  /**
   * The reason that this chat completions choice completed its generated.
   */
  @JsonProperty(value = "finish_reason")
  private CompletionsFinishReason finishReason;

}