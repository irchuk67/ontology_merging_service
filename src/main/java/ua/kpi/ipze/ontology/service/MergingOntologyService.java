package ua.kpi.ipze.ontology.service;

import org.apache.jena.ontology.DatatypeProperty;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MergingOntologyService {

    private static final List<String> DEFAULT_PROPERTY_CLASSES = List.of("Thing", "Resource");

    public void mergeEquivalentClasses(OntClass ontClass1, OntClass ontClass2) {
        OntClass eqClass = ontClass1.getOntModel().createClass(ontClass2.getURI());
        ontClass1.addEquivalentClass(eqClass);
        mergeDataProperties(ontClass1, ontClass2);
        mergeObjectProperties(ontClass1, ontClass2);
        /*
         * 1. додаємо екв клас
         * 2. перевіряємо проперті на сем сумісність( щоб не дубоювати)
         * 3. додаємо проперті яких не було в онт 1 з онт 2
         * 4. додаємо всі класи які з'єднуються з екв obj property
         * 5. Додаємо проперті і з'єднані класи  для класів з п. 4
         */


    }

    private void mergeObjectProperties(OntClass ontClass1, OntClass ontClass2) {
        List<ObjectProperty> ontology1DomainObjProp = ontClass1.getOntModel().listObjectProperties().toList().stream()
                .filter(objectProperty -> objectProperty.hasDomain(ontClass1))
                .toList();
        List<ObjectProperty> ontology2DomainObjProp = ontClass2.getOntModel().listObjectProperties().toList().stream()
                .filter(objectProperty -> objectProperty.hasDomain(ontClass2))
                .toList();

        List<String> ontClass1DomainPropertyNames = ontology1DomainObjProp.stream()
                .map(Property::getLocalName)
                .map(String::toLowerCase)
                .toList();
        ontology2DomainObjProp.stream()
                .filter(ontProperty -> !ontClass1DomainPropertyNames.contains(ontProperty.getLocalName().toLowerCase())) //TODO: replace with semantic compatibility
                .forEach(ontProperty -> {
                    OntProperty newProperty = ontClass1.getOntModel().createObjectProperty(ontProperty.getURI());
                    newProperty.addDomain(ontClass1);
                    newProperty.addRange(ontProperty.listRange().toList().stream().filter(range -> !DEFAULT_PROPERTY_CLASSES.contains(range.getLocalName())).findFirst().get());
                });
    }

    private void mergeDataProperties(OntClass ontClass1, OntClass ontClass2) {
        List<DatatypeProperty> ontClass2DataProperty = ontClass2.getOntModel().listDatatypeProperties().toList().stream()
                .filter(datatypeProperty -> datatypeProperty.getDomain().getLocalName().contentEquals(ontClass2.getLocalName()))
                .toList();
        List<DatatypeProperty> ontClass1DataProperty = ontClass1.getOntModel().listDatatypeProperties().toList().stream()
                .filter(datatypeProperty -> datatypeProperty.getDomain().getLocalName().contentEquals(ontClass1.getLocalName()))
                .toList();
        List<String> ontClass1PropertyNames = ontClass1DataProperty.stream()
                .map(Property::getLocalName)
                .map(String::toLowerCase)
                .toList();
        ontClass2DataProperty.stream()
                .filter(ontProperty -> !ontClass1PropertyNames.contains(ontProperty.getLocalName().toLowerCase())) //TODO: replace with semantic compatibility
                .forEach(ontProperty -> {
                    OntProperty newProperty = ontClass1.getOntModel().createDatatypeProperty(ontProperty.getURI());
                    newProperty.addDomain(ontClass1);
                    newProperty.addRange(ontProperty.getRange());
                });
    }

}
