package ua.kpi.ipze.ontology.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ua.kpi.ipze.ontology.service.OntologyService;

import java.io.IOException;

@RestController
@RequestMapping("/ontologies")
@Slf4j
@RequiredArgsConstructor
@CrossOrigin
public class OntologyController {

    private final OntologyService ontologyService;

    @GetMapping
    public byte[] getCurrentOntology() {
        return ontologyService.getOntology();
    }

    @PutMapping("/{sessionId}")
    public void mergeOntology(@RequestPart MultipartFile file, @PathVariable String sessionId) throws IOException {
        log.info("Received file to merge ontologies with sessionId={}", sessionId);
        ontologyService.mergeOntologies(file.getInputStream(), sessionId);
    }

}
