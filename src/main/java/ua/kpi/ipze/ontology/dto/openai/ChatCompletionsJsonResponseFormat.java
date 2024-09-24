package ua.kpi.ipze.ontology.dto.openai;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * A response format for Chat Completions that restricts responses to emitting valid JSON objects.
 */
@JsonTypeName("json_object")

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
public final class ChatCompletionsJsonResponseFormat extends ChatCompletionsResponseFormat {
}