package com.citihub.configr.api;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.mockito.Mock;
import com.citihub.configr.mongostorage.MongoConfigRepository;
import com.citihub.configr.mongostorage.MongoNamespaceQueries;
import com.fasterxml.jackson.databind.ObjectMapper;

@TestInstance(Lifecycle.PER_CLASS)
public class TestConfigurationService {

  @Mock
  private MongoConfigRepository configRepo;

  @Mock
  private MongoNamespaceQueries nsQueries;

  private ConfigurationService configService;

  @BeforeAll
  public void setup() {
    configService = new ConfigurationService(configRepo, nsQueries, new ObjectMapper());
  }

  @Test
  public void testTrimPath() {
    assertThat(configService.trimPath("/configuration/x/y/z")).isEqualTo("/x/y/z");
  }

}
