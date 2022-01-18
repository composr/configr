package com.citihub.configr.metadata;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import com.citihub.configr.base.UnitTest;
import com.citihub.configr.exception.SchemaValidationException;
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
  public void testValidateJSONWithInvalidMinimumShallFail() {

    String jsonInvalidMinimumPriceValue = """
        {
        	"id": 1,
        	"name": "book",
        	"price": 0
        }
        """;
    var result = validationService.validateJSON(jsonInvalidMinimumPriceValue, jsonSchema);
    assertThat(result.getIsSuccess()).isFalse();

  }

  @Test
  public void testValidateJSONWithInvalidTypeShallFail() {

    String jsonInvalidIDType = """
        {
            "id": "not a number",
            "name": "book",
            "price": 0
        }
        """;
    var result = validationService.validateJSON(jsonInvalidIDType, jsonSchema);
    assertThat(result.getIsSuccess()).isFalse();

  }

  @Test
  public void testValidateJSONWithMissingRequiredFieldShallFail() {

    String jsonInvalidMissingField = """
        {
            "id": 1,
            "price": 2
        }
        """;
    var result = validationService.validateJSON(jsonInvalidMissingField, jsonSchema);
    assertThat(result.getIsSuccess()).isFalse();

  }

  @Test
  public void testValidateJSONWithValidShallSucceed() {

    var result = validationService.validateJSON(jsonValid, jsonSchema);
    assertThat(result.getIsSuccess()).isTrue();

  }

  @Test
  public void testValidateJSONWithNullShallRaiseException() {

    assertThrows(SchemaValidationException.class, () -> validationService.validateJSON(null, null));
    assertThrows(SchemaValidationException.class,
        () -> validationService.validateJSON(null, jsonSchema));
    assertThrows(SchemaValidationException.class,
        () -> validationService.validateJSON(jsonValid, null));
  }

  @Test
  public void testValidateJSONWithMalformedShallRaiseException() {

    String jsonInvalidSyntax = """
        {
            "id": 1,
            "name": "book
            "price": 2
        }
        """;
    assertThrows(SchemaValidationException.class,
        () -> validationService.validateJSON(jsonInvalidSyntax, jsonSchema));

  }

}
