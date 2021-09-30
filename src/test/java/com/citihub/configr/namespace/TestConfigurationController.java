package com.citihub.configr.namespace;

import static org.assertj.core.api.Assertions.assertThat;
import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;

@Tag("unit")
@TestInstance(Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
public class TestConfigurationController {

  @MockBean
  private NamespaceService configurationService;

  @InjectMocks
  private NamespaceController configController;

  @Test
  public void testTrimPath() {
    HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    Mockito.when(request.getRequestURI()).thenReturn("/configuration/x/y/z");

    assertThat(configController.getTrimmedPath(request)).isEqualTo("/x/y/z");
  }
}
