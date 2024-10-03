package ua.kpi.ipze.ontology;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

@SpringBootApplication
@EnableFeignClients
@EnableCaching
@EnableWebSocket
@EnableAsync
public class OntologyHandlerApplication {

    public static void main(String[] args) {
        SpringApplication.run(OntologyHandlerApplication.class, args);
    }

}
