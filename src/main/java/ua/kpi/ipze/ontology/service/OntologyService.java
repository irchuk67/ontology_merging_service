package ua.kpi.ipze.ontology.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileUtils;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.model.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ua.kpi.ipze.ontology.entity.Ontology;
import ua.kpi.ipze.ontology.repository.OntologyRepository;
import ua.kpi.ipze.ontology.service.io.WebSocketHandler;
import ua.kpi.ipze.ontology.dao.OntologyDao;
import ua.kpi.ipze.ontology.service.io.WebSocketIOService;

import java.io.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class OntologyService {

    public static final List<String> DEFAULT_CLASSES = List.of(
            "Thing",
            "FunctionalProperty",
            "FunctionalProperty",
            "Bag",
            "List",
            "Class",
            "OntologyProperty",
            "Alt",
            "Restriction",
            "SymmetricProperty",
            "ContainerMembershipProperty",
            "Property",
            "Container",
            "Class",
            "Literal",
            "Resource",
            "Statement",
            "Property",
            "ObjectProperty",
            "Datatype",
            "InverseFunctionalProperty",
            "DatatypeProperty",
            "Ontology",
            "TransitiveProperty",
            "Seq",
            "decimal",
            "string",
            "dateTime",
            "integer",
            "date"
    );

    private final OpenAiService openAiService;
    private final WebSocketHandler webSocketHandler;
    private final OntologyRepository ontologyRepository;

    Ontology getActualOntology() {
        return ontologyRepository.findFirstByOrderByDateTimeDesc();
    }

    public byte[] getOntology() {
        Ontology ontology = getActualOntology();
        if(ontology == null) {
            throw new RuntimeException("Could not find ontology");
        }
        return ontology.getOwlContent().getBytes();
    }

    @Async("ontologyMergingExecutor")
    public void mergeOntologies(List<OntClass> classes1, List<OntClass> classes2, OntModel ontology1, OntModel ontology2,  String sessionId) {
        MessageCollectorService messageCollectorService = new MessageCollectorService();
        WebSocketIOService webSocketIOService = new WebSocketIOService(sessionId, webSocketHandler);
        MergingClassOntologyService mergingOntologyService = new MergingClassOntologyService(
                openAiService,
                webSocketIOService,
                classes1,
                classes2,
                messageCollectorService
        );
        mergingOntologyService.mergeOntologies();
        new MergingIndividualOntologyService(ontology1, ontology2, messageCollectorService).mergeOntologies();

        Ontology updatedOntology = Ontology.builder()
                .dateTime(LocalDateTime.now())
                .owlContent(new String(modelToByteArray(ontology1)))
                .build();

        ontologyRepository.save(updatedOntology);
        webSocketIOService.sendPerformedActions(messageCollectorService);
        webSocketHandler.closeConnection(sessionId);
    }

    public static byte[] modelToByteArray(OntModel model) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        model.write(baos, "RDF/XML");
        return baos.toByteArray();
    }

}

