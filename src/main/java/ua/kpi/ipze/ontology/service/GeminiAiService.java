package ua.kpi.ipze.ontology.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ua.kpi.ipze.ontology.client.HuggingFaceClient;
import ua.kpi.ipze.ontology.dto.huggingface.TextGenerationResponse;
import ua.kpi.ipze.ontology.dto.huggingface.TextGenerationsRequest;
import ua.kpi.ipze.ontology.dto.openai.ChatCompletions;
import ua.kpi.ipze.ontology.dto.openai.ChatCompletionsOptions;
import ua.kpi.ipze.ontology.dto.openai.ChatRequestUserMessage;
import ua.kpi.ipze.ontology.dto.openai.SemanticCompatibilityResponse;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class GeminiAiService {

    private static final String PROMPT = """
            You are one of the most proficient linguistic specialists, who are also proficient in data science;
            Your task to check semantic compatibility between words: "%s" and all of those - %s.
            
            Provide result as a floating point number from 0 to 1, where 0 - completely different meaning, 1 - exactly the same words:
                        1) if words are synonyms, has different spelling, or they are subset of each other - provide value from 0.7 to 0.95
                        2) if words are not about the same thing, but share the same domain - provide value between 0.45 and 0.699
                        3) words are the same - 1.0
                        4) other cases - less then 0.499
            Take into account logical compatibility, usages of provided words, but not the domain.
            
            Respond STRICTLY IN JSON FORMAT:
            { "result" : [value1, value2]}, where value1, value2 are floating point numbers, like 0.567890.
            Each value in the result list should correspond to the provided word to be compared.
            If you have met in the list exactly the same word, as the first one - DO NOT SKIP IT, and place in the correct order value 1.0.
            
            When you prepared answer - check results and read prompt one again. Check, whether there are some mistakes in compatibility between words.
            Return ONLY JSON, without any markers, comments or anything - ONLY JSON!!! Skip The comments - provide result number in the same order, as words in array.
            COMMENTS ARE BANNED!
            """;

    private final HuggingFaceClient huggingFaceClient;

    public SemanticCompatibilityResponse getSemanticCompatibility(String word, List<String> toCompare) {
        TextGenerationResponse response = huggingFaceClient.getSemanticCompatibility(new TextGenerationsRequest(String.format(PROMPT, word, toCompare.toString())));
        try {
            return new ObjectMapper().readValue(response.generated_text(), SemanticCompatibilityResponse.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
