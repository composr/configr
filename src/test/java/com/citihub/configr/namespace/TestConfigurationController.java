package com.citihub.configr.namespace;

import static org.assertj.core.api.Assertions.assertThat;
import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import com.citihub.configr.base.UnitTest;

public class TestConfigurationController extends UnitTest {

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
