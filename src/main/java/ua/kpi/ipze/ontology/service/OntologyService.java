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
import ua.kpi.ipze.ontology.service.io.WebSocketHandler;
import ua.kpi.ipze.ontology.dao.OntologyDao;
import ua.kpi.ipze.ontology.service.io.WebSocketIOService;

import java.io.*;
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

    private final OntologyDao ontologyDao;
    private final OpenAiService openAiService;
    private final WebSocketHandler webSocketHandler;

    public byte[] getOntology() {
        return ontologyDao.getTheLatestVersion();
    }

    @Async("ontologyMergingExecutor")
    public void mergeOntologies(MultipartFile file, String sessionId) {
        try {
            mergeOntologies(new ByteArrayInputStream(getOntology()), file.getInputStream(), sessionId);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void mergeOntologies(InputStream inputStreamOntology1, InputStream inputStreamOntology2, String sessionId) {
        OntModel ontology1 = readOntologyModel(inputStreamOntology1);
        List<OntClass> classes1 = extractOntologyClasses(ontology1).toList();
        OntModel ontology2 = readOntologyModel(inputStreamOntology2);
        List<OntClass> classes2 = extractOntologyClasses(ontology2).toList();

        MergingClassOntologyService mergingClassOntologyService = new MergingClassOntologyService(openAiService, new WebSocketIOService(sessionId, webSocketHandler), classes1, classes2);
        mergingClassOntologyService.mergeOntologies();
        MergingIndividualOntologyService mergingIndividualOntologyService = new MergingIndividualOntologyService(ontology1, ontology2);
        mergingIndividualOntologyService.mergeOntologies();

        ontologyDao.updateOntology(modelToByteArray(ontology1));
    }

    public static byte[] modelToByteArray(OntModel model) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        model.write(baos, "RDF/XML");
        return baos.toByteArray();
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
                .filterDrop(ontClass -> DEFAULT_CLASSES.contains(ontClass.getLocalName()));
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

