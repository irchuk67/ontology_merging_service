package ua.kpi.ipze.ontology.service;

import org.apache.jena.ontology.*;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import ua.kpi.ipze.ontology.util.StringUtility;

import java.util.List;

public class MergingIndividualOntologyService {

    private final OntModel ontModel1;
    private final OntModel ontModel2;

    public MergingIndividualOntologyService(OntModel ontModel1, OntModel ontModel2) {
        this.ontModel1 = ontModel1;
        this.ontModel2 = ontModel2;
    }

    public void mergeOntologies() {
        List<Resource> ont1Individuals = ontModel1.listSubjects().toList();
        List<Individual> ont2Individuals = ontModel2.listIndividuals().toList();

        for (Individual individual2 : ont2Individuals) {
            List<Resource> ont1SameNamedIndividuals = ont1Individuals.stream()
                    .filter(resource -> !resource.getLocalName().isEmpty())
                    .filter(subject -> StringUtility.equalNormalized(subject.getLocalName(), individual2.getLocalName()))
                    .toList();
            if(!ont1SameNamedIndividuals.isEmpty()) {
                ont1SameNamedIndividuals.forEach(individual1 -> mergeProperties(individual1, individual2));
            } else {
                ontModel1.createIndividual(individual2.getURI(), individual2.getRDFType(true));
            }

        }
    }

    private void mergeProperties(Resource individual1, Individual individual2) {
        List<Statement> properties1 = individual1.listProperties().toList().stream().distinct().toList();
        List<Statement> properties2 = individual2.listProperties().toList().stream().distinct().toList();
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
                /*                           todo: check if property exists in ontology
                 *                             if not - make new property
                 *                               if yes - add property for that individual
                 * */
//                            List<Statement> property2InOntology1 = findPropertyInOntology(property2, ontModel1);
                List<ObjectProperty> objectPropertiesInOntology1 = ontModel1.listObjectProperties().toList().stream()
                        .filter(property -> property.getLocalName().contentEquals(property2.getPredicate().getLocalName()))
                        .distinct()
                        .toList();
                List<DatatypeProperty> dataPropertiesInOntology1 = ontModel1.listDatatypeProperties().toList().stream()
                        .filter(property -> property.getLocalName().contentEquals(property2.getPredicate().getLocalName()))
                        .distinct()
                        .toList();
                if (!dataPropertiesInOntology1.isEmpty()) { //if property is not present
                    dataPropertiesInOntology1.forEach(datatypeProperty -> {
                        datatypeProperty.addDomain(individual1);
                        List<? extends OntResource> property2Ranges = property2.getPredicate().as(DatatypeProperty.class).listRange().toList();
                        List<? extends OntResource> rangesToAdd = property2Ranges.stream().filter(range -> !datatypeProperty.hasRange(range)).toList();
                        rangesToAdd.forEach(datatypeProperty::addRange);
                    });
                } else if (!objectPropertiesInOntology1.isEmpty()) {
                    objectPropertiesInOntology1.forEach(objectProperty -> {
                        objectProperty.addDomain(individual1);
                        List<? extends OntResource> property2Ranges = property2.getPredicate().as(ObjectProperty.class).listRange().toList();
                        List<? extends OntResource> rangesToAdd = property2Ranges.stream().filter(range -> !objectProperty.hasRange(range)).toList();
                        rangesToAdd.forEach(objectProperty::addRange);
                    });
                } else {
                    addNewProperty(individual1, property2);
                }
            }

        }
    }

    private void addNewProperty(Resource individual1, Statement property2) {
        if (property2.getPredicate().canAs(ObjectProperty.class)) {
            ObjectProperty objectProperty = ontModel1.createObjectProperty(property2.getPredicate().getURI());
            objectProperty.addDomain(individual1);
            List<? extends OntResource> property2Ranges = property2.getPredicate().as(ObjectProperty.class).listRange().toList();
            property2Ranges.forEach(objectProperty::addRange);
        } else if(property2.getPredicate().canAs(DatatypeProperty.class)) {
            DatatypeProperty datatypeProperty = ontModel1.createDatatypeProperty(property2.getPredicate().getURI());
            datatypeProperty.addDomain(individual1);
            List<? extends OntResource> property2Ranges = property2.getPredicate().as(DatatypeProperty.class).listRange().toList();
            property2Ranges.forEach(datatypeProperty::addRange);
        }
    }


}