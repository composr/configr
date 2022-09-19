package com.citihub.configr;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.citihub.configr.base.SecureIntegrationTest;


public class TestSecurity extends SecureIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Test
  public void testShouldForbidRequest() throws Exception {
    mockMvc.perform(get("/configuration/x").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());
  }

  @WithMockToken(authorities = "read")
  @Test
  public void testCanRead() throws Exception {
    mockMvc.perform(get("/configuration/x").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  @WithMockToken(authorities = "none")
  @Test
  public void testCannotRead() throws Exception {
    mockMvc.perform(get("/configuration/x").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());
  }

  @WithMockToken(authorities = "write")
  @Test
  public void testCanWrite() throws Exception {
    mockMvc.perform(put("/configuration/x").contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isOk());
  }

  @WithMockToken(authorities = "read")
  @Test
  public void testCannotWrite() throws Exception {
    mockMvc.perform(put("/configuration/x").contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isForbidden());
  }


  @WithMockToken(authorities = "delete")
  @Test
  public void testCanDelete() throws Exception {
    mockMvc
        .perform(delete("/configuration/x").contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isNotFound());
  }

  @WithMockToken(authorities = "read")
  @Test
  public void testCannotDelete() throws Exception {
    mockMvc
        .perform(delete("/configuration/x").contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isForbidden());
  }
}
