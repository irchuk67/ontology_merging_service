package ua.kpi.ipze.ontology.dto.openai;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * A description of the intended purpose of a message within a chat completions interaction.
 */
@Getter
@RequiredArgsConstructor
public enum ChatRole {
  /**
   * The role that instructs or sets the behavior of the assistant.
   */
  SYSTEM("system"),

  /**
   * The role that provides responses to system-instructed, user-prompted input.
   */
  ASSISTANT("assistant"),

  /**
   * The role that provides input for chat completions.
   */
  USER("user");

  private final String value;

  /**
   * Returns the enum constant of this type with the specified name.
   * The string must match exactly an identifier used to declare an enum constant in this type.
   *
   * @param name the name of the enum constant to be returned.
   * @return the enum constant with the specified name
   */
  @JsonCreator
  public static ChatRole fromString(String name) {
    for (ChatRole role : ChatRole.values()) {
      if (role.name().equalsIgnoreCase(name) || role.value.equalsIgnoreCase(name)) {
        return role;
      }
    }
    throw new IllegalArgumentException("No constant with text " + name + " found");
  }
}