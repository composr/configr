package com.citihub.configr.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

public class ConflictException extends HttpClientErrorException {

  public ConflictException() {
    super(HttpStatus.CONFLICT, "Requested resource already exists. "
        + "Use PUT or PATCH if you'd like to overwrite or merge with it.");
  }

}
