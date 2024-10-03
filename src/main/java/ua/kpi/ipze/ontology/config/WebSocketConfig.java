package ua.kpi.ipze.ontology.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import ua.kpi.ipze.ontology.service.io.WebSocketHandler;

@Configuration
public class WebSocketConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new WebSocketHandler(), "/merge-ontologies")
                .setAllowedOrigins("*"); // You can restrict allowed origins as necessary.
    }
}

