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
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ua.kpi.ipze.ontology.client.HuggingFaceClient;
import ua.kpi.ipze.ontology.dao.OntologyDao;
import ua.kpi.ipze.ontology.dao.SemanticCompatibilityRequest;
import ua.kpi.ipze.ontology.util.StringUtility;

import java.io.*;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class OntologyService implements CommandLineRunner {

    private static final List<String> DEFAULT_CLASSES = List.of(
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
            "dateTime"
    );

    private final OntologyDao ontologyDao;
    private final HuggingFaceClient huggingFaceClient;
    private final OpenAiService openAiService;
    private final GeminiAiService geminiAiService;

    public byte[] getOntology() {
        return ontologyDao.getTheLatestVersion();
    }

    public void mergeOntologies(MultipartFile file) {
        try {
            mergeOntologies(new ByteArrayInputStream(getOntology()), file.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void mergeOntologies(InputStream inputStreamOntology1, InputStream inputStreamOntology2) {
//        OntModel resultModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF);

        OntModel ontology1 = readOntologyModel(inputStreamOntology1);
        ExtendedIterator<OntClass> classes1 = extractOntologyClasses(ontology1);

        OntModel ontology2 = readOntologyModel(inputStreamOntology2);

        while (classes1.hasNext()) {
            OntClass ontology1Class = getClassFromIterator(classes1);
            ExtendedIterator<OntClass> classes2 = extractOntologyClasses(ontology2);
            List<String> classes2Names = classes2.mapWith(ontClass -> ontClass.getLocalName()).toList();
            List<Double> semanticCompatibility = openAiService.getSemanticCompatibility(ontology1Class.getLocalName(), classes2Names).result();

            for (int i = 0; i < semanticCompatibility.size(); i++) {
                log.info(
                        "Class '{}' to '{}' is compatible: [{}]",
                        ontology1Class.getLocalName(),
                        classes2Names.get(i),
                        semanticCompatibility.get(i)
                );
            }
            System.out.println();
            break;
        }
    }

    private OntClass getClassFromIterator(ExtendedIterator<OntClass> classes) {
        OntClass next;
        do {
            next = classes.next();
        } while(next != null && (StringUtility.equalNormalized(next.getLocalName(), "thing") || StringUtility.equalNormalized(next.getLocalName(), "nothing")));

        return next;
    }

    public static byte[] modelToByteArray(OntModel model) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        model.write(baos, "RDF/XML");
        return baos.toByteArray();
    }

    @Override
    public void run(String... args) throws Exception {
        mergeOntologies(
                new ByteArrayInputStream(getOntology()),
                new FileInputStream(Paths.get("src", "main", "resources", "ontology","lab2.owx").toFile())
        );
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

    private ExtendedIterator<OntClass> extractOntologyClasses(OntModel ontologyModel) {
        return ontologyModel.listNamedClasses()
                .filterDrop(ontClass -> DEFAULT_CLASSES.contains(ontClass.getLocalName()));
    }

    private InputStream convert(InputStream inputStream) throws OWLOntologyCreationException, IOException, OWLOntologyStorageException {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = manager.loadOntologyFromOntologyDocument(inputStream);

        // save the file into RDF/XML format
        //in this case, my ontology file and format already manage prefix
        RDFXMLOntologyFormat rdfxmlFormat = new RDFXMLOntologyFormat();
        File temp = File.createTempFile(UUID.randomUUID().toString(), ".rdf");
        manager.saveOntology(ontology, rdfxmlFormat, IRI.create(temp));
        return new FileInputStream(temp);
    }

}

