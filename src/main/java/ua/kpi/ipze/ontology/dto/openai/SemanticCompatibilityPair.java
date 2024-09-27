package ua.kpi.ipze.ontology.dto.openai;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SemanticCompatibilityPair {
    private String word1;
    private String word2;
    private Double value;
}
