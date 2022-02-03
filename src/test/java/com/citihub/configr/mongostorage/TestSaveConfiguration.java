package com.citihub.configr.mongostorage;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.citihub.configr.MongoConfiguration;
import com.citihub.configr.base.IntegrationTest;
import com.citihub.configr.exception.SaveFailureException;
import com.citihub.configr.namespace.Namespace;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.mongodb.client.MongoClient;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestSaveConfiguration extends IntegrationTest {

  private static final String POST_SAVE_CONTENT =
      "{\"x\":{\"y\":{\"z\":{ \"foo\": { \"bar\": { \"baz\": [ { \"buzz\": \"bizz\" }, { \"foo2\": \"bar2\" } ] } } }}}}";

  private static final String SAMPLE_CONTENT =
      "{ \"foo\": { \"bar\": { \"baz\": [ { \"buzz\": \"bizz\" }, { \"foo2\": \"bar2\" } ] } } }";

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

  @BeforeEach
  public void setupMock() throws JsonProcessingException {

    when(mongoTemplate.save(any(Namespace.class))).thenAnswer(new Answer<Namespace>() {
      @Override
      public Namespace answer(InvocationOnMock invocation) throws Throwable {
        return (Namespace) invocation.getArgument(0);
      }
    });

  }

  @Test
  public void testBadSaveRequest() throws Exception {
    mockMvc
        .perform(
            post("/configuration").content(SAMPLE_CONTENT).contentType(MediaType.APPLICATION_JSON))
        .andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  public void testConflate() throws Exception {
    mockMvc
        .perform(patch("/configuration/x/y/z").content(SAMPLE_CONTENT)
            .contentType(MediaType.APPLICATION_JSON))
        .andDo(print()).andExpect(status().isOk()).andExpect(content().json(POST_SAVE_CONTENT));
  }

  @Test
  public void testSaveFailureException() throws Exception {
    doThrow(new SaveFailureException()).when(mongoTemplate).save(any());

    mockMvc
        .perform(post("/configuration/x/y/z").content(SAMPLE_CONTENT)
            .contentType(MediaType.APPLICATION_JSON))
        .andDo(print()).andExpect(status().isInternalServerError());

    Mockito.reset(mongoTemplate);
  }

  @Test
  public void testRandomException() throws Exception {
    doThrow(new NullPointerException()).when(mongoTemplate).save(any());

    mockMvc
        .perform(post("/configuration/x/y/z").content(SAMPLE_CONTENT)
            .contentType(MediaType.APPLICATION_JSON))
        .andDo(print()).andExpect(status().isInternalServerError());

    Mockito.reset(mongoTemplate);
  }

}
