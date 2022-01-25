package com.citihub.configr.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

public class SchemaValidationException extends HttpClientErrorException {

  public SchemaValidationException(String validationError) {
    super(HttpStatus.UNPROCESSABLE_ENTITY, validationError);
  }

}
