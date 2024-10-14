package ua.kpi.ipze.ontology.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import ua.kpi.ipze.ontology.entity.AutoGeneratedOntology;

@Repository
public interface AutoGeneratedOntologyRepository extends MongoRepository<AutoGeneratedOntology, String> {
}
