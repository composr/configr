package com.citihub.configr.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import com.jayway.jsonpath.JsonPathException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<String> handleJsonMappingException(BadURIException ex) {
    return new ResponseEntity<String>(ex.getStatusText(), HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<String> handleBadJsonPathQuery(JsonPathException ex) {
    return new ResponseEntity<String>(ex.getMessage(), HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public ResponseEntity<String> handleJsonMappingException(NotFoundException ex) {
    return new ResponseEntity<String>(ex.getStatusText(), HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ResponseEntity<String> handleSaveFailureException(SaveFailureException ex) {
    return new ResponseEntity<String>(ex.getStatusText(), HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler
  @ResponseStatus(HttpStatus.CONFLICT)
  public ResponseEntity<String> handleJsonMappingException(ConflictException ex) {
    return new ResponseEntity<String>(ex.getStatusText(), HttpStatus.CONFLICT);
  }

  @ExceptionHandler
  @ResponseStatus(HttpStatus.FORBIDDEN)
  public ResponseEntity<String> handleAccessDenied(AccessDeniedException ex) {
    return new ResponseEntity<String>(ex.getMessage(), HttpStatus.FORBIDDEN);
  }

  @ExceptionHandler
  @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
  public ResponseEntity<String> handleSchemaValidationFailure(SchemaValidationException ex) {
    return new ResponseEntity<String>(ex.getStatusText(), HttpStatus.UNPROCESSABLE_ENTITY);
  }

  @ExceptionHandler(Throwable.class)
  public ResponseEntity<String> handleDefaultException(Throwable ex) {
    String errorResponse = "Unknown error occurred.";

    log.error("{}", ex);

    return new ResponseEntity<String>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
