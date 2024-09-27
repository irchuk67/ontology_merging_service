package ua.kpi.ipze.ontology.service.io;

import ua.kpi.ipze.ontology.dto.ClassRelation;

public interface IOService {

    ClassRelation askForRelation(String class1, String class2);

}
