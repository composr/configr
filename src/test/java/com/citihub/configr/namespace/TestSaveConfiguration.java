package com.citihub.configr.namespace;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.citihub.configr.mongostorage.MongoConfigRepository;
import com.citihub.configr.namespace.Namespace;
import com.mongodb.client.MongoClient;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(Lifecycle.PER_CLASS)
public class TestSaveConfiguration {

  private static final String POST_SAVE_CONTENT =
      "{\"x\":{\"y\":{\"z\":{ \"foo\": { \"bar\": { \"baz\": [ { \"buzz\": \"bizz\" }, { \"foo2\": \"bar2\" } ] } } }}}}";

  private static final String SAMPLE_CONTENT =
      "{ \"foo\": { \"bar\": { \"baz\": [ { \"buzz\": \"bizz\" }, { \"foo2\": \"bar2\" } ] } } }";

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private MongoClient mongoClient;

  @MockBean
  private MongoConfigRepository configurationRepository;

  @BeforeAll
  public void setupMock() {
    when(configurationRepository.save(any(Namespace.class))).thenAnswer(new Answer<Namespace>() {
      public Namespace answer(InvocationOnMock invocation) throws Throwable {
        return (Namespace) invocation.getArguments()[0];
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
        .perform(post("/configuration/x/y/z").content(SAMPLE_CONTENT)
            .contentType(MediaType.APPLICATION_JSON))
        .andDo(print()).andExpect(status().isOk()).andExpect(content().json(POST_SAVE_CONTENT));
  }
}
