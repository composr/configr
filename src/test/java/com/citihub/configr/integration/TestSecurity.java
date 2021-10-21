package com.citihub.configr.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.citihub.configr.base.SecureIntegrationTest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestSecurity extends SecureIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Test
  public void testShouldForbidRequest() throws Exception {
    mockMvc.perform(get("/configuration/x").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());
  }

}
