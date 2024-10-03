package ua.kpi.ipze.ontology.dto;

import java.util.List;

public record ClassConflictMessage(
        String class1,
        String class2,
        List<ClassRelation> options) {
}
