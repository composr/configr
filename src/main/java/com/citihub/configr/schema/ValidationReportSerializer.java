package com.citihub.configr.schema;

import java.io.IOException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.github.fge.jsonschema.core.report.ListProcessingReport;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ValidationReportSerializer extends JsonSerializer<ListProcessingReport> {

  @Override
  public void serialize(ListProcessingReport report, JsonGenerator gen,
      SerializerProvider serializers) throws IOException {
    gen.writeObject(report.asJson());
  }

}
