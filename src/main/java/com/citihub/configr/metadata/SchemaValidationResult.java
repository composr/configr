package com.citihub.configr.metadata;

import com.citihub.configr.schema.ValidationReportSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import lombok.Data;

@Data
public class SchemaValidationResult {

  private boolean isSuccess;

  @JsonSerialize(using = ValidationReportSerializer.class)
  private ProcessingReport report;

  public SchemaValidationResult(boolean isSuccess, ProcessingReport report) {
    this.isSuccess = isSuccess;
    this.report = report;
  }

}
