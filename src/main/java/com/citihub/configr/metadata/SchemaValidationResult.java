package com.citihub.configr.metadata;

import lombok.Data;

@Data
public class SchemaValidationResult {

  private boolean isSuccess;

  private String message;

  public SchemaValidationResult(boolean isSuccess, String message) {
    this.isSuccess = isSuccess;
    this.message = message;
  }

}
