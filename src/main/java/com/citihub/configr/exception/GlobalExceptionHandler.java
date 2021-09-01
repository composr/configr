package com.citihub.configr.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<String> handleJsonMappingException(BadURIException ex) {
    return new ResponseEntity<String>(ex.getStatusText(), HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(Throwable.class)
  public ResponseEntity<String> handleDefaultException(Throwable ex) {
    String errorResponse = "Unknown error occurred.";
    return new ResponseEntity<String>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
