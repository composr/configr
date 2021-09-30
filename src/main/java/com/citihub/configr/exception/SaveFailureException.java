package com.citihub.configr.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

public class SaveFailureException extends HttpClientErrorException {

  public SaveFailureException() {
    super(HttpStatus.INTERNAL_SERVER_ERROR,
        "Save operation failed unexpectedly; cowardly refusing to commit transaction "
            + "to avoid corrupted state. Please try again.");
  }

}
