package ua.kpi.ipze.ontology;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class OntologyHandlerApplication {

    public static void main(String[] args) {
        SpringApplication.run(OntologyHandlerApplication.class, args);
    }

}
