package ua.kpi.ipze.ontology.dto.openai;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * An abstract representation of a chat message as provided in a request.
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "role",
        defaultImpl = ChatRequestMessage.class)
@JsonTypeName("ChatRequestMessage")
@JsonSubTypes({
        @JsonSubTypes.Type(name = "system", value = ChatRequestSystemMessage.class),
        @JsonSubTypes.Type(name = "user", value = ChatRequestUserMessage.class),
        @JsonSubTypes.Type(name = "assistant", value = ChatRequestAssistantMessage.class)
})
public class ChatRequestMessage {
}
