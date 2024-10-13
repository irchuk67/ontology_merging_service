package ua.kpi.ipze.ontology.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import ua.kpi.ipze.ontology.entity.Ontology;

@Repository
public interface OntologyRepository extends MongoRepository<Ontology, String> {
    Ontology findFirstByOrderByDateTimeDesc ();
}
