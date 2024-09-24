package ua.kpi.ipze.ontology.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import ua.kpi.ipze.ontology.dto.openai.ChatCompletions;
import ua.kpi.ipze.ontology.dto.openai.ChatCompletionsOptions;

@FeignClient(
        value = "open-ai-client",
        url = "https://api.openai.com/v1/chat/completions",
        configuration = OpenAiConfig.class
)
public interface OpenAiClient {

    @ResponseStatus(HttpStatus.OK)
    @PostMapping
    ChatCompletions completions(@RequestBody ChatCompletionsOptions request);
}
