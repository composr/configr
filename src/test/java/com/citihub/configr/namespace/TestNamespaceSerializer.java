package com.citihub.configr.namespace;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import com.citihub.configr.base.UnitTest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestNamespaceSerializer extends UnitTest {

  private static final String MEDIUM_COMPLEX_JSON_SAMPLE =
      "{ \"foo\": { \"bar\": { \"baz\": [ { \"buzz\": \"bizz\" }, { \"foo2\": \"bar2\" } ] } } }";
  private static final String TRIVIAL_JSON_SAMPLE = "{ \"foo\": \"bar\", \"baz\": \"buzz\" }";
  private static final String COMPLEX_EXPECTED_RESULT =
      "{\"foo\":{\"bar\":{\"baz\":[{\"buzz\":\"bizz\"},{\"foo2\":\"bar2\"}]}}}";

  private ObjectMapper mapper;

  @BeforeAll
  public void setup() {
    mapper = new ObjectMapper();
    SimpleModule module = new SimpleModule();
    module.addSerializer(Namespace.class, new NamespaceSerializer());
    mapper.registerModule(module);
  }

  @Test
  public void testDeserializeTrivialNamespace()
      throws JsonMappingException, JsonProcessingException {
    Map<String, Object> value = mapper.readValue(TRIVIAL_JSON_SAMPLE, new HashMap<>().getClass());
    Namespace namespace = new Namespace("foo", value);
    log.info("{}", mapper.writeValueAsString(namespace));
    assertThat(mapper.writeValueAsString(namespace))
        .isEqualTo(TRIVIAL_JSON_SAMPLE.replaceAll("\\s", ""));
  }

  @Test
  public void testDeserializeComplexNamespace()
      throws JsonMappingException, JsonProcessingException {
    Map<String, Object> value =
        mapper.readValue(MEDIUM_COMPLEX_JSON_SAMPLE, new HashMap<>().getClass());
    Namespace namespace = new Namespace("foo", value);
    log.info("{}", mapper.writeValueAsString(namespace));
    log.info("...and a namespace of {}", namespace.getNamespace());
    assertThat(mapper.writeValueAsString(namespace)).isEqualTo(COMPLEX_EXPECTED_RESULT);
  }

}
