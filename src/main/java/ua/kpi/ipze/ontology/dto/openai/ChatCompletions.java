package ua.kpi.ipze.ontology.dto.openai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Representation of the response data from a chat completions request.
 * Completions support a wide variety of tasks and generate text that continues from or "completes"
 * provided prompt data.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public final class ChatCompletions {

  /**
   * A unique identifier associated with this chat completions response.
   */
  @JsonProperty(value = "id")
  private String id;

  /**
   * The collection of completions choices associated with this completions response.
   * Generally, `n` choices are generated per provided prompt with a default value of 1.
   * Token limits and other settings may limit the number of choices generated.
   */
  @JsonProperty(value = "choices")
  private List<ChatChoice> choices;

  /**
   * Usage information for tokens processed and generated as part of this completions operation.
   */
  @JsonProperty(value = "usage")
  private CompletionsUsage usage;

  /**
   * The first timestamp associated with generation activity for this completions response,
   * represented as seconds since the beginning of the Unix epoch of 00:00 on 1 Jan 1970.
   */
  @JsonProperty(value = "created")
  private long createdAt;

  /**
   * Can be used in conjunction with the `seed` request parameter to understand when backend changes have been made
   * that
   * might impact determinism.
   */
  @JsonProperty(value = "system_fingerprint")
  private String systemFingerprint;
}
