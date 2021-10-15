package com.citihub.configr.namespace;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import com.citihub.configr.base.IntegrationTest;
import com.citihub.configr.mongostorage.MongoOperations;
import com.mongodb.client.MongoClient;

public class TestURIVariations extends IntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  /**
   * You may have success locally removing this but you will have a bad time in GitLab CI land, so
   * don't remove please.
   */
  private MongoClient mongoClient;

  @MockBean
  private MongoOperations nsQueries;

  @MockBean
  private NamespaceService configurationService;

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
