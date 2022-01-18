package com.citihub.configr.metadata;

import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.citihub.configr.exception.SchemaValidationException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;

@Service
public class SchemaValidationService {

  private ObjectMapper objectMapper;

  public SchemaValidationService(@Autowired ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public SchemaValidationResult validateJSON(final String json, final String schema)
      throws SchemaValidationException {

    if (json == null || schema == null)
      throw new SchemaValidationException("Inputs cannot be null");

    try {
      final JsonNode jsonObj = this.objectMapper.readTree(json);
      final JsonNode schemaObj = this.objectMapper.readTree(schema);

      final JsonSchema jsonSchema = JsonSchemaFactory.byDefault().getJsonSchema(schemaObj);

      ProcessingReport report = jsonSchema.validate(jsonObj);

      return new SchemaValidationResult(report.isSuccess(), report.toString());

    } catch (IOException | ProcessingException e) {
      e.printStackTrace();
      throw new SchemaValidationException(
          "An error occurred while processing schema validation. Check logs.");
    }

  }

}
