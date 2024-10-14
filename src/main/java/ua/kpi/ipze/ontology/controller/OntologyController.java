package ua.kpi.ipze.ontology.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ua.kpi.ipze.ontology.entity.AutoGeneratedOntology;
import ua.kpi.ipze.ontology.service.AutoGeneratedOntologyService;
import ua.kpi.ipze.ontology.service.OntologyFacade;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/ontologies")
@Slf4j
@RequiredArgsConstructor
@CrossOrigin
public class OntologyController {

    private final AutoGeneratedOntologyService autoGeneratedOntologyService;
    private final OntologyFacade ontologyFacade;

    @GetMapping
    public byte[] getCurrentOntology() {
        return ontologyFacade.getOntology();
    }

    @PutMapping("/{sessionId}")
    public ResponseEntity<String> mergeOntology(@RequestPart MultipartFile file, @PathVariable String sessionId) throws IOException {
        log.info("Received file to merge ontologies with sessionId={}", sessionId);
        ontologyFacade.mergeOntologies(file, sessionId);
        return ResponseEntity.ok(file.getOriginalFilename());
    }

    @GetMapping("/merge-options")
    public List<AutoGeneratedOntology> getAutoGeneratedOntologies(){
        return autoGeneratedOntologyService.getAutogeneratedOntologies();
    }

}
