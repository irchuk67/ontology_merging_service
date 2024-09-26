package ua.kpi.ipze.ontology.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.jena.ontology.ObjectProperty;
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

    public static final Double SIMILARITY_THRESHOLD = 0.9;
    private static final Double COMPATIBILITY_THRESHOLD = 0.5;
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
    private final MergingOntologyService mergingOntologyService;

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
        OntModel ontology1 = readOntologyModel(inputStreamOntology1);
        List<OntClass> classes1 = extractOntologyClasses(ontology1).toList();
//        List<OntClass> classes1List = classes1.toList();
//        OntClass teacher1Class = classes1List.stream()
//                .filter(ontClass -> ontClass.getLocalName().contentEquals("Teacher"))
//                .findFirst().get();
        OntModel ontology2 = readOntologyModel(inputStreamOntology2);
        List<OntClass> classes2 = extractOntologyClasses(ontology2).toList();
//        OntClass teacher2Class = classes2.stream()
//                .filter(ontClass -> ontClass.getLocalName().contentEquals("Teacher"))
//                .findFirst().get();

        for (OntClass ontology1Class : classes1){
            List<String> classes2Names = classes2.stream()
                    .map(ontClass -> ontClass.getLocalName()).toList();

            List<Double> semanticCompatibility = openAiService.getSemanticCompatibility(ontology1Class.getLocalName(), classes2Names).result();

            for (int i = 0; i < semanticCompatibility.size(); i++) {
                log.info(
                        "Class '{}' to '{}' is compatible: [{}]",
                        ontology1Class.getLocalName(),
                        classes2Names.get(i),
                        semanticCompatibility.get(i)
                );
                if (semanticCompatibility.get(i) >= SIMILARITY_THRESHOLD) {
                    int finalI = i;
                    OntClass equivalentClass = classes2.stream().filter(cls -> cls.getLocalName().contentEquals(classes2Names.get(finalI))).findFirst().get();
                    mergingOntologyService.mergeEquivalentClasses(ontology1Class, equivalentClass);
                }
            }
            System.out.println();
        }
        ontologyDao.updateOntology(modelToByteArray(ontology1));
        System.exit(0);
    }

    private OntClass getClassFromIterator(ExtendedIterator<OntClass> classes) {
        OntClass next;
        do {
            next = classes.next();
        } while (next != null && (StringUtility.equalNormalized(next.getLocalName(), "thing") || StringUtility.equalNormalized(next.getLocalName(), "nothing")));

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
                new FileInputStream(Paths.get("src", "main", "resources", "ontology", "base.rdf").toFile())
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

