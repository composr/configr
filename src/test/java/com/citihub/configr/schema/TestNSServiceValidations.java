package com.citihub.configr.schema;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import com.citihub.configr.base.UnitTest;
import com.citihub.configr.exception.SchemaValidationException;
import com.citihub.configr.metadata.Metadata;
import com.citihub.configr.metadata.Metadata.ValidationLevel;
import com.citihub.configr.metadata.MetadataService;
import com.citihub.configr.metadata.SchemaValidationResult;
import com.citihub.configr.mongostorage.MongoConfigRepository;
import com.citihub.configr.mongostorage.MongoOperations;
import com.citihub.configr.namespace.Namespace;
import com.citihub.configr.namespace.NamespaceService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonschema.core.report.ListProcessingReport;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestNSServiceValidations extends UnitTest {

  private final String TEST_JSON =
      "{\"x\":{\"z\":{\"y\":{\"a\":{\"f\":{\"boo\":{\"foo\":[\"ballz\",\"bazz\"]},\"ba\":{\"nee\":\"nah\"}}}}},\"y\":{\"a\":{\"f\":{\"boo\":{\"fooz\":[\"ball\",\"bazz\"]},\"ba\":{\"nee\":\"nah\"}}}}}}";

  @Mock
  private MongoConfigRepository configRepo;

  @Mock
  private MongoOperations nsQueries;

  @Mock
  private MetadataService metadataService;

  @Spy
  private ObjectMapper objectMapper;

  @Spy
  @InjectMocks
  private SchemaValidationService schemaValidationService;

  @Spy
  @InjectMocks
  private NamespaceService configService;


  Map<String, Object> getMockedNamespaceValue() {
    return Collections.singletonMap("y", "z");
  }

  Namespace getMockedNamespace() {
    return new Namespace("x", getMockedNamespaceValue(), "/x/y");
  }

  void mockObjectMapperExceptions() throws JsonProcessingException {
    Mockito.doThrow(JsonProcessingException.class).when(objectMapper).writeValueAsString(any());
  }

  void mockMetadata(Optional<Metadata> metadata) {
    Mockito.when(metadataService.getMetadataForNamespace(any(String.class))).thenReturn(metadata);
  }

  void mockNoSchemaMetadata() {
    Metadata metadata = new Metadata();
    metadata.setValidationLevel(ValidationLevel.NONE);
    mockMetadata(Optional.of(metadata));
  }

  void mockNoValidationMetadata() {
    Metadata metadata = new Metadata();
    metadata.setValidationLevel(ValidationLevel.NONE);
    metadata.setSchema(Collections.singletonMap("message", "I am a schema"));
    mockMetadata(Optional.of(metadata));
  }

  void mockLooseMetadata() {
    Metadata metadata = new Metadata();
    metadata.setValidationLevel(ValidationLevel.LOOSE);
    metadata.setSchema(Collections.singletonMap("message", "I am a schema"));
    mockMetadata(Optional.of(metadata));
  }

  void mockStrictMetadata() {
    Metadata metadata = new Metadata();
    metadata.setValidationLevel(ValidationLevel.STRICT);
    metadata.setSchema(Collections.singletonMap("message", "I am a schema"));
    mockMetadata(Optional.of(metadata));
  }

  ProcessingReport getEmptyReport() {
    return new ListProcessingReport();
  }

  @BeforeEach
  public void reset() {
    Mockito.reset(schemaValidationService, configService, objectMapper);
  }

  @Test
  public void testMetadataWithoutSchema() throws Exception {
    mockNoSchemaMetadata();

    assertThat(schemaValidationService.getValidationReport(getMockedNamespace())).isEmpty();
  }

  @Test
  public void testProcessingExceptionWithMapSchema() throws Exception {
    mockObjectMapperExceptions();

    assertThrows(SchemaValidationException.class,
        () -> schemaValidationService.validateJSON("json", Collections.EMPTY_MAP));
  }

  @Test
  public void testProcessingExceptionGettingReport() throws Exception {
    mockObjectMapperExceptions();
    mockStrictMetadata();
    mockUnsuccessfulReportResult();

    assertThrows(SchemaValidationException.class,
        () -> schemaValidationService.getValidationReport("json", "schema"));
  }

  @Test
  public void testValidateNamespaceWithoutMetadataShallSkip() throws Exception {

    mockMetadata(Optional.empty());

    Optional<SchemaValidationResult> result =
        schemaValidationService.getValidationReport(getMockedNamespace());

    System.out.println("Result is " + result);
    assertThat(result).isEmpty();
  }

  @Test
  public void testValidateNamespaceWithValidationLevelNONEShallSkip() throws Exception {

    mockNoValidationMetadata();

    Optional<SchemaValidationResult> result =
        schemaValidationService.getValidationReport(getMockedNamespace());

    assertThat(result).isEmpty();
  }

  @Test
  public void testValidateNamespaceWithValidationLevelLOOSEAndValidJSONShallSucceed()
      throws Exception {

    mockLooseMetadata();

    // Simulating valid json
    SchemaValidationResult successfulValidationResult =
        new SchemaValidationResult(true, getEmptyReport());
    Mockito.doReturn(successfulValidationResult).when(schemaValidationService).validateJSON(any(),
        any(Map.class));

    Optional<SchemaValidationResult> result =
        schemaValidationService.getValidationReport(getMockedNamespace());

    assertThat(result.get().isSuccess()).isTrue();
  }

  @Test
  public void testValidateNamespaceWithValidationLevelLOOSEAndInvalidJSONShallFail()
      throws Exception {

    mockLooseMetadata();

    // Simulating invalid json
    mockUnsuccessfulReportResult();

    Optional<SchemaValidationResult> result =
        schemaValidationService.getValidationReport(getMockedNamespace());
    assertThat(result.get().isSuccess()).isFalse();
  }

  private void mockUnsuccessfulReportResult() {
    SchemaValidationResult unsuccessfulValidationResult =
        new SchemaValidationResult(false, getEmptyReport());
    Mockito.doReturn(unsuccessfulValidationResult).when(schemaValidationService).validateJSON(any(),
        any(Map.class));
  }


  @Test
  public void testValidateNamespaceWithValidationLevelSTRICTAndValidJSONShallSucceed()
      throws Exception {

    mockStrictMetadata();

    // Simulating valid json
    SchemaValidationResult successfulValidationResult =
        new SchemaValidationResult(true, getEmptyReport());
    Mockito.doReturn(successfulValidationResult).when(schemaValidationService).validateJSON(any(),
        any(Map.class));

    Optional<SchemaValidationResult> result =
        schemaValidationService.getValidationReport(getMockedNamespace());

    assertThat(result.get().isSuccess()).isTrue();
  }

  @Test
  public void testValidateNamespaceWithValidationLevelSTRICTAndInvalidJSONShallThrowException()
      throws Exception {

    mockStrictMetadata();

    mockUnsuccessfulReportResult();

    assertThrows(SchemaValidationException.class,
        () -> schemaValidationService.getValidationReport(getMockedNamespace()));
  }

  @Test
  public void testStoreNamespaceShallCallValidate() throws Exception {

    Mockito.doReturn(Optional.of(new SchemaValidationResult(true, getEmptyReport())))
        .when(schemaValidationService).getValidationReport(any(Namespace.class));

    Mockito.when(configRepo.save(any())).thenReturn(new Namespace());

    configService.storeNamespace(getMockedNamespaceValue(), "/sampleNamespace", false, false);

    // Verify validateNamespace was called
    Mockito.verify(schemaValidationService, Mockito.times(1))
        .getValidationReport(any(Namespace.class));
  }

  @Test
  public void testStoreNamespaceWithValidationLevelSTRICTAndInvalidJSONShallThrowException()
      throws Exception {

    mockStrictMetadata();

    mockUnsuccessfulReportResult();

    assertThrows(SchemaValidationException.class, () -> configService
        .storeNamespace(getMockedNamespaceValue(), "/sampleNamespace", false, false));
  }

  @Test
  public void testStoreNamespaceWithValidationSKIPPEDShallSave() throws Exception {

    Mockito.doReturn(Optional.empty()).when(schemaValidationService)
        .getValidationReport(any(Namespace.class));

    // Simulating DB save
    Mockito.when(configRepo.save(any())).thenReturn(new Namespace());

    Namespace savedNamespace =
        configService.storeNamespace(getMockedNamespaceValue(), "/sampleNamespace", false, false);

    assertThat(savedNamespace).isNotNull();
  }

  @Test
  public void testStoreNamespaceWithValidationSUCCEEDEDShallSave() throws Exception {

    Mockito.doReturn(Optional.of(new SchemaValidationResult(true, getEmptyReport())))
        .when(schemaValidationService).getValidationReport(any(Namespace.class));
    // Mockito.when(objectMapper.writeValueAsString(any())).thenReturn("");

    // Simulating DB save
    Mockito.when(configRepo.save(any())).thenReturn(new Namespace());

    Namespace savedNamespace =
        configService.storeNamespace(getMockedNamespaceValue(), "/sampleNamespace", false, false);

    assertThat(savedNamespace).isNotNull();
  }

  @Test
  public void testStoreNamespaceWithLooseValidationFAILEDShallSave() throws Exception {

    // Add mocked response object
    MockHttpServletResponse response = new MockHttpServletResponse();
    RequestContextHolder
        .setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest(), response));

    // Simulating skipped validation
    Mockito.doReturn(Optional.of(new SchemaValidationResult(false, getEmptyReport())))
        .when(schemaValidationService).getValidationReport(any(Namespace.class));

    configService.storeNamespace(getMockedNamespaceValue(), "/sampleNamespace", false, false);

    assertThat(response.getHeader("X-Schema-Validity")).isEqualTo("false");
  }

}
