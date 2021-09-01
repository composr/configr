package com.citihub.configr.api;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class TestConfigurationStoreApplication {


  @Autowired
  private ConfigurationController configurationController;

  @Test
  public void contextLoads() throws Exception {
    assertThat(configurationController).isNotNull();
  }
}
