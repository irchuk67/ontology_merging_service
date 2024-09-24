package ua.kpi.ipze.ontology.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ua.kpi.ipze.ontology.service.OntologyService;

@RestController
@RequestMapping("/ontologies")
@Slf4j
@RequiredArgsConstructor
public class OntologyController {

    private final OntologyService ontologyService;

    @GetMapping
    public byte[] getCurrentOntology() {
        return ontologyService.getOntology();
    }

    @PutMapping
    public void mergeOntology(@RequestPart MultipartFile file) {
        ontologyService.mergeOntologies(file);
    }

}
