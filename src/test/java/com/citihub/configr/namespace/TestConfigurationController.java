package com.citihub.configr.namespace;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import com.citihub.configr.mongostorage.MongoOperations;
import com.citihub.configr.namespace.NamespaceController;
import com.citihub.configr.namespace.NamespaceService;
import com.mongodb.client.MongoClient;

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
