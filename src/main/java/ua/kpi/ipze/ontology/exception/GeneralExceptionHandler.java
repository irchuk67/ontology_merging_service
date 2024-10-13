package ua.kpi.ipze.ontology.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GeneralExceptionHandler {

    public record ErrorMessage(String message){}

    @ExceptionHandler(OntologiesAreNotCompatibleException.class)
    @ResponseStatus(HttpStatus.EXPECTATION_FAILED)
    public ErrorMessage handleOntologiesAreNotCompatibleException(OntologiesAreNotCompatibleException e) {
        return new ErrorMessage(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorMessage handleGenericException(Exception e) {
        return new ErrorMessage(e.getMessage());
    }

}
