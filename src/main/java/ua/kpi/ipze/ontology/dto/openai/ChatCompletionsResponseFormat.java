package ua.kpi.ipze.ontology.dto.openai;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * An abstract representation of a response format configuration usable by Chat Completions. Can be used to enable JSON
 * mode.
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type",
        defaultImpl = ChatCompletionsResponseFormat.class)
@JsonTypeName("ChatCompletionsResponseFormat")
@JsonSubTypes({
        @JsonSubTypes.Type(name = "text", value = ChatCompletionsTextResponseFormat.class),
        @JsonSubTypes.Type(name = "json_object", value = ChatCompletionsJsonResponseFormat.class)})
public class ChatCompletionsResponseFormat {
}