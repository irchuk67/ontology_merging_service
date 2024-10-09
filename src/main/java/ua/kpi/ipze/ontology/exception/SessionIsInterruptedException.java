package ua.kpi.ipze.ontology.exception;

public class SessionIsInterruptedException extends RuntimeException{
    public SessionIsInterruptedException(String message) {
        super(message);
    }
}
