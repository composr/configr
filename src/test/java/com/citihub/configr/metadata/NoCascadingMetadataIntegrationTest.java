package com.citihub.configr.metadata;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import com.citihub.configr.TestMongoConfiguration;
import com.citihub.configr.metadata.Metadata.ValidationLevel;
import com.citihub.configr.mongostorage.MongoMetadataRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import com.mongodb.client.MongoClient;

@SpringBootTest({"authentication.enabled=false", "authorization.enabled=false",
    "authorization.disableAggregation=true"})
@WithMockUser(username = "bobbo")
@Import(TestMongoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
@TestInstance(Lifecycle.PER_CLASS)
@Tag("integration")
@ActiveProfiles("test")
public class NoCascadingMetadataIntegrationTest {

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
  public void testDisabledACLsUnsetMetadata() throws Exception {
    when(metadataRepository.findById(any(String.class))).thenReturn(Optional.empty());
    when(metadataRepository.findById(eq("/x"))).thenReturn(Optional.of(testMetadata));

    mockMvc.perform(get("/metadata/x/y/z")).andExpect(status().isNotFound());

    Mockito.reset(metadataRepository);
  }

  @Test
  public void testDisabledCascadingACLsFromParent() throws Exception {
    Metadata rootMetadata =
        new Metadata("/x/y/z", "Foo bar", Sets.newHashSet(new ACL("admin", true, true, true)), null,
            ValidationLevel.NONE, Sets.newHashSet("Foo", "bar", "baz"));

    when(metadataRepository.findById(eq("/x/y/z"))).thenReturn(Optional.of(rootMetadata));
    when(metadataRepository.findById(eq("/x"))).thenReturn(Optional.of(testMetadata));

    mockMvc.perform(get("/metadata/x/y/z")).andExpect(status().isOk())
        .andExpect(content().json(mapper.writeValueAsString(rootMetadata)));

    Mockito.reset(metadataRepository);
  }
}
