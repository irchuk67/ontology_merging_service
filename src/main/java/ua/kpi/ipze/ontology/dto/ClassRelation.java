package ua.kpi.ipze.ontology.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum ClassRelation {

    EQUIVALENT(1),
    SUPERCLASS(2),
    SUBCLASS(3),
    NO_RELATION(4);

    private final int option;

    public static ClassRelation readFromOption(String option) {
        int value = Integer.parseInt(option);
        return Arrays.stream(ClassRelation.values())
                .filter(classRelation -> classRelation.option == value)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Can not identify option"));
    }

    public static ClassRelation fromName(String value) {
        return Arrays.stream(ClassRelation.values())
                .filter(classRelation -> classRelation.name().contentEquals(value))
                .findFirst()
                .orElseThrow();
    }

}
