package com.citihub.configr.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import com.citihub.configr.base.IntegrationTest;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestVersioningE2E extends IntegrationTest {

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
    mockMvc.perform(put("/configuration/x").content(readResource("smallConfig.json"))
        .contentType(MediaType.APPLICATION_JSON)).andExpect(status().is2xxSuccessful());
    mockMvc.perform(put("/configuration/x").content(readResource("mediumConfig.json"))
        .contentType(MediaType.APPLICATION_JSON)).andExpect(status().is2xxSuccessful());
  }

  @Test
  public void testGetVersions() throws Exception {
    MvcResult result = mockMvc.perform(get("/version/x").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is2xxSuccessful()).andReturn();

    String content = result.getResponse().getContentAsString();

    List<Map<String, Object>> nss =
        new ObjectMapper().readValue(content, new TypeReference<List<Map<String, Object>>>() {});

    assertThat(nss.size()).isGreaterThanOrEqualTo(2);
  }

}
