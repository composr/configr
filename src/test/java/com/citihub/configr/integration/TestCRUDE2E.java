package com.citihub.configr.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
public class TestCRUDE2E extends IntegrationTest {

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
  public void testGet() throws Exception {
    MvcResult result =
        mockMvc.perform(get("/configuration/x/z").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().is2xxSuccessful()).andReturn();

    assertThat(result.getResponse().getContentAsString()).isEqualTo(
        "{\"z\":{\"y\":{\"a\":{\"f\":{\"boo\":{\"fooz\":[\"ballz\",\"bazz\"],\"foo\":[\"ballz\",\"bazz\"]},\"ba\":{\"nee\":\"nah\"}}}}}}");
  }

  @Test
  public void testGetNotFound() throws Exception {
    mockMvc.perform(get("/configuration/abc/itseasy").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  @Test
  public void testGetBadRequest() throws Exception {
    mockMvc.perform(get("/configuration").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void testPut() throws Exception {
    MvcResult result = mockMvc
        .perform(put("/configuration/x").content(readResource("smallConfig.json"))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is2xxSuccessful()).andReturn();

    assertThat(result.getResponse().getContentAsString())
        .isEqualTo("{\"x\":{\"a\":{\"f\":{\"boo\":\"fooz\"}}}}");
  }

  @Test
  public void testPutToChild() throws Exception {
    MvcResult result = mockMvc
        .perform(put("/configuration/x/y").content(readResource("smallConfig.json"))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is2xxSuccessful()).andReturn();

    assertThat(result.getResponse().getContentAsString()).isEqualTo(
        "{\"x\":{\"z\":{\"y\":{\"a\":{\"f\":{\"boo\":{\"fooz\":[\"ballz\",\"bazz\"],\"foo\":[\"ballz\",\"bazz\"]},\"ba\":{\"nee\":\"nah\"}}}}},\"y\":{\"a\":{\"f\":{\"boo\":\"fooz\"}}}}}");
  }

  @Test
  public void testPostConflict() throws Exception {
    mockMvc.perform(post("/configuration/x").content(readResource("smallConfig.json"))
        .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isConflict());
  }

  @Test
  public void testPost() throws Exception {
    MvcResult result = mockMvc
        .perform(post("/configuration/x/a").content(readResource("smallConfig.json"))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is2xxSuccessful()).andReturn();

    assertThat(result.getResponse().getContentAsString()).isEqualTo(
        "{\"x\":{\"z\":{\"y\":{\"a\":{\"f\":{\"boo\":{\"fooz\":[\"ballz\",\"bazz\"],\"foo\":[\"ballz\",\"bazz\"]},\"ba\":{\"nee\":\"nah\"}}}}},\"y\":{\"a\":{\"f\":{\"boo\":{\"fooz\":[\"ball\",\"bazz\"]},\"ba\":{\"nee\":\"nah\"}}}},\"a\":{\"a\":{\"f\":{\"boo\":\"fooz\"}}}}}");

  }

  @Test
  public void testPatchSameAsPost() throws Exception {
    MvcResult result = mockMvc
        .perform(patch("/configuration/x/a").content(readResource("smallConfig.json"))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is2xxSuccessful()).andReturn();

    assertThat(result.getResponse().getContentAsString()).isEqualTo(
        "{\"x\":{\"z\":{\"y\":{\"a\":{\"f\":{\"boo\":{\"fooz\":[\"ballz\",\"bazz\"],\"foo\":[\"ballz\",\"bazz\"]},\"ba\":{\"nee\":\"nah\"}}}}},\"y\":{\"a\":{\"f\":{\"boo\":{\"fooz\":[\"ball\",\"bazz\"]},\"ba\":{\"nee\":\"nah\"}}}},\"a\":{\"a\":{\"f\":{\"boo\":\"fooz\"}}}}}");
  }

  @Test
  public void testPatchDeepMerge() throws Exception {
    MvcResult result = mockMvc
        .perform(patch("/configuration/x").content(readResource("mediumConfig.json"))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is2xxSuccessful()).andReturn();

    assertThat(result.getResponse().getContentAsString()).isEqualTo(
        "{\"x\":{\"z\":{\"y\":{\"a\":{\"f\":{\"boo\":{\"fooz\":[\"ballz\",\"bazz\"],\"foo\":[\"ballz\",\"bazz\"]},\"ba\":{\"nee\":\"nah\"}}}}},\"y\":{\"a\":{\"f\":{\"boo\":{\"fooz\":[\"ball\",\"bazz\"]},\"ba\":{\"nee\":\"nah\"}}},\"ba\":{\"nee\":\"nah\"}},\"a\":{\"f\":{\"boo\":{\"foo\":[\"barn\",\"nrab\"]},\"ba\":{\"nee\":\"nah\"}}}}}");
  }

}
