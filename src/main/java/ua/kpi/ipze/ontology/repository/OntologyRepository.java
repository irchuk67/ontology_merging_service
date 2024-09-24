package ua.kpi.ipze.ontology.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import ua.kpi.ipze.ontology.entity.Ontology;

public interface OntologyRepository extends MongoRepository<Ontology, String> {
}
