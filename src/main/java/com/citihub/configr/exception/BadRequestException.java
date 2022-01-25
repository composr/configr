package com.citihub.configr.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

public class BadRequestException extends HttpClientErrorException {

  public BadRequestException() {
    super(HttpStatus.BAD_REQUEST,
        "Bad request - you have supplied something syntactically incorrect.");
  }

}
