package com.citihub.configr;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.citihub.configr.base.SecureIntegrationTest;
import com.citihub.configr.metadata.ACL;
import com.citihub.configr.metadata.Metadata;
import com.citihub.configr.metadata.MetadataService;
import com.citihub.configr.metadata.Metadata.ValidationLevel;

public class TestSecurity extends SecureIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private MetadataService mockMetaDataService;

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

  @WithMockToken(authorities = "test_service_meta_read")
  @Test
  public void testCanReadMetadata() throws Exception {
    Set<ACL> acls = new HashSet<ACL>();
    acls.add(new ACL("test_service_meta_read", true, false, false));

    Metadata testMetadata = new Metadata("/metadata/services/test_md_read_service",
        "meta data for test service", acls, null, ValidationLevel.NONE, null);

    Mockito
        .when(mockMetaDataService
            .getMetadataForNamespace("/configuration/services/test_md_read_service"))
        .thenReturn(Optional.of(testMetadata));

    mockMvc
        .perform(get("/configuration/services/test_md_read_service")
            .contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isNotFound());
  }

  @WithMockToken(authorities = "read")
  @Test
  public void testCannotReadMetadata() throws Exception {
    Set<ACL> acls = new HashSet<ACL>();
    acls.add(new ACL("test_service_meta_read", true, false, false));

    Metadata testMetadata = new Metadata("/metadata/services/test_md_read_service",
        "meta data for test service", acls, null, ValidationLevel.NONE, null);

    Mockito
        .when(mockMetaDataService
            .getMetadataForNamespace("/configuration/services/test_md_read_service"))
        .thenReturn(Optional.of(testMetadata));

    mockMvc
        .perform(get("/configuration/services/test_md_read_service")
            .contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isForbidden());
  }

  @WithMockToken(authorities = "test_service_meta_write")
  @Test
  public void testCanWriteMetadata() throws Exception {
    // mock metadata service and return acls
    Set<ACL> acls = new HashSet<ACL>();
    acls.add(new ACL("test_service_meta_write", false, true, false));

    Metadata testMetadata = new Metadata("/metadata/services/test_md_write_service",
        "meta data for test service", acls, null, ValidationLevel.NONE, null);

    Mockito
        .when(mockMetaDataService
            .getMetadataForNamespace("/configuration/services/test_md_write_service"))
        .thenReturn(Optional.of(testMetadata));

    mockMvc.perform(post("/configuration/services/test_md_write_service")
        .contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isOk());
  }

  @WithMockToken(authorities = "write")
  @Test
  public void testCannotWriteMetadata() throws Exception {
    // mock metadata service and return acls
    Set<ACL> acls = new HashSet<ACL>();
    acls.add(new ACL("test_service_meta_write", false, true, false));

    Metadata testMetadata = new Metadata("/metadata/services/test_md_write_service",
        "meta data for test service", acls, null, ValidationLevel.NONE, null);

    Mockito
        .when(mockMetaDataService
            .getMetadataForNamespace("/configuration/services/test_md_write_service"))
        .thenReturn(Optional.of(testMetadata));

    mockMvc
        .perform(post("/configuration/services/test_md_write_service")
            .contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isForbidden());
  }

  @WithMockToken(authorities = "test_service_meta_admin")
  @Test
  public void testCanDeleteMetadata() throws Exception {
    // mock metadata service and return acls
    Set<ACL> acls = new HashSet<ACL>();
    // admin can do everything ;-)
    acls.add(new ACL("test_service_meta_admin", true, true, true));

    Metadata testMetadata = new Metadata("/metadata/services/test_md_delete_service",
        "meta data for test service", acls, null, ValidationLevel.NONE, null);

    Mockito
        .when(mockMetaDataService
            .getMetadataForNamespace("/configuration/services/test_md_delete_service"))
        .thenReturn(Optional.of(testMetadata));

    mockMvc
        .perform(delete("/configuration/services/test_md_delete_service")
            .contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isNotFound());
  }

  @WithMockToken(authorities = "write")
  @Test
  public void testCannotDeleteMetadata() throws Exception {
    Set<ACL> acls = new HashSet<ACL>();
    acls.add(new ACL("test_service_meta_admin", true, true, true));

    Metadata testMetadata = new Metadata("/metadata/services/test_md_delete_service",
        "meta data for test service", acls, null, ValidationLevel.NONE, null);

    Mockito
        .when(mockMetaDataService
            .getMetadataForNamespace("/configuration/services/test_md_delete_service"))
        .thenReturn(Optional.of(testMetadata));

    mockMvc
        .perform(delete("/configuration/services/test_md_delete_service")
            .contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isForbidden());
  }
}
