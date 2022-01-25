package com.citihub.configr.schema;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.web.servlet.MockMvc;
import com.citihub.configr.base.IntegrationTest;
import com.citihub.configr.metadata.Metadata;
import com.citihub.configr.metadata.Metadata.ValidationLevel;
import com.citihub.configr.metadata.MetadataService;
import com.citihub.configr.metadata.SchemaValidationResult;
import com.citihub.configr.namespace.Namespace;
import com.citihub.configr.namespace.NamespaceService;
import com.github.fge.jsonschema.core.report.AbstractProcessingReport;
import com.github.fge.jsonschema.core.report.LogLevel;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.github.fge.jsonschema.core.report.ProcessingReport;

public class SchemaControllerIntegrationTest extends IntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private MetadataService metadataService;

  @MockBean
  private NamespaceService namespaceService;

  @SpyBean
  SchemaValidationService schemaValidationService;

  @InjectMocks
  SchemaController schemaController;

  Map<String, Object> getMockedNamespaceValue() {
    return Collections.singletonMap("y", "z");
  }

  void mockReportSuccess() {
    Mockito.doReturn(new SchemaValidationResult(true, getEmptyReport()))
        .when(schemaValidationService).validateJSON(any(String.class), any(String.class));
  }

  void mockReportFail() {
    Mockito.doReturn(new SchemaValidationResult(false, getEmptyReport()))
        .when(schemaValidationService).validateJSON(any(String.class), any(String.class));
  }

  void mockNamespace() {
    Mockito.when(namespaceService.fetchNamespace(any()))
        .thenReturn(new Namespace("x", getMockedNamespaceValue(), "/x/y"));
  }

  void mockMetadata(Optional<Metadata> metadata) {
    Mockito.when(metadataService.getMetadataForNamespace(any(String.class))).thenReturn(metadata);
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
    return new EmptyProcessingReport();
  }

  @BeforeEach
  public void setup() {
    Mockito.reset(metadataService, namespaceService);
    mockNamespace();
  }

  /**
   * This has to be its own actual Class instead of an inline implementation so it has a
   * CanonicalName - without which an NPE will be thrown from the toString(). boo.
   */
  private class EmptyProcessingReport extends AbstractProcessingReport {
    @Override
    public void log(LogLevel level, ProcessingMessage message) {}
  }

  @Test
  public void testGetSuccessReport() throws Exception {
    mockStrictMetadata();
    mockReportSuccess();
    mockMvc.perform(get("/validity/x/y")).andDo(print()).andExpect(status().isOk())
        .andExpect(jsonPath("$.success", Matchers.is(true)));
  }

  @Test
  public void testGetEmptyValidityReport() throws Exception {
    mockLooseMetadata();
    mockReportFail();
    mockMvc.perform(get("/validity/x/y")).andDo(print()).andExpect(status().isOk())
        .andExpect(jsonPath("$.success", Matchers.is(false)));
  }
}
