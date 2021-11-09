package com.citihub.configr.jsonpath;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import com.citihub.configr.base.IntegrationTest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestJsonPathQuery extends IntegrationTest {

  private static final String QUERY = "$..*[?(@.boo)]";

  private static final String EXPECTED_RESPONSE =
      "{\"results\":[{\"boo\":{\"fooz\":[\"ballz\",\"bazz\"],\"foo\":[\"ballz\",\"bazz\"]},\"ba\":{\"nee\":\"nah\"}},{\"boo\":{\"fooz\":[\"ball\",\"bazz\"]},\"ba\":{\"nee\":\"nah\"}}]}";

  @Autowired
  private MockMvc mockMvc;

  private Path workingDir;

  private String readResource(String resourceName) throws IOException {
    return Files.readString(this.workingDir.resolve(resourceName));
  }

  @BeforeAll
  public void init() {
    this.workingDir = Path.of("", "src/test/resources");
  }

  @BeforeEach
  public void setupData() throws Exception {
    mockMvc.perform(put("/configuration/x").content(readResource("largeConfig.json"))
        .contentType(MediaType.APPLICATION_JSON));
  }

  @Test
  public void testQueryPost() throws Exception {
    MvcResult result =
        mockMvc.perform(post("/query/x").content(QUERY).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().is2xxSuccessful()).andReturn();

    String content = result.getResponse().getContentAsString();

    assertThat(content).isEqualTo(EXPECTED_RESPONSE);
  }

  @Test
  public void testQueryGet() throws Exception {
    MvcResult result = mockMvc
        .perform(
            get("/query/x").queryParam("jsonPath", QUERY).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is2xxSuccessful()).andReturn();

    String content = result.getResponse().getContentAsString();

    assertThat(content).isEqualTo(EXPECTED_RESPONSE);
  }

  @Test
  public void testQueryException() throws Exception {
    MvcResult result =
        mockMvc.perform(post("/query/x").content("$$$$").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest()).andReturn();

    String content = result.getResponse().getContentAsString();

    assertThat(content).startsWith("Illegal character");
  }
}
