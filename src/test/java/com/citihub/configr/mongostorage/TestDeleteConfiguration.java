package com.citihub.configr.mongostorage;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.citihub.configr.MongoConfiguration;
import com.citihub.configr.base.IntegrationTest;
import com.citihub.configr.namespace.Namespace;
import com.citihub.configr.storage.StoreOperations;
import com.citihub.configr.storage.StoreOperationsFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClient;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestDeleteConfiguration extends IntegrationTest {

  private final String SAMPLE =
      "{\"x\":{\"z\":{\"y\":{\"a\":{\"f\":{\"boo\":{\"foo\":[\"ballz\",\"bazz\"]},\"ba\":{\"nee\":\"nah\"}}}}}}}";
  private final String DELETE_BOO_FROM_LEFT =
      "{\"x\":{\"z\":{\"y\":{\"a\":{\"f\":{\"ba\":{\"nee\":\"nah\"}}}}}}}";

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  protected MongoClient mongoClient;

  @MockBean
  protected MongoTemplate mongoTemplate;

  @MockBean
  protected MongoMetadataRepository metadataRepo;

  @MockBean
  protected MongoConfiguration mongoConfig;

  @MockBean
  protected GridFsTemplate template;

  @Autowired
  protected StoreOperationsFactory factory;

  @SpyBean
  protected StoreOperations mongoOperations;

  @BeforeEach
  public void setupMock() throws JsonProcessingException {

    Mockito.doReturn((new Namespace("x", new ObjectMapper().readValue(SAMPLE, Map.class), "/x")))
        .when(mongoOperations).findByPath(any(String.class));

    Mockito.doAnswer(new Answer<Namespace>() {
      @Override
      public Namespace answer(InvocationOnMock invocation) throws Throwable {
        return (Namespace) invocation.getArgument(3);
      }
    }).when(mongoOperations).saveNamespace(any(String[].class), anyBoolean(), anyBoolean(),
        any(Namespace.class));
  }

  @Test
  public void testDelete() throws Exception {
    mockMvc
        .perform(delete("/configuration/x/z/y/a/f/boo").content(SAMPLE)
            .contentType(MediaType.APPLICATION_JSON))
        .andDo(print()).andExpect(status().isOk()).andExpect(content().json(DELETE_BOO_FROM_LEFT));
  }

  @Test
  public void testDeleteNotFound() throws Exception {
    mockMvc.perform(delete("/configuration/a/b/c/its/easy")).andDo(print())
        .andExpect(status().isNotFound());
  }

}
