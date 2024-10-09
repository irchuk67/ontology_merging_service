package ua.kpi.ipze.ontology.service;

import java.util.ArrayList;
import java.util.List;

public class MessageCollectorService {

    private static final String ADD_NEW_CLASS = "Class \"%s\" from uploaded ontology was added";
    private static final String ADD_EQUIVALENT_CLASS = "Class \"%s\" from uploaded ontology is equivalent to \"%s\"";
    private static final String ADD_SUBCLASS_CLASS = "Class \"%s\" from uploaded ontology is a child class of \"%s\"";
    private static final String ADD_SUPERCLASS_CLASS = "Class \"%s\" from uploaded ontology is a parent class of \"%s\"";
    private static final String ADD_NEW_PROPERTY = "New property \"%s\" from uploaded ontology was added to ontology";
    private static final String EXISTING_PROPERTY_MERGED = "Property \"%s\" from uploaded ontology was merged with property \"%s\"";
    private static final String EXISTING_PROPERTY_EXTENDED_WITH_RANGE = "Property \"%s\" was extended with new range \"%s\"";
    private static final String ADD_STATEMENT_TO_INDIVIDUAL = "Individual \"%s\" was extended with a statement: \"%s\" -> \"%s\"";
    private static final String ADD_NEW_INDIVIDUAL = "Individual \"%s\" was added to ontology";

    private final List<String> performedActions = new ArrayList<>();

    public void addNewClass(String ont2Class) {
        performedActions.add(String.format(ADD_NEW_CLASS, ont2Class));
    }

    public void addEquivalentClass(String ont1Class, String ont2Class) {
        performedActions.add(String.format(ADD_EQUIVALENT_CLASS, ont2Class, ont1Class));
    }

    public void addSuperClass(String ont1Class, String ont2Class) {
        performedActions.add(String.format(ADD_SUPERCLASS_CLASS, ont2Class, ont1Class));
    }

    public void addSubClass(String ont1Class, String ont2Class) {
        performedActions.add(String.format(ADD_SUBCLASS_CLASS, ont2Class, ont1Class));
    }

    public void addNewProperty(String property) {
        performedActions.add(String.format(ADD_NEW_PROPERTY, property));
    }

    public void addMergedProperty(String property1, String property2) {
        performedActions.add(String.format(EXISTING_PROPERTY_MERGED, property2, property1));
    }

    public void addExtendedWithRangeProperty(String property1, String range) {
        performedActions.add(String.format(EXISTING_PROPERTY_EXTENDED_WITH_RANGE, property1, range));
    }

    public void addStatementToIndividual(String individual, String statement, String object) {
        performedActions.add(String.format(ADD_STATEMENT_TO_INDIVIDUAL, individual, statement, object));
    }

    public void addNewIndividual(String individual) {
        performedActions.add(String.format(ADD_NEW_INDIVIDUAL, individual));
    }

    public List<String> getPerformedActions() {
        return performedActions;
    }

}