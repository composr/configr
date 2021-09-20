package com.citihub.configr.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import com.citihub.configr.mongostorage.MongoNamespaceQueries;
import com.mongodb.client.MongoClient;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(Lifecycle.PER_CLASS)
public class TestConfigurationController {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private MongoClient mongoClient;
  
  @MockBean
  private MongoNamespaceQueries nsQueries;
  
  @MockBean
  private ConfigurationService configurationService;
  
  @Test
  public void testNotFound() throws Exception {
    mockMvc.perform(get("/foo/bar/baz")).andDo(print()).andExpect(status().isNotFound());
  }

  @Test
  public void testGetBadRequest() throws Exception {
    mockMvc.perform(get("/configuration")).andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  public void testGetFound() throws Exception {
    mockMvc.perform(get("/configuration/x")).andDo(print()).andExpect(status().isOk());
  }
  
}
