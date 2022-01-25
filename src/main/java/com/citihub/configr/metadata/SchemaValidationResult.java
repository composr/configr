package com.citihub.configr.metadata;

import com.github.fge.jsonschema.core.report.ProcessingReport;
import lombok.Data;

@Data
public class SchemaValidationResult {

  private boolean isSuccess;

  private ProcessingReport report;

  public SchemaValidationResult(boolean isSuccess, ProcessingReport report) {
    this.isSuccess = isSuccess;
    this.report = report;
  }

}
