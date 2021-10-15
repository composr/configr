package com.citihub.configr.namespace;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import com.citihub.configr.base.UnitTest;
import com.citihub.configr.exception.NotFoundException;
import com.citihub.configr.mongostorage.MongoConfigRepository;
import com.citihub.configr.mongostorage.MongoOperations;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestConfigurationService extends UnitTest {

  private final String TEST_JSON =
      "{\"x\":{\"z\":{\"y\":{\"a\":{\"f\":{\"boo\":{\"foo\":[\"ballz\",\"bazz\"]},\"ba\":{\"nee\":\"nah\"}}}}},\"y\":{\"a\":{\"f\":{\"boo\":{\"fooz\":[\"ball\",\"bazz\"]},\"ba\":{\"nee\":\"nah\"}}}}}}";

  private final String MERGE_LEFT_SAMPLE =
      "{\"x\":{\"z\":{\"y\":{\"a\":{\"f\":{\"boo\":{\"foo\":[\"ballz\",\"bazz\"]},\"ba\":{\"nee\":\"nah\"}}}}}}}";
  private final String MERGE_RIGHT_SAMPLE =
      "{\"x\":{\"y\":{\"a\":{\"f\":{\"boo\":{\"fooz\":[\"ball\",\"bazz\"]},\"ba\":{\"nee\":\"nah\"}}}}}}";

  private final String REPLACE_ROOT_SAMPLE =
      "{\"x\":{\"z\":{\"a\":{\"f\":{\"boo\":{\"fooz\":[\"ball\",\"bazz\"]},\"ba\":{\"nee\":\"nah\"}}}}}}";
  private final String REPLACE_SMALL_SAMPLE = "{\"x\":{\"a\":{\"f\":{\"boo\":\"fooz\"}}}}";

  private final String REPLACE_PRIMITIVE_SAMPLE =
      "{\"x\":{\"a\":{\"f\":{\"boo\":{\"fooz\":{\"foo\": [\"ball\",\"bazz\"]}}}}}}";
  private final String REPLACE_PRIMITIVE_RESULT =
      "{\"x\":{\"a\":{\"f\":{\"boo\":{\"fooz\":{\"foo\":[\"ball\",\"bazz\"]}}}}}}";

  private final String REPLACE_NULL_OBJECT_RIGHT =
      "{\"x\":{\"z\":{\"heythere\":{\"toast\":{\"boo\":\"fooz\"}}}}}";
  private final String REPLACE_NULL_OBJECT_RESULT =
      "{\"x\":{\"a\":{\"f\":{\"boo\":\"fooz\"}},\"z\":{\"heythere\":{\"toast\":{\"boo\":\"fooz\"}}}}}";


  @Mock
  private MongoConfigRepository configRepo;

  @Mock
  private MongoOperations nsQueries;

  @InjectMocks
  private NamespaceService configService;

  @Test
  public void testfindNamespaceOrThrowException() {
    assertThrows(NotFoundException.class,
        () -> configService.findNamespaceOrThrowException("/x/y/z"));
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
    Map<String, Object> jsonMap =
        new ObjectMapper().readValue(TEST_JSON, new HashMap<String, Object>().getClass());

    Map<String, Object> result = configService
        .trimKeysFromResponseMap(new String[] {"x", "y", "a", "f", "boo", "fooz"}, jsonMap);

    assertThat(result.keySet().size() == 1);
    assertThat(result.keySet().iterator().next().equals("fooz"));
    assertThat(result.get("fooz")).isEqualTo(Arrays.asList(new String[] {"ball", "bazz"}));
  }

  @Test
  public void testTrimPathFromResponse() throws Exception {
    Map<String, Object> jsonMap =
        new ObjectMapper().readValue(TEST_JSON, new HashMap<String, Object>().getClass());

    Map<String, Object> result =
        configService.trimKeysFromResponseMap(new String[] {"x", "y"}, jsonMap);
    log.info(new ObjectMapper().writeValueAsString(result));
    assertThat(result.keySet().size()).isEqualTo(1);
    assertThat(result.keySet().iterator().next().equals("y"));
  }

  @Test
  public void testShouldReplaceRoot() throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    Map<String, Object> left = mapper.readValue(MERGE_LEFT_SAMPLE, new HashMap<>().getClass());
    Map<String, Object> right = mapper.readValue(REPLACE_ROOT_SAMPLE, new HashMap<>().getClass());

    configService.replace(left, right, new String[] {"x"});
    assertThat(mapper.writeValueAsString(left)).isEqualTo(REPLACE_ROOT_SAMPLE);
  }

  @Test
  public void testShouldReplaceObject() throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    Map<String, Object> left = mapper.readValue(MERGE_LEFT_SAMPLE, new HashMap<>().getClass());
    Map<String, Object> right = mapper.readValue(REPLACE_ROOT_SAMPLE, new HashMap<>().getClass());

    configService.replace(left, right, new String[] {"x", "z", "y"});
    assertThat(mapper.writeValueAsString(left)).isEqualTo(REPLACE_ROOT_SAMPLE);
  }

  @Test
  public void testShouldInsertBranch() throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    Map<String, Object> left = mapper.readValue(REPLACE_SMALL_SAMPLE, new HashMap<>().getClass());
    Map<String, Object> right =
        mapper.readValue(REPLACE_NULL_OBJECT_RIGHT, new HashMap<>().getClass());

    configService.replace(left, right, new String[] {"x", "z", "heythere", "toast"});
    log.info("ladi da {}", mapper.writeValueAsString(left));
    assertThat(mapper.writeValueAsString(left)).isEqualTo(REPLACE_NULL_OBJECT_RESULT);
  }

  @Test
  public void testShouldReplacePrimitive() throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    Map<String, Object> left = mapper.readValue(REPLACE_SMALL_SAMPLE, new HashMap<>().getClass());
    Map<String, Object> right =
        mapper.readValue(REPLACE_PRIMITIVE_SAMPLE, new HashMap<>().getClass());

    configService.replace(left, right, new String[] {"x", "a", "f", "boo", "fooz"});
    assertThat(mapper.writeValueAsString(left)).isEqualTo(REPLACE_PRIMITIVE_RESULT);
  }

  @Test
  public void testWouldReplacePrimitive() throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    Map<String, Object> left = mapper.readValue(REPLACE_SMALL_SAMPLE, new HashMap<>().getClass());
    Map<String, Object> right =
        mapper.readValue(REPLACE_PRIMITIVE_SAMPLE, new HashMap<>().getClass());

    boolean result =
        configService.wouldReplace(left, right, new String[] {"x", "a", "f", "boo", "fooz"});
    assertThat(result).isTrue();
  }

  @Test
  public void testWouldReplaceObject() throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    Map<String, Object> left = mapper.readValue(MERGE_LEFT_SAMPLE, new HashMap<>().getClass());
    Map<String, Object> right = mapper.readValue(REPLACE_ROOT_SAMPLE, new HashMap<>().getClass());

    boolean result = configService.wouldReplace(left, right, new String[] {"x", "z", "y"});
    assertThat(result).isTrue();
  }

  @Test
  public void testWouldReplaceRoot() throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    Map<String, Object> left = mapper.readValue(MERGE_LEFT_SAMPLE, new HashMap<>().getClass());
    Map<String, Object> right = mapper.readValue(REPLACE_ROOT_SAMPLE, new HashMap<>().getClass());

    boolean result = configService.wouldReplace(left, right, new String[] {"x"});
    assertThat(result).isTrue();
  }

  @Test
  public void testWouldNotReplace() throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    Map<String, Object> left = mapper.readValue(REPLACE_SMALL_SAMPLE, new HashMap<>().getClass());
    Map<String, Object> right =
        mapper.readValue(REPLACE_NULL_OBJECT_RIGHT, new HashMap<>().getClass());

    boolean result =
        configService.wouldReplace(left, right, new String[] {"x", "z", "heythere", "toast"});
    assertThat(result).isFalse();
  }
}
