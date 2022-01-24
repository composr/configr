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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonschema.core.report.AbstractProcessingReport;
import com.github.fge.jsonschema.core.report.LogLevel;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
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

  void mockMetadata(Optional<Metadata> metadata) {
    Mockito.when(metadataService.getMetadataForNamespace(any(String.class))).thenReturn(metadata);
  }

  void mockNoValidationMetadata() {
    Metadata metadata = new Metadata();
    metadata.setValidationLevel(ValidationLevel.NONE);
    metadata.setSchema("I am a schema");
    mockMetadata(Optional.of(metadata));
  }

  void mockLooseMetadata() {
    Metadata metadata = new Metadata();
    metadata.setValidationLevel(ValidationLevel.LOOSE);
    metadata.setSchema("I am a schema");
    mockMetadata(Optional.of(metadata));
  }

  void mockStrictMetadata() {
    Metadata metadata = new Metadata();
    metadata.setValidationLevel(ValidationLevel.STRICT);
    metadata.setSchema("I am a schema");
    mockMetadata(Optional.of(metadata));
  }

  ProcessingReport getEmptyReport() {
    return new EmptyProcessingReport();
  }

  /**
   * This has to be its own actual Class instead of an inline implementation so it has a
   * CanonicalName - without which an NPE will be thrown from the toString(). boo.
   */
  private class EmptyProcessingReport extends AbstractProcessingReport {
    @Override
    public void log(LogLevel level, ProcessingMessage message) {}
  }

  @BeforeEach
  public void reset() {
    Mockito.reset(schemaValidationService, configService, objectMapper);
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
        any());

    Optional<SchemaValidationResult> result =
        schemaValidationService.getValidationReport(getMockedNamespace());

    assertThat(result.get().isSuccess()).isTrue();
  }

  @Test
  public void testValidateNamespaceWithValidationLevelLOOSEAndInvalidJSONShallFail()
      throws Exception {

    mockLooseMetadata();

    // Simulating invalid json
    SchemaValidationResult unsuccessfulValidationResult =
        new SchemaValidationResult(false, getEmptyReport());
    Mockito.doReturn(unsuccessfulValidationResult).when(schemaValidationService).validateJSON(any(),
        any());

    Optional<SchemaValidationResult> result =
        schemaValidationService.getValidationReport(getMockedNamespace());
    assertThat(result.get().isSuccess()).isFalse();
  }


  @Test
  public void testValidateNamespaceWithValidationLevelSTRICTAndValidJSONShallSucceed()
      throws Exception {

    mockStrictMetadata();

    // Simulating valid json
    SchemaValidationResult successfulValidationResult =
        new SchemaValidationResult(true, getEmptyReport());
    Mockito.doReturn(successfulValidationResult).when(schemaValidationService).validateJSON(any(),
        any());

    Optional<SchemaValidationResult> result =
        schemaValidationService.getValidationReport(getMockedNamespace());

    assertThat(result.get().isSuccess()).isTrue();
  }

  @Test
  public void testValidateNamespaceWithValidationLevelSTRICTAndInvalidJSONShallThrowException()
      throws Exception {

    mockStrictMetadata();

    // Simulating invalid json
    SchemaValidationResult unsuccessfulValidationResult =
        new SchemaValidationResult(false, getEmptyReport());
    Mockito.doReturn(unsuccessfulValidationResult).when(schemaValidationService).validateJSON(any(),
        any());

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

    SchemaValidationResult unsuccessfulValidationResult =
        new SchemaValidationResult(false, getEmptyReport());
    Mockito.doReturn(unsuccessfulValidationResult).when(schemaValidationService).validateJSON(any(),
        any());

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
