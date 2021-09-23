package com.citihub.configr.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.citihub.configr.exception.NotFoundException;
import com.citihub.configr.mongostorage.MongoConfigRepository;
import com.citihub.configr.mongostorage.MongoNamespaceQueries;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ExtendWith(MockitoExtension.class)
@TestInstance(Lifecycle.PER_CLASS)
public class TestConfigurationService {

  private final String TEST_JSON = "{\"x\":{\"z\":{\"y\":{\"a\":{\"f\":{\"boo\":{\"foo\":[\"ballz\",\"bazz\"]},\"ba\":{\"nee\":\"nah\"}}}}},\"y\":{\"a\":{\"f\":{\"boo\":{\"fooz\":[\"ball\",\"bazz\"]},\"ba\":{\"nee\":\"nah\"}}}}}}";
  
  private final String MERGE_LEFT_SAMPLE = "{\"x\":{\"z\":{\"y\":{\"a\":{\"f\":{\"boo\":{\"foo\":[\"ballz\",\"bazz\"]},\"ba\":{\"nee\":\"nah\"}}}}}}}";
  private final String MERGE_RIGHT_SAMPLE = "{\"x\":{\"y\":{\"a\":{\"f\":{\"boo\":{\"fooz\":[\"ball\",\"bazz\"]},\"ba\":{\"nee\":\"nah\"}}}}}}";
  
  
  @Mock
  private MongoConfigRepository configRepo;

  @Mock
  private MongoNamespaceQueries nsQueries;
  
  @InjectMocks
  private ConfigurationService configService;

  @Test
  public void testTrimPath() {
    assertThat(configService.trimPath("/configuration/x/y/z")).isEqualTo("/x/y/z");
  }
  
  @Test
  public void testfindNamespaceOrThrowException() {
    assertThrows(NotFoundException.class, () -> 
        configService.findNamespaceOrThrowException("/x/y/z"));
  }
  
  @Test
  public void testMerge() throws Exception {
   ObjectMapper mapper = new ObjectMapper();
   Map<String, Object> left = mapper.readValue(MERGE_LEFT_SAMPLE, new HashMap<>().getClass());
   Map<String, Object> right = mapper.readValue(MERGE_RIGHT_SAMPLE, new HashMap<>().getClass());
   
   configService.merge(left, right);
   assertThat(mapper.writeValueAsString(left)).isEqualTo(TEST_JSON);
  }
  
  @Test
  public void testTrimPathFromResponseDeep() throws Exception {
    Map<String, Object> jsonMap = new ObjectMapper().readValue(TEST_JSON, 
        new HashMap<String, Object>().getClass());
    
    Map<String, Object> result = configService.trimPathFromResponse("/configuration/x/y/a/f/boo/fooz", jsonMap);
    
    assertThat(result.keySet().size() == 1 );
    assertThat(result.keySet().iterator().next().equals("fooz"));
    assertThat(result.get("fooz")).isEqualTo(Arrays.asList(new String[] {"ball", "bazz"}));
  }
  
  @Test
  public void testTrimPathFromResponse() throws Exception {
    Map<String, Object> jsonMap = new ObjectMapper().readValue(TEST_JSON, 
        new HashMap<String, Object>().getClass());
    
    Map<String, Object> result = configService.trimPathFromResponse("/configuration/x/y", jsonMap);
    log.info(new ObjectMapper().writeValueAsString(result));
    assertThat(result.keySet().size()).isEqualTo(1);
    assertThat(result.keySet().iterator().next().equals("y"));
  }

}
