package ua.kpi.ipze.ontology.client;

import feign.Logger;
import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

public class OpenAiConfig {

    @Bean
    public RequestInterceptor requestInterceptor(@Value("${openai-api.key}") String openAiApiKey) {
        return requestTemplate -> requestTemplate.header("Authorization", openAiApiKey);
    }

    @Bean
    public Logger.Level loggerLevel() {
        return Logger.Level.FULL;
    }
}
