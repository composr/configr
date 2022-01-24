package com.citihub.configr.schema;

import java.io.IOException;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.citihub.configr.exception.SchemaValidationException;
import com.citihub.configr.metadata.Metadata;
import com.citihub.configr.metadata.Metadata.ValidationLevel;
import com.citihub.configr.metadata.MetadataService;
import com.citihub.configr.metadata.SchemaValidationResult;
import com.citihub.configr.namespace.Namespace;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SchemaValidationService {

  private ObjectMapper objectMapper;

  private MetadataService metadataService;

  public SchemaValidationService(@Autowired ObjectMapper objectMapper,
      @Autowired MetadataService metadataService) {
    this.objectMapper = objectMapper;
    this.metadataService = metadataService;
  }

  public Optional<SchemaValidationResult> getValidationReport(Namespace namespace)
      throws SchemaValidationException, JsonProcessingException {
    return getValidationReport(objectMapper.writeValueAsString(namespace.getValue()),
        namespace.getNamespace());
  }

  public Optional<SchemaValidationResult> getValidationReport(String nsJson, String namespace)
      throws SchemaValidationException {

    Optional<Metadata> metadata = metadataService.getMetadataForNamespace(namespace);

    if (!hasSchema(metadata)) {
      return Optional.empty();
    }

    return getValidationReport(nsJson, metadata.get());
  }

  private boolean hasSchema(Optional<Metadata> metadata) {
    return metadata.isPresent() && !Strings.isNullOrEmpty(metadata.get().getSchema());
  }

  public Optional<SchemaValidationResult> getValidationReport(String nsJson, Metadata metadata)
      throws SchemaValidationException {

    if (metadata.getValidationLevel() == ValidationLevel.NONE) {
      return Optional.empty();
    }

    SchemaValidationResult result = validateJSON(nsJson, metadata.getSchema());

    if (!result.isSuccess() && metadata.getValidationLevel() == ValidationLevel.STRICT) {
      throw new SchemaValidationException(result.getReport().toString());
    }

    return Optional.of(result);
  }

  public SchemaValidationResult validateJSON(final String json, final String schema)
      throws SchemaValidationException {
    System.out.println(json + " and " + schema);
    if (json == null || schema == null)
      throw new SchemaValidationException("Inputs cannot be null");

    try {
      final JsonNode jsonObj = this.objectMapper.readTree(json);
      final JsonNode schemaObj = this.objectMapper.readTree(schema);

      final JsonSchema jsonSchema = JsonSchemaFactory.byDefault().getJsonSchema(schemaObj);

      ProcessingReport report = jsonSchema.validate(jsonObj);

      return new SchemaValidationResult(report.isSuccess(), report);

    } catch (IOException | ProcessingException e) {
      e.printStackTrace();
      throw new SchemaValidationException(
          "An error occurred while processing schema validation. Check logs.");
    }

  }

}
