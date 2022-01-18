package com.citihub.configr.metadata;

import com.citihub.configr.base.UnitTest;
import com.citihub.configr.exception.SchemaValidationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SchemaValidationServiceTest extends UnitTest {

  private SchemaValidationService validationService;

  private final String jsonSchema = """
      {
      	"$schema": "http://json-schema.org/draft-04/schema#",
      	"title": "Product",
      	"description": "A product from the catalog",
      	"type": "object",
      	"properties": {
      		"id": {
      			"description": "The unique identifier for a product",
                  "type": "integer"
              },
              "name": {
                  "description": "Name of the product",
                  "type": "string"
              },
              "price": {
                  "type": "number",
                  "minimum": 0,
                  "exclusiveMinimum": true
              }
      	},
      	"required": ["id", "name", "price"]
      }
      """;

  private final String jsonValid = """
      {
      	"id": 1,
      	"name": "book",
      	"price": 2
      }
      """;

  @BeforeAll
  public void setup() {
    ObjectMapper mapper = new ObjectMapper();
    validationService = new SchemaValidationService(mapper);
  }

  @Test
  public void testValidateJSON_WithInvalid_ShallFail() {

    String jsonInvalidPriceValue = """
        {
        	"id": 1,
        	"name": "book",
        	"price": 0
        }
        """;
    // According to schema, "price" shall be exclusively greater than 0, so this should fail
    var result = validationService.validateJSON(jsonInvalidPriceValue, jsonSchema);
    assertThat(result.getIsSuccess()).isFalse();

    String jsonInvalidIDType = """
        {
        	"id": "not a number",
        	"name": "book",
        	"price": 0
        }
        """;
    // According to schema, "id" shall be of type integer, so this should fail
    result = validationService.validateJSON(jsonInvalidIDType, jsonSchema);
    assertThat(result.getIsSuccess()).isFalse();

    String jsonInvalidMissingField = """
        {
        	"id": 1,
        	"price": 2
        }
        """;
    // According to schema, "name" is required, so this should fail
    result = validationService.validateJSON(jsonInvalidMissingField, jsonSchema);
    assertThat(result.getIsSuccess()).isFalse();

  }

  @Test
  public void testValidateJSON_WithValid_ShallSucceed() {

    // According to schema:
    // "id" shall be of type integer
    // "name" shall be of type string
    // "price" shall be exclusively greater than 0
    var result = validationService.validateJSON(jsonValid, jsonSchema);
    assertThat(result.getIsSuccess()).isTrue();
  }

  @Test
  public void testValidateJSON_WithNullOrMalformed_ShallRaiseException() {

    String jsonInvalidSyntax = """
        {
        	"id": 1,
        	"name": "book
        	"price": 2
        }
        """;
    // If json object is malformed this should fail
    assertThrows(SchemaValidationException.class,
        () -> validationService.validateJSON(jsonInvalidSyntax, jsonSchema));

    // If json and/or schema inputs are null this should fail
    assertThrows(SchemaValidationException.class, () -> validationService.validateJSON(null, null));
    assertThrows(SchemaValidationException.class,
        () -> validationService.validateJSON(null, jsonSchema));
    assertThrows(SchemaValidationException.class,
        () -> validationService.validateJSON(jsonValid, null));
  }

}
