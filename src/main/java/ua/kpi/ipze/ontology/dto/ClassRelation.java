package ua.kpi.ipze.ontology.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum ClassRelation {

    EQUIVALENT(1),
    SUBCLASS(2),
    SUPERCLASS(3),
    DISJOINT(4);

    private final int option;

    public static ClassRelation readFromOption(String option) {
        int value = Integer.parseInt(option);
        return Arrays.stream(ClassRelation.values())
                .filter(classRelation -> classRelation.option == value)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Can not identify option"));
    }

}
