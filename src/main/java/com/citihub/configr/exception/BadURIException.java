package com.citihub.configr.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

public class BadURIException extends HttpClientErrorException {

  public BadURIException() {
    super(HttpStatus.BAD_REQUEST,
        "Request URI is not valid. " + "It must start with /configuration, /version or /metadata "
            + "and be followed by a proper namespace.");
  }

}
