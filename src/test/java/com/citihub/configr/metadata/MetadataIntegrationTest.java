package com.citihub.configr.metadata;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.citihub.configr.base.IntegrationTest;
import com.citihub.configr.metadata.Metadata.ValidationLevel;
import com.citihub.configr.mongostorage.MongoMetadataRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClient;

public class MetadataIntegrationTest extends IntegrationTest {

  private Metadata testMetadata;

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private MongoClient mongoClient;

  @MockBean
  private MongoMetadataRepository metadataRepository;

  private ACL testAcl;
  private Set<ACL> testACLSet;
  private Set<String> testTagSet;
  private ObjectMapper mapper;

  @BeforeAll
  public void setupOnce() {
    testTagSet = new HashSet<String>();
    testTagSet.add("hi");
    testTagSet.add("my");

    testAcl = new ACL("testUser", true, false, false);
    testACLSet = new HashSet<ACL>();
    testACLSet.add(testAcl);

    mapper = new ObjectMapper();
  }

  @BeforeEach
  public void setupMock() {
    testMetadata = new Metadata("/x", "the", testACLSet,
        Collections.singletonMap("message", "I am a schema"), ValidationLevel.NONE, testTagSet);

    when(metadataRepository.save(any(Metadata.class))).thenAnswer(new Answer<Metadata>() {
      public Metadata answer(InvocationOnMock invocation) throws Throwable {
        return (Metadata) invocation.getArguments()[0];
      }
    });
  }

  @Test
  public void testBadSaveRequestURI() throws Exception {
    mockMvc
        .perform(put("/metadata").content(mapper.writeValueAsString(testMetadata))
            .contentType(MediaType.APPLICATION_JSON))
        .andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  public void testPatch() throws Exception {
    Set<String> mergeTagSet = new HashSet<String>();
    mergeTagSet.add("name");
    mergeTagSet.add("is");
    mergeTagSet.addAll(testTagSet);

    Metadata expectedMetadata = new Metadata("/x", "the", testACLSet,
        Collections.singletonMap("message", "I am a schema"), ValidationLevel.NONE, mergeTagSet);

    String expectedResult = mapper.writeValueAsString(expectedMetadata);

    expectedMetadata.getTags().removeAll(testTagSet);
    expectedMetadata.setDescription("");
    when(metadataRepository.findById(any(String.class))).thenReturn(Optional.of(testMetadata));

    mockMvc
        .perform(patch("/metadata/x").content(mapper.writeValueAsString(expectedMetadata))
            .contentType(MediaType.APPLICATION_JSON))
        .andDo(print()).andExpect(status().isOk()).andExpect(content().json(expectedResult));

    Mockito.reset(metadataRepository);
  }

  @Test
  public void testPut() throws Exception {
    testMetadata = new Metadata("/x", "the", testACLSet,
        Collections.singletonMap("message", "I am a schema"), ValidationLevel.NONE, testTagSet);

    mockMvc
        .perform(put("/metadata/x").content(mapper.writeValueAsString(testMetadata))
            .contentType(MediaType.APPLICATION_JSON))
        .andDo(print()).andExpect(status().isOk())
        .andExpect(content().json(mapper.writeValueAsString(testMetadata)));
  }

  @Test
  public void testGetNotFoundException() throws Exception {
    when(metadataRepository.findById(any(String.class))).thenReturn(Optional.empty());

    mockMvc.perform(get("/metadata/x/y/z")).andExpect(status().isNotFound());

    Mockito.reset(metadataRepository);
  }


  @Test
  public void testGet() throws Exception {
    when(metadataRepository.findById(any(String.class))).thenReturn(Optional.of(testMetadata));

    mockMvc.perform(get("/metadata/x/y/z")).andExpect(status().isOk())
        .andExpect(content().json(mapper.writeValueAsString(testMetadata)));

    Mockito.reset(metadataRepository);
  }
}
