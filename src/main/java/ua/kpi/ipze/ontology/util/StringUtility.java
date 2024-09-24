package ua.kpi.ipze.ontology.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class StringUtility {

    public static StringObject normalizeString(String string) {
        if(string == null || string.isEmpty()) {
            throw new IllegalArgumentException("String is null or empty");
        }
        String normalized = string.replaceAll("\\s+", "")
                .replaceAll("[^a-zA-Z0-9]+", "")
                .toLowerCase();
        return new StringObject(string, normalized);
    }

    public static boolean equalNormalized(String s1, String s2) {
        return normalizeString(s1).normalized().equals(normalizeString(s2).normalized());
    }

}
