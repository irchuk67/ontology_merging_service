package ua.kpi.ipze.ontology.service;

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
import java.nio.file.Paths;
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
    private List<OntClass> ontology1HandledClasses = new ArrayList<>();
    private List<String> ontology2HandledClasses = new ArrayList<>();

    //key - ontology1, value - ontology2
    private List<Map.Entry<OntClass, OntClass>> handledClassPairs = new ArrayList<>();

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

    private boolean checkComparisonPerformed(OntClass ontClass1, OntClass ontClass2) {
        return handledClassPairs.stream()
                .anyMatch(handled -> StringUtility.equalNormalized(handled.getKey().getLocalName(), ontClass1.getLocalName()) &&
                        StringUtility.equalNormalized(handled.getValue().getLocalName(), ontClass2.getLocalName()));
    }

    private Optional<OntClass> findClassInOntology1(OntClass ontClass) {
        return OntologyService.extractOntologyClasses(ontology1Classes.get(0).getOntModel()).toList().stream()
                .filter(ont1Class -> StringUtility.equalNormalized(ont1Class.getLocalName(), ontClass.getLocalName()))
                .findFirst();
    }

    private List<OntClass> getOntology1SameClasses(OntClass ontClass) {
        return OntologyService.extractOntologyClasses(ontology1Classes.get(0).getOntModel()).toList().stream()
                .filter(ont1Class -> StringUtility.equalNormalized(ont1Class.getLocalName(), ontClass.getLocalName()))
                .toList();
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
            allSemanticCompatibilities.addAll(new ObjectMapper().readValue(s, new TypeReference<List<SemanticCompatibilityPair>>() {
            }));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for (int i = 0; i < allSemanticCompatibilities.size(); i++) {
            log.info(
                    "Class '{}' to '{}' is compatible: [{}]",
                    allSemanticCompatibilities.get(i).getWord1(),
                    allSemanticCompatibilities.get(i).getWord2(),
                    allSemanticCompatibilities.get(i).getValue()
            );
            OntClass ontology1Class = ontology1ClassesMap.get(allSemanticCompatibilities.get(i).getWord1());
            OntClass ontology2Class = ontology2ClassesMap.get(allSemanticCompatibilities.get(i).getWord2());
            if (checkComparisonPerformed(ontology1Class, ontology2Class)) {
                continue;
            }
            compareClasses(
                    ontology1Class,
                    ontology2Class,
                    allSemanticCompatibilities.get(i));
            handledClassPairs.add(Map.entry(ontology1Class, ontology2Class));
        }
        System.out.println();
    }

    public boolean compareClasses(OntClass ontology1Class, OntClass ontology2Class, SemanticCompatibilityPair semanticCompatibility) {
        if (semanticCompatibility.getValue() >= SIMILARITY_THRESHOLD) {
            mergeEquivalentClasses(ontology1Class, ontology2Class);
            return true;
        }
        if (semanticCompatibility.getValue() >= COMPATIBILITY_THRESHOLD) {
            ClassRelation classRelation = ioService.askForRelation(ontology1Class.getLocalName(), ontology2Class.getLocalName());
            switch (classRelation) {
                case EQUIVALENT -> {
                    mergeEquivalentClasses(ontology1Class, ontology2Class);
                    return true;
                }
                case SUBCLASS -> {
                    mergeNewSubclass(ontology1Class, ontology2Class);
                    return true;
                }
                case SUPERCLASS -> {
                    mergeNewSuperclass(ontology1Class, ontology2Class);
                    return true;
                }
                case NO_RELATION -> {
                    return false;
                }
            }
        }
        return false;
    }

    public void mergeEquivalentClasses(OntClass ontClass1, OntClass ontClass2) {
        ontology2HandledClasses.add(ontClass2.getLocalName());
        if (!StringUtility.equalNormalized(ontClass1.getLocalName(), ontClass2.getLocalName()) && findClassInOntology1(ontClass2).isEmpty()) {
            OntClass eqClass = ontClass1.getOntModel().createClass(ontClass2.getURI());
            ontClass1.addEquivalentClass(eqClass);
            addRelatedClasses(ontClass2, eqClass, ontClass1.getOntModel());
        }
        if (findClassInOntology1(ontClass2).isPresent()) {
            List<OntClass> ont1RepetitiveClassesWithOntClass2 = getOntology1SameClasses(ontClass2);
            ont1RepetitiveClassesWithOntClass2.forEach(ontClass -> {
                ontClass1.addEquivalentClass(ontClass);
                addRelatedClasses(ontClass2, ontClass, ontClass1.getOntModel());

            });

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
        ontology2HandledClasses.add(ontClass2.getLocalName());

        Optional<OntClass> existingClassOptional = ontology1Classes.stream()
                .filter(ontClass -> StringUtility.equalNormalized(ontClass.getLocalName(), ontClass2.getLocalName()))
                .findFirst();
        if (existingClassOptional.isPresent()) {
            List<OntClass> ontology1SameClasses = getOntology1SameClasses(ontClass2);
            ontology1SameClasses.forEach(ontClass -> {
                ontClass.addSuperClass(ontClass1);
                addRelatedClasses(ontClass2, ontClass, ontClass1.getOntModel());
            });
            return;
        }
        OntClass subclass = ontClass1.getOntModel().createClass(ontClass2.getURI());
        subclass.addSuperClass(ontClass1);
        addRelatedClasses(ontClass2, subclass, ontClass1.getOntModel());
        // todo: copy subclass properties to new ontology
    }

    //todo: add non related classes from ontology 2
    public void mergeNewSuperclass(OntClass ontClass1, OntClass ontClass2) {
        ontology2HandledClasses.add(ontClass2.getLocalName());
        Optional<OntClass> existingClassOptional = ontology1Classes.stream()
                .filter(ontClass -> StringUtility.equalNormalized(ontClass.getLocalName(), ontClass2.getLocalName()))
                .findFirst();
        if (existingClassOptional.isPresent()) {
            List<OntClass> ontology1SameClasses = getOntology1SameClasses(ontClass2);
            ontology1SameClasses.forEach(ontClass -> {
                ontClass1.addSuperClass(ontClass);
                addRelatedClasses(ontClass2, ontClass, ontClass1.getOntModel());
            });
            return;
        }
        if (!StringUtility.equalNormalized(ontClass1.listSuperClasses().toList().get(0).getLocalName(), ontClass2.getLocalName())) {
            OntClass parentClass = ontClass1.getOntModel().createClass(ontClass2.getURI());
            ontClass1.addSuperClass(parentClass);
            addRelatedClasses(ontClass2, parentClass, ontClass1.getOntModel());
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
        if (ontClass1DomainObjectPropertyNames.isEmpty() || ontClass2DomainObjectPropertyNames.isEmpty()) {
            return;
        }
        List<SemanticCompatibilityPair> semanticCompatibility = openAiService.getSemanticCompatibility(ontClass1DomainObjectPropertyNames, ontClass2DomainObjectPropertyNames).getResult();
        for (ObjectProperty ont2Property : ontology2DomainObjProp) {
            Optional<ObjectProperty> exitingPropertyOptional = findObjectSemanticCompatibleObjectProperty(ont2Property, ontology1DomainObjProp, semanticCompatibility);
            if (exitingPropertyOptional.isEmpty()) {
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
        List<? extends OntResource> rangeClasses = ontProperty.listRange().toList().stream()
                .filter(range -> !DEFAULT_PROPERTY_CLASSES.contains(range.getLocalName()))
                .toList();

        rangeClasses.forEach(rangeClass -> {
            List<OntClass> ontology1SameClasses = getOntology1SameClasses((OntClass) rangeClass);
            if (ontology1SameClasses.isEmpty()) {
                OntClass range = ontClass1.getOntModel().createClass(rangeClass.getURI());
                newProperty.addRange(range);
                addRelatedClasses((OntClass) rangeClass, range, ontClass1.getOntModel());
            } else {
                ontology1SameClasses.forEach(ontClass -> {
                    newProperty.addRange(ontClass);
                    addRelatedClasses((OntClass) rangeClass, ontClass, ontClass1.getOntModel());
                });
            }
        });
    }

    private void handleObjectPropertyMerging(OntProperty propertyToMerge, ObjectProperty existingProperty) {
        List<? extends OntResource> list = propertyToMerge.listRange().toList().stream()
                .filter(range -> !DEFAULT_PROPERTY_CLASSES.contains(range.getLocalName()))
                .toList();
        List<? extends OntResource> list1 = existingProperty.listRange().toList().stream()
                .filter(range -> !DEFAULT_PROPERTY_CLASSES.contains(range.getLocalName()))
                .toList();

        for (OntResource ont2PropertyRange : list) {
            boolean merged = false;
            for (OntResource ont1PropertyRange : list1) {
                Optional<SemanticCompatibilityPair> semanticCompatibilityOptional = findSemanticCompatibilityForWords(ont1PropertyRange.getLocalName(), ont2PropertyRange.getLocalName());
                if (semanticCompatibilityOptional.isPresent()) {
                    //FIXME: it can be not only class
                    OntClass ont1PropertyRangeClass = (OntClass) ont1PropertyRange;
                    OntClass ont2PropertyRangeClass = (OntClass) ont2PropertyRange;
                    if (checkComparisonPerformed(ont1PropertyRangeClass, ont2PropertyRangeClass)) {
                        continue;
                    }
                    boolean classIsMerged = compareClasses(ont1PropertyRangeClass, ont2PropertyRangeClass, semanticCompatibilityOptional.get());
                    handledClassPairs.add(Map.entry(ont1PropertyRangeClass, ont2PropertyRangeClass));
                    if (classIsMerged) {
                        merged = true;
                        break;
                    }
                }
            }
            if (merged) {
                continue;
            }
            OntClass ont2PropertyRangeClass = (OntClass) ont2PropertyRange;
            Optional<OntClass> classInOntology1Optional = findClassInOntology1(ont2PropertyRangeClass);
            if (classInOntology1Optional.isPresent()) {
                ontology2HandledClasses.add(ont2PropertyRangeClass.getLocalName());
                existingProperty.addRange(classInOntology1Optional.get());
                addRelatedClasses(ont2PropertyRangeClass, classInOntology1Optional.get(), existingProperty.getOntModel());
            } else {
                ontology2HandledClasses.add(ont2PropertyRangeClass.getLocalName());
                OntClass newRangeClass = existingProperty.getOntModel().createClass(ont2PropertyRangeClass.getURI());
                existingProperty.addRange(newRangeClass);
                addRelatedClasses(ont2PropertyRangeClass, newRangeClass, existingProperty.getOntModel());
            }
        }
    }

    private void addRelatedClasses(OntClass ont2Class, OntClass newClass, OntModel ontModel1) {
        //merge superclasses
        if (ont2Class.getLocalName().contentEquals("Nothing")) {
            return;
        }
        List<OntClass> ont2ClassSuperclasses = ont2Class.listSuperClasses(true).toList().stream()
                .filter(ontClass -> !ontClass.getLocalName().contentEquals("Thing"))
                .filter(ontClass -> !ontology2HandledClasses.contains(ontClass.getLocalName()))
                .toList();
        for (OntClass ont2ClassSuperclass : ont2ClassSuperclasses) {
            boolean merged = false;
            for (OntClass ont1Class : OntologyService.extractOntologyClasses(ontModel1).toList()) {
                Optional<SemanticCompatibilityPair> semanticCompatibilityOptional = findSemanticCompatibilityForWords(ont1Class.getLocalName(), ont2ClassSuperclass.getLocalName());
                if (semanticCompatibilityOptional.isPresent()) {
                    if (checkComparisonPerformed(ont1Class, ont2ClassSuperclass)) {
                        continue;
                    }
                    boolean classIsMerged = compareClasses(ont1Class, ont2ClassSuperclass, semanticCompatibilityOptional.get());
                    handledClassPairs.add(Map.entry(ont1Class, ont2ClassSuperclass));
                    if (classIsMerged) {
                        merged = true;
                        Optional<OntClass> classInOntology1Optional = findClassInOntology1(ont2ClassSuperclass);
                        ontology2HandledClasses.add(ont2ClassSuperclass.getLocalName());
                        if (classInOntology1Optional.isEmpty()) {
                            newClass.addSuperClass(ontModel1.createClass(ont2ClassSuperclass.getURI()));
                        } else {
                            getOntology1SameClasses(ont2ClassSuperclass).forEach(ontClass -> newClass.addSuperClass(ontClass));
                        }
                        break;
                    }
                }
            }
            if (merged) {
                continue;
            }
            Optional<OntClass> classInOntology1Optional = findClassInOntology1(ont2ClassSuperclass);
            ontology2HandledClasses.add(ont2ClassSuperclass.getLocalName());
            if (classInOntology1Optional.isEmpty()) {
                newClass.addSuperClass(ontModel1.createClass(ont2ClassSuperclass.getURI()));
            } else {
                getOntology1SameClasses(ont2ClassSuperclass).forEach(ontClass -> newClass.addSuperClass(ontClass));
            }
        }

        //merge subclasses
        List<OntClass> ont2ClassSubclasses = ont2Class.listSubClasses(true).toList().stream()
                .filter(ontClass -> !ontClass.getLocalName().contentEquals("Nothing"))
                .filter(ontClass -> !ontology2HandledClasses.contains(ontClass.getLocalName()))
                .toList();
        for (OntClass ont2Subclass : ont2ClassSubclasses) {
            boolean merged = false;
            for (OntClass ont1Class : OntologyService.extractOntologyClasses(ontModel1).toList()) {
                Optional<SemanticCompatibilityPair> semanticCompatibilityOptional = findSemanticCompatibilityForWords(ont1Class.getLocalName(), ont2Subclass.getLocalName());
                if (semanticCompatibilityOptional.isPresent()) {
                    if (checkComparisonPerformed(ont1Class, ont2Subclass)) {
                        continue;
                    }
                    boolean classIsMerged = compareClasses(ont1Class, ont2Subclass, semanticCompatibilityOptional.get());
                    handledClassPairs.add(Map.entry(ont1Class, ont2Subclass));
                    if (classIsMerged) {
                        merged = true;
                        Optional<OntClass> classInOntology1Optional = findClassInOntology1(ont2Subclass);
                        if (classInOntology1Optional.isEmpty()) {
                            ontology2HandledClasses.add(ont2Subclass.getLocalName());
                            ontModel1.createClass(ont2Subclass.getURI()).addSuperClass(newClass);
                        } else {
                            getOntology1SameClasses(ont2Subclass).forEach(ontClass -> ontClass.addSuperClass(newClass));
                        }
                        break;
                    }
                }
            }
            if (merged) {
                continue;
            }
            Optional<OntClass> classInOntology1Optional = findClassInOntology1(ont2Subclass);
            if (classInOntology1Optional.isEmpty()) {
                ontology2HandledClasses.add(ont2Subclass.getLocalName());
                ontModel1.createClass(ont2Subclass.getURI()).addSuperClass(newClass);
            } else {
                getOntology1SameClasses(ont2Subclass).forEach(ontClass -> ontClass.addSuperClass(newClass));
            }
        }
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

}
