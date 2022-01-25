package com.citihub.configr.schema;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import com.citihub.configr.base.UnitTest;
import com.citihub.configr.exception.SchemaValidationException;
import com.citihub.configr.metadata.SchemaValidationResult;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SchemaValidationServiceTest extends UnitTest {

  private SchemaValidationService validationService;

  private String jsonSchema;

  private String jsonValid;

  private Path workingDir;

  private String readResource(String resourceName) throws IOException {
    return Files.readString(this.workingDir.resolve(resourceName));
  }

  @BeforeAll
  public void setup() throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    validationService = new SchemaValidationService(mapper, null);
    this.workingDir = Path.of("", "src/test/resources");
    this.jsonSchema = readResource("jsonSchema.json");
    this.jsonValid = readResource("jsonValidSchema.json");
  }

  @Test
  public void testValidateJSONWithInvalidMinimumShallFail() throws Exception {

    SchemaValidationResult result = validationService
        .validateJSON(readResource("jsonInvalidMinimumPriceValue.json"), jsonSchema);

    assertThat(result.isSuccess()).isFalse();

  }

  @Test
  public void testValidateJSONWithInvalidTypeShallFail() throws Exception {

    SchemaValidationResult result =
        validationService.validateJSON(readResource("jsonInvalidIDType.json"), jsonSchema);

    assertThat(result.isSuccess()).isFalse();

  }

  @Test
  public void testValidateJSONWithMissingRequiredFieldShallFail() throws Exception {

    SchemaValidationResult result = validationService
        .validateJSON(readResource("jsonInvalidMissingRequiredField.json"), jsonSchema);

    assertThat(result.isSuccess()).isFalse();

  }

  @Test
  public void testValidateJSONWithValidShallSucceed() {

    SchemaValidationResult result = validationService.validateJSON(jsonValid, jsonSchema);

    assertThat(result.isSuccess()).isTrue();

  }

  @Test
  public void testValidateJSONWithNullsShallRaiseException() {

    assertThrows(SchemaValidationException.class,
        () -> validationService.validateJSON(null, (Map<String, Object>) null));

  }

  @Test
  public void testValidateJSONWithNullStringsShallRaiseException() {

    assertThrows(SchemaValidationException.class,
        () -> validationService.validateJSON("foo", (String) null));

  }

  @Test
  public void testValidateJSONWithNullJSONShallRaiseException() {

    assertThrows(SchemaValidationException.class,
        () -> validationService.validateJSON(null, jsonSchema));

  }

  @Test
  public void testValidateJSONWithNullSchemaShallRaiseException() {

    assertThrows(SchemaValidationException.class,
        () -> validationService.validateJSON(jsonValid, (Map<String, Object>) null));

  }

  @Test
  public void testValidateJSONWithMalformedShallRaiseException() throws Exception {

    assertThrows(SchemaValidationException.class,
        () -> validationService.validateJSON(readResource("jsonInvalidSyntax.json"), jsonSchema));

  }

}
