package com.citihub.configr.api;

import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.HashMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import com.citihub.configr.ConfigurationStoreApplication;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@DataMongoTest
@ExtendWith(SpringExtension.class)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = ConfigurationStoreApplication.class)
public class TestConfigurationController {
  
  @Autowired
  private RestTemplate restTemplate;
  
  @Test
  public void testNotFound() {
    Exception exception = assertThrows(RestClientException.class, () -> {
      restTemplate.getForEntity("/foo/bar/baz", Object.class, new HashMap<String, Object>());
    });
    
    log.info("Exception thrown: {}", exception.getMessage());
  }
  
}
