package ua.kpi.ipze.ontology.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import ua.kpi.ipze.ontology.exception.TechnicalException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

@Repository
@Slf4j
public class OntologyDao {

    private static final String DIRECTORY = "ontology";
    private static final String ONTOLOGY_FILE_NAME = "ontology";
    private static final String ONTOLOGY_FILE_SUFFIX = ".rdf";

    private volatile AtomicInteger currentVersion = new AtomicInteger(0);

    @Value("${ontology.file.root.location}")
    private String rootLocation;

    public synchronized byte[] getTheLatestVersion() {
        String fileName = new StringBuilder()
                .append(rootLocation)
                .append(File.separator)
                .append(DIRECTORY)
                .append(File.separator)
                .append(ONTOLOGY_FILE_NAME)
                .append(currentVersion.get())
                .append(ONTOLOGY_FILE_SUFFIX)
                .toString();
        File file = new File(fileName);
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            return fileInputStream.readAllBytes();
        } catch (IOException e) {
            throw new TechnicalException("Could not read ontology file", e);
        }
    }

    public synchronized void updateOntology(byte[] newFile) {
        String fileName = new StringBuilder()
                .append(rootLocation)
                .append(File.separator)
                .append(DIRECTORY)
                .append(File.separator)
                .append(ONTOLOGY_FILE_NAME)
                .append(currentVersion.incrementAndGet())
                .append(ONTOLOGY_FILE_SUFFIX)
                .toString();
        File file = new File(fileName);
        try(FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            fileOutputStream.write(newFile);
        } catch (IOException e) {
            throw new TechnicalException("Could not write new ontology file", e);
        }
    }

}
