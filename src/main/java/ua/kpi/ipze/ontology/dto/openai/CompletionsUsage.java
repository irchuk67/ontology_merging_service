package ua.kpi.ipze.ontology.dto.openai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Representation of the token counts processed for a completions request.
 * Counts consider all tokens across prompts, choices, choice alternates, best_of generations, and
 * other consumers.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public final class CompletionsUsage {
  /**
   * The number of tokens generated across all completions emissions.
   */
  @JsonProperty(value = "completion_tokens")
  private int completionTokens;

  /**
   * The number of tokens in the provided prompts for the completions request.
   */
  @JsonProperty(value = "prompt_tokens")
  private int promptTokens;

  /**
   * The total number of tokens processed for the completions request and response.
   */
  @JsonProperty(value = "total_tokens")
  private int totalTokens;
}
