package ua.kpi.ipze.ontology.dto.openai;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * The standard Chat Completions response format that can freely generate text and is not guaranteed to produce
 * response
 * content that adheres to a specific schema.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonTypeName("text")
public final class ChatCompletionsTextResponseFormat extends ChatCompletionsResponseFormat {
}