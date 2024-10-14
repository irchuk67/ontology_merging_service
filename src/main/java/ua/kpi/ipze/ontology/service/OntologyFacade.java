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
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ua.kpi.ipze.ontology.entity.Ontology;
import ua.kpi.ipze.ontology.exception.OntologiesAreNotCompatibleException;
import ua.kpi.ipze.ontology.service.io.WebSocketHandler;

import java.io.*;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OntologyFacade {

    private final OntologyService ontologyService;
    private final OpenAiService openAiService;
    private final WebSocketHandler webSocketHandler;

    public byte[] getOntology() {
        return ontologyService.getOntology();
    }

    public void mergeOntologies(MultipartFile file, String sessionId) {
        Ontology ontology = ontologyService.getActualOntology();
        log.info("Start to merge ontology with id {}", ontology.getId());
        ByteArrayInputStream inputStream = new ByteArrayInputStream(ontology.getOwlContent().getBytes());
        OntModel ontology1 = readOntologyModel(inputStream);
        List<OntClass> classes1 = extractOntologyClasses(ontology1).toList();
        OntModel ontology2 = null;
        try {
            ontology2 = readOntologyModel(file.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        List<OntClass> classes2 = extractOntologyClasses(ontology2).toList();
        boolean ontologiesAreCompatible = openAiService.checkOntologiesCompatibility(classes1, classes2);
        if(!ontologiesAreCompatible) {
            webSocketHandler.closeConnection(sessionId);
            throw new OntologiesAreNotCompatibleException("Ontology has nothing in common with current. Please upload another one, or update current ontology");
        }
        ontologyService.mergeOntologies(classes1, classes2, ontology1, ontology2, sessionId, ontology.getVersion());
    }

    private OntModel readOntologyModel(InputStream inputStream) {
        OntModel ontologyModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF);
        try {
            ontologyModel.read(convert(inputStream), null, FileUtils.langXML);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return ontologyModel;
    }

    public static ExtendedIterator<OntClass> extractOntologyClasses(OntModel ontologyModel) {
        return ontologyModel.listNamedClasses()
                .filterDrop(ontClass -> OntologyService.DEFAULT_CLASSES.contains(ontClass.getLocalName()));
    }

    private InputStream convert(InputStream inputStream) throws OWLOntologyCreationException, IOException, OWLOntologyStorageException {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = manager.loadOntologyFromOntologyDocument(inputStream);

        RDFXMLOntologyFormat rdfxmlFormat = new RDFXMLOntologyFormat();
        File temp = File.createTempFile(UUID.randomUUID().toString(), ".rdf");
        manager.saveOntology(ontology, rdfxmlFormat, IRI.create(temp));
        return new FileInputStream(temp);
    }

}
