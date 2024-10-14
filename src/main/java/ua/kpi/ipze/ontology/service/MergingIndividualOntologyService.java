package ua.kpi.ipze.ontology.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.jena.ontology.*;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import ua.kpi.ipze.ontology.util.StringUtility;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class MergingIndividualOntologyService {

    private static final List<String> DEFAULT_DOMAINS = List.of("http://www.w3.org", "https://www.w3.org");

    private final OntModel ontModel1;
    private final OntModel ontModel2;
    private final MessageCollectorService messageCollectorService;

    public MergingIndividualOntologyService(OntModel ontModel1, OntModel ontModel2, MessageCollectorService messageCollectorService) {
        this.ontModel1 = ontModel1;
        this.ontModel2 = ontModel2;
        this.messageCollectorService = messageCollectorService;
    }

    public void mergeOntologies() {
        List<Individual> ont1Individuals = ontModel1.listIndividuals().toList();
        List<Individual> ont2Individuals = ontModel2.listIndividuals().toList();

        for (Individual individual2 : ont2Individuals) {
            List<Individual> ont1SameNamedIndividuals = ont1Individuals.stream()
                    .filter(resource -> !resource.getLocalName().isEmpty())
                    .filter(subject -> StringUtility.equalNormalized(subject.getLocalName(), individual2.getLocalName()))
                    .toList();
            if (!ont1SameNamedIndividuals.isEmpty()) {
                ont1SameNamedIndividuals.forEach(individual1 -> {
                    log.info("Merge individual {} with individual {}", individual1.getLocalName(), individual1.getLocalName());
                    mergeProperties(individual1, individual2);
                });
            } else {
                Individual individual = ontModel1.createIndividual(individual2.getURI(), individual2.getRDFType(true));
                messageCollectorService.addNewIndividual(individual.getLocalName());
                mergeProperties(individual, individual2);
            }
        }
    }

    private List<Statement> getIndividualStatement(OntModel ontModel, Resource individual) {
        List<Statement> statements = ontModel.listStatements().toList();
        List<Statement> result = new ArrayList<>();
        for (Statement statement : statements) {
            if (statement.getSubject().canAs(Resource.class)) {
                Resource resource = statement.getSubject().as(Resource.class);
                if (!resource.getLocalName().isEmpty() && StringUtility.equalNormalized(resource.getLocalName(), individual.getLocalName())) {
                    boolean hasDefaultNsDomain = DEFAULT_DOMAINS.stream()
                            .anyMatch(domain -> statement.getPredicate().getURI().startsWith(domain));
                    if (!hasDefaultNsDomain) {
                        result.add(statement);
                    }
                }
            }
        }
        return result;
    }

    private void mergeProperties(Individual individual1, Individual individual2) {
        List<Statement> properties1 = getIndividualStatement(ontModel1, individual1);
        List<Statement> properties2 = getIndividualStatement(ontModel2, individual2);
        for (Statement property2 : properties2) {
            boolean merged = false;
            for (Statement property1 : properties1) {
                if (StringUtility.equalNormalized(property1.getPredicate().getLocalName(), property2.getPredicate().getLocalName())) {
                    if (!StringUtility.equalNormalized(property1.getSubject().getLocalName(), property2.getSubject().getLocalName())) {
                        individual1.addProperty(ontModel1.getProperty(property1.getPredicate().getURI()), property2.getSubject());
                    }
                    merged = true;
                }
            }
            if (!merged) {
                List<ObjectProperty> objectProperties = ontModel1.listObjectProperties().toList().stream()
                        .filter(property -> property.getLocalName().contentEquals(property2.getPredicate().getLocalName()))
                        .distinct().toList();
                objectProperties
                        .forEach(objectProperty -> {
                            Statement statement = ontModel1.createStatement(individual1, objectProperty, property2.getObject());
                            ontModel1.add(statement);
                            messageCollectorService.addStatementToIndividual(individual1.getLocalName(), objectProperty.getLocalName(), property2.getObject().asNode().getLocalName());
                        });
                List<DatatypeProperty> dataProperties = ontModel1.listDatatypeProperties().toList().stream()
                        .filter(property -> property.getLocalName().contentEquals(property2.getPredicate().getLocalName()))
                        .distinct().toList();
                dataProperties
                        .forEach(dataProperty -> {
                            Statement statement = ontModel1.createStatement(individual1, dataProperty, property2.getObject());
                            messageCollectorService.addStatementToIndividual(individual1.getLocalName(), dataProperty.getLocalName(), property2.getObject().asLiteral().getLexicalForm());
                            ontModel1.add(statement);
                        });
            }

        }
    }

}