package ua.kpi.ipze.ontology.dto.openai;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SemanticCompatibilityDto {
    private List<SemanticCompatibilityPair> result;
}
