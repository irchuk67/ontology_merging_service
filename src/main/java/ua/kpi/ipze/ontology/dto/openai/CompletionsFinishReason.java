package ua.kpi.ipze.ontology.dto.openai;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Representation of the manner in which a completions response concluded.
 */
@Getter
@RequiredArgsConstructor
public enum CompletionsFinishReason {

  /**
   * Completions ended normally and reached its end of token generation.
   */
  STOPPED("stop"),

  /**
   * Completions exhausted available token limits before generation could complete.
   */
  TOKEN_LIMIT_REACHED("length"),

  /**
   * Completions generated a response that was identified as potentially sensitive per content
   * moderation policies.
   */
  CONTENT_FILTERED("content_filter"),

  /**
   * Completion ended normally, with the model requesting a function to be called.
   */
  FUNCTION_CALL("function_call"),

  /**
   * Completion ended with the model calling a provided tool for output.
   */
  TOOL_CALLS("tool_calls");

  private final String value;

  /**
   * Returns the enum constant of this type with the specified name.
   * The string must match exactly an identifier used to declare an enum constant in this type.
   *
   * @param name the name of the enum constant to be returned.
   * @return the enum constant with the specified name
   */
  @JsonCreator
  public static CompletionsFinishReason fromString(String name) {
    for (CompletionsFinishReason reason : CompletionsFinishReason.values()) {
      if (reason.name().equalsIgnoreCase(name) || reason.value.equalsIgnoreCase(name)) {
        return reason;
      }
    }
    throw new IllegalArgumentException("No constant with text " + name + " found");
  }
}
