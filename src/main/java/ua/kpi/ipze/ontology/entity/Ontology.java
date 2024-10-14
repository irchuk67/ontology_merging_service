package ua.kpi.ipze.ontology.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "domain")
@Builder
public class Ontology {
    @Id
    private String id;
    private String owlContent;
    private LocalDateTime dateTime;
    private String name;
    private Integer version;
}
