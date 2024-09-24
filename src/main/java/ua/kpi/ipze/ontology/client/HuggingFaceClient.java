package ua.kpi.ipze.ontology.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ua.kpi.ipze.ontology.dao.SemanticCompatibilityRequest;
import ua.kpi.ipze.ontology.dto.huggingface.TextGenerationResponse;
import ua.kpi.ipze.ontology.dto.huggingface.TextGenerationsRequest;

import java.util.List;

@FeignClient(
        value = "hugging-face-ai-client",
        url = "https://api-inference.huggingface.co/models",
        configuration = HuggingFaceConfig.class
)
public interface HuggingFaceClient {

    @PostMapping("/sentence-transformers/all-MiniLM-L6-v2")
    List<Double> getSemanticCompatibility(@RequestBody SemanticCompatibilityRequest request);

    @PostMapping("/microsoft/Phi-3-mini-4k-instruct")
    TextGenerationResponse getSemanticCompatibility(@RequestBody TextGenerationsRequest request);
}
