package ua.kpi.ipze.ontology.client;

import feign.Logger;
import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

public class HuggingFaceConfig {

    @Bean
    public RequestInterceptor requestInterceptor(@Value("${hugging-face-api.key}") String huggingFaceApiKey) {
        return requestTemplate -> {
            requestTemplate.header("Authorization", huggingFaceApiKey);
            requestTemplate.header("Content-Type", "application/json");
        };
    }

    @Bean
    public Logger.Level loggerLevel() {
        return Logger.Level.FULL;
    }

}
