package ua.kpi.ipze.ontology.dao;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record SemanticCompatibilityRequest(Inputs inputs) {

    public record Inputs(

            @JsonProperty("source_sentence")
            String sourceSentence,

            @JsonProperty("sentences")
            List<String> sentences

    ) {}
}
