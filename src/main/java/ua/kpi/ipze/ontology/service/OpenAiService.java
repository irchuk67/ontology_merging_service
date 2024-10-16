package ua.kpi.ipze.ontology.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.jena.ontology.OntClass;
import org.springframework.stereotype.Service;
import ua.kpi.ipze.ontology.client.OpenAiClient;
import ua.kpi.ipze.ontology.dto.openai.*;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenAiService {

    private static final String ARRAY_TO_ARRAY_SEMANTIC_COMPATIBILITY_PROMPT = """
             You are one of the most proficient linguistic specialists, who are also proficient in data science;
             Your task to check semantic compatibility between each words in these 2 arrays pairwise: %s and all of those - %s.
             Provide result as a floating point number from 0.0 to 1.0, where 0.0- completely different meaning, 1.0 - totally the same word with same meaning:
               1) if words are synonyms, has different spelling, or they are subset of each other - provide value from 0.7 to 0.99
               3) words are exactly the same - 1.0
               4) other cases - less then 0.699
            Take into account logical compatibility, usages of provided words, but not the domain. The more words has the same meaning, the bigger this number should be
            Respond STRICTLY IN STRING AS JSON FORMAT:
            { "result" : [{"word1": word1, "word2": word2, "value": value}]}, where word1 is word from the first array,
             word2 is word from the second array that is compared with main word, value is result of comparison in floating point numbers, like 0.567890.
            Each value in the result list should correspond to the provided word to be compared.
            If you have met in the list exactly the same word, as the first one - DO NOT SKIP IT, and place in the correct order value 1.0.
             When you prepared answer - check results and read prompt one again. Check, whether there are some mistakes in compatibility between words.
             Return ONLY STRING AS JSON, without any markers, comments or anything - ONLY JSON!!! Skip The comments - provide result number in the same order, as words in array.
             COMMENTS ARE BANNED!
            """;

    private static final String ONTOLOGY_CLASSES_SEMANTIC_COMPATIBILITY_PROMPT = """
             You are one of the most proficient linguistic specialists, who are also proficient in data science;
             You need to define, whether two ontologies has intersected domains, or they don`t have any intersections.
             
             Ontology 1 represents with classes: %s.
             Ontology 2 represents with classes: %s.
             
             Do next steps:
             1) Define domain of the ontology 1, based on provided classes. Then do the same for the ontology 2.
             2) Define, whether there are any intersections between those domains of these ontologies.
             3) Respond with a single word: true, or false. true means that these ontologies have at least something, what intersects between them,
             false - if these ontologies are completely different, and don`t anything related.
             
             YOUR RESPONSE SHOULD HAVE ONLY ONE WORD: true OR false !!!
            """;

    private final OpenAiClient openAiClient;

    public SemanticCompatibilityDto getSemanticCompatibility(Collection<String> words, Collection<String> toCompare) {
        ChatCompletionsOptions chatCompletionsOptions = ChatCompletionsOptions.builder()
                .temperature(0.5)
                .messages(List.of(
                        new ChatRequestUserMessage(String.format(ARRAY_TO_ARRAY_SEMANTIC_COMPATIBILITY_PROMPT, words.toString(), toCompare.toString()))
                ))
                .model("gpt-4o")
                .build();
        ChatCompletions completions = openAiClient.completions(chatCompletionsOptions);
        try {
            String content = completions.getChoices().get(0).getMessage().getContent();
            if (content.startsWith("```json")) {
                content = content.replace("```json", "```")
                        .replace("```", "");
            }
            return new ObjectMapper().readValue(content, SemanticCompatibilityDto.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean checkOntologiesCompatibility(List<OntClass> classes1, List<OntClass> classes2) {
        ChatCompletionsOptions chatCompletionsOptions = ChatCompletionsOptions.builder()
                .temperature(0.5)
                .messages(List.of(
                        new ChatRequestUserMessage(String.format(ONTOLOGY_CLASSES_SEMANTIC_COMPATIBILITY_PROMPT, classes1.toString(), classes2.toString()))
                ))
                .model("gpt-4o")
                .build();
        ChatCompletions completions = openAiClient.completions(chatCompletionsOptions);
        String content = completions.getChoices().get(0).getMessage().getContent();
        return Boolean.parseBoolean(content);
    }
}
