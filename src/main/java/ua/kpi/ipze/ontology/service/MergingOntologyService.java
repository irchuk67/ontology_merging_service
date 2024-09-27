package ua.kpi.ipze.ontology.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.jena.ontology.*;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import ua.kpi.ipze.ontology.dto.ClassRelation;
import ua.kpi.ipze.ontology.dto.openai.SemanticCompatibilityPair;
import ua.kpi.ipze.ontology.service.io.IOService;
import ua.kpi.ipze.ontology.util.StringUtility;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class MergingOntologyService {

    private static final Double SIMILARITY_THRESHOLD = 0.95;
    private static final Double COMPATIBILITY_THRESHOLD = 0.8;
    private static final List<String> DEFAULT_PROPERTY_CLASSES = List.of("Thing", "Resource");

    private final OpenAiService openAiService;
    private final IOService ioService;

    private final List<OntClass> ontology1Classes;
    private final Map<String, OntClass> ontology1ClassesMap;
    private final List<OntClass> ontology2Classes;
    private final Map<String, OntClass> ontology2ClassesMap;
    private List<SemanticCompatibilityPair> allSemanticCompatibilities = new ArrayList<>();

    public MergingOntologyService(OpenAiService openAiService, IOService ioService, List<OntClass> ontology1Classes, List<OntClass> ontology2Classes) {
        this.openAiService = openAiService;
        this.ioService = ioService;
        this.ontology1Classes = ontology1Classes;
        this.ontology1ClassesMap = ontology1Classes.stream()
                .collect(Collectors.toMap(Resource::getLocalName, ontClass -> ontClass));
        this.ontology2Classes = ontology2Classes;
        this.ontology2ClassesMap = ontology2Classes.stream()
                .collect(Collectors.toMap(Resource::getLocalName, ontClass -> ontClass));
    }

    public void mergeOntologies() {
//        for (OntClass ontology1Class : ontology1Classes) {
//            List<SemanticCompatibilityPair> semanticCompatibility = openAiService.getSemanticCompatibility(
//                    Collections.singletonList(ontology1Class.getLocalName()),
//                    ontology2ClassesMap.keySet()
//            ).getResult();
//
//
//            allSemanticCompatibilities.addAll(semanticCompatibility);
//        }
//        try {
//            String s = new ObjectMapper().writeValueAsString(allSemanticCompatibilities);
//            Files.writeString(Paths.get("src", "main", "resources", "semantic.json"), s, StandardOpenOption.WRITE);
//        } catch (JsonProcessingException e) {
//            throw new RuntimeException(e);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }

        try {
            String s = Files.readString(Paths.get("src", "main", "resources", "semantic.json"));
            allSemanticCompatibilities.addAll(new ObjectMapper().readValue(s, new TypeReference<List<SemanticCompatibilityPair>>(){}));
        }catch (IOException e){
            throw new RuntimeException(e);
        }
        for (int i = 0; i < allSemanticCompatibilities.size(); i++) {
            log.info(
                    "Class '{}' to '{}' is compatible: [{}]",
                    allSemanticCompatibilities.get(i).getWord1(),
                    allSemanticCompatibilities.get(i).getWord2(),
                    allSemanticCompatibilities.get(i).getValue()
            );
            compareClasses(
                    ontology1ClassesMap.get(allSemanticCompatibilities.get(i).getWord1()),
                    ontology2ClassesMap.get(allSemanticCompatibilities.get(i).getWord2()),
                    allSemanticCompatibilities.get(i));
        }
        System.out.println();
    }

    public void compareClasses(OntClass ontology1Class, OntClass ontology2Class, SemanticCompatibilityPair semanticCompatibility) {
        if (semanticCompatibility.getValue() >= SIMILARITY_THRESHOLD) {
            mergeEquivalentClasses(ontology1Class, ontology2Class);
            return;
        }
        if (semanticCompatibility.getValue() >= COMPATIBILITY_THRESHOLD) {
            ClassRelation classRelation = ioService.askForRelation(ontology1Class.getLocalName(), ontology2Class.getLocalName());
            switch (classRelation) {
                case EQUIVALENT -> mergeEquivalentClasses(ontology1Class, ontology2Class);
                case SUBCLASS -> mergeNewSubclass(ontology1Class, ontology2Class);
                case SUPERCLASS -> mergeNewSuperclass(ontology1Class, ontology2Class);
                case DISJOINT -> ontology1Class.addDisjointWith(ontology2Class);
            }
        }
    }

    public void mergeEquivalentClasses(OntClass ontClass1, OntClass ontClass2) {
        OntClass eqClass = ontClass1.getOntModel().createClass(ontClass2.getURI());
        if (!StringUtility.equalNormalized(ontClass1.getLocalName(), ontClass2.getLocalName()) && !ontology1ClassesMap.containsKey(ontClass2.getLocalName())) {
            ontClass1.addEquivalentClass(eqClass);
        }
        mergeDataProperties(ontClass1, ontClass2);
        mergeObjectProperties(ontClass1, ontClass2);
        /*
        todo:
         * 1. додаємо екв клас
         * 2. перевіряємо проперті на сем сумісність( щоб не дубоювати)
         * 3. додаємо проперті яких не було в онт 1 з онт 2
         * 4. додаємо всі класи які з'єднуються з екв obj property
         * 5. Додаємо проперті і з'єднані класи  для класів з п. 4
         */
    }

    public void mergeNewSubclass(OntClass ontClass1, OntClass ontClass2) {
        ontClass1.addSubClass(ontClass2);
        // todo: copy subclass properties to new ontology
    }

    public void mergeNewSuperclass(OntClass ontClass1, OntClass ontClass2) {
        OntClass parentClass = ontClass1.getOntModel().createClass(ontClass2.getURI());
        if (!StringUtility.equalNormalized(ontClass1.listSuperClasses().toList().get(0).getLocalName(), ontClass2.getLocalName())) {
            ontClass1.addSuperClass(parentClass);
        }
        mergeDataProperties(ontClass1, ontClass2);
        mergeObjectProperties(ontClass1, ontClass2);
    }


    private void mergeObjectProperties(OntClass ontClass1, OntClass ontClass2) {
        List<ObjectProperty> ontology1DomainObjProp = ontClass1.getOntModel().listObjectProperties().toList().stream()
                .filter(objectProperty -> objectProperty.hasDomain(ontClass1))
                .toList();
        List<ObjectProperty> ontology2DomainObjProp = ontClass2.getOntModel().listObjectProperties().toList().stream()
                .filter(objectProperty -> objectProperty.hasDomain(ontClass2))
                .toList();

        List<String> ontClass1DomainObjectPropertyNames = ontology1DomainObjProp.stream()
                .map(Property::getLocalName)
                .toList();
        List<String> ontClass2DomainObjectPropertyNames = ontology2DomainObjProp.stream()
                .map(Property::getLocalName)
                .toList();
        if(ontClass1DomainObjectPropertyNames.isEmpty() || ontClass2DomainObjectPropertyNames.isEmpty()) {
            return;
        }
        List<SemanticCompatibilityPair> semanticCompatibility = openAiService.getSemanticCompatibility(ontClass1DomainObjectPropertyNames, ontClass2DomainObjectPropertyNames).getResult();
        for (ObjectProperty ont2Property : ontology2DomainObjProp) {
            Optional<ObjectProperty> exitingPropertyOptional = findObjectSemanticCompatibleObjectProperty(ont2Property, ontology1DomainObjProp, semanticCompatibility);
            if(exitingPropertyOptional.isEmpty()){
                createNewObjectProperty(ontClass1, ont2Property);
            } else {
                handleObjectPropertyMerging(ont2Property, exitingPropertyOptional.get());
            }
        }

    }

    private Optional<ObjectProperty> findObjectSemanticCompatibleObjectProperty(ObjectProperty propertyToCreate, List<ObjectProperty> existingProperties, List<SemanticCompatibilityPair> semanticCompatibility) {
        return existingProperties.stream()
                .filter(existingProperty -> {
                    Optional<SemanticCompatibilityPair> semanticCompatibilityPairOptional = semanticCompatibility.stream()
                            .filter(pair -> StringUtility.equalNormalized(pair.getWord1(), existingProperty.getLocalName()) && StringUtility.equalNormalized(pair.getWord2(), propertyToCreate.getLocalName()))
                            .findFirst();
                    if (semanticCompatibilityPairOptional.isPresent()) {
                        return semanticCompatibilityPairOptional.get().getValue() >= COMPATIBILITY_THRESHOLD;
                    } else {
                        log.info("For object properties '{}' and '{}' there are no semantic compatibility!!!", existingProperty.getLocalName(), propertyToCreate.getLocalName());
                        return false;
                    }
                }).findFirst();
    }

    private void createNewObjectProperty(OntClass ontClass1, OntProperty ontProperty) {
        OntProperty newProperty = ontClass1.getOntModel().createObjectProperty(ontProperty.getURI());
        newProperty.addDomain(ontClass1);
        OntResource rangeClass = ontProperty.listRange().toList().stream()
                .filter(range -> !DEFAULT_PROPERTY_CLASSES.contains(range.getLocalName()))
                .findFirst().get();
        OntClass range = ontClass1.getOntModel().createClass(rangeClass.getURI());
        newProperty.addRange(range);
    }

    private void handleObjectPropertyMerging(OntProperty propertyToMerge, ObjectProperty existingProperty) {
        propertyToMerge.listRange().toList().forEach(ont2PropertyRange -> {
            existingProperty.listRange().toList().forEach(ont1PropertyRange -> {
                findSemanticCompatibilityForWords(ont1PropertyRange.getLocalName(), ont2PropertyRange.getLocalName())
                        .ifPresent(semanticCompatibilityPair -> {
                            //FIXME: it can be not only class
                            compareClasses((OntClass) ont1PropertyRange, (OntClass) ont2PropertyRange, semanticCompatibilityPair);
                        });
            });
        });
    }

    private Optional<SemanticCompatibilityPair> findSemanticCompatibilityForWords(String word1, String word2) {
        return allSemanticCompatibilities.stream()
                .filter(pair -> pair.getWord1().contentEquals(word1) && pair.getWord2().contentEquals(word2))
                .findFirst();
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

    private void addConnectedClasses(OntClass ontClass1, OntClass ontClass2) {

    }


}
