package com.citihub.configr;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest({"authentication.enabled=true", "authentication.proxy.host=foo",
    "authentication.proxy.port=bar"})
@AutoConfigureMockMvc(addFilters = true)
@TestInstance(Lifecycle.PER_CLASS)
@Tag("authProxy")
@Tag("integration")
@ActiveProfiles("test")
public class TestSecurityWithProxy {

  @Autowired
  private MockMvc mockMvc;

  @Test
  public void testShouldForbidRequestCoveringProxy() throws Exception {
    mockMvc.perform(get("/configuration/x").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());
  }

}
