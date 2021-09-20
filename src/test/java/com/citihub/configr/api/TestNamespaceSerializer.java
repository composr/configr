package com.citihub.configr.api;

import static org.assertj.core.api.Assertions.assertThat;
import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import com.citihub.configr.mongostorage.MongoNamespaceDeserializer;
import com.citihub.configr.mongostorage.MongoNamespaceSerializer;
import com.citihub.configr.namespace.Namespace;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@TestInstance(Lifecycle.PER_CLASS)
public class TestNamespaceSerializer {

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
    module.addSerializer(Namespace.class, new MongoNamespaceSerializer());
    module.addDeserializer(Namespace.class, new MongoNamespaceDeserializer());
    mapper.registerModule(module);
  }

  @Test
  public void testDeserializeTrivialNamespace()
      throws JsonMappingException, JsonProcessingException {
    Namespace namespace = mapper.readValue(TRIVIAL_JSON_SAMPLE, Namespace.class);
    log.info("{}", mapper.writeValueAsString(namespace));
    assertThat(mapper.writeValueAsString(namespace))
        .isEqualTo(TRIVIAL_JSON_SAMPLE.replaceAll("\\s", ""));
  }

  @Test
  public void testDeserializeComplexNamespace()
      throws JsonMappingException, JsonProcessingException {
    Namespace namespace = mapper.readValue(MEDIUM_COMPLEX_JSON_SAMPLE, Namespace.class);
    log.info("{}", mapper.writeValueAsString(namespace));
    log.info("...and a namespace of {}", namespace.getNamespace());
    assertThat(mapper.writeValueAsString(namespace)).isEqualTo(COMPLEX_EXPECTED_RESULT);
  }

  @Test
  public void testDeserializeArrayNamespace() throws JsonMappingException, JsonProcessingException {
    Namespace namespace = mapper.readValue(
        "[ \"foo\", 1, null, true, 1.5, " + Integer.MAX_VALUE + 2 + " ]", Namespace.class);
    log.info("{}", mapper.writeValueAsString(namespace));
    log.info("...and a namespace of {}", namespace.getNamespace());
    assertThat(namespace.getKey()).isNullOrEmpty();
    assertThat(namespace.getValue())
        .isEqualTo(Arrays.asList(new Object[] {"foo", 1, null, true, 1.5, 21474836472L}));
  }


  @Test
  public void testSerializeNamespace() throws JsonMappingException, JsonProcessingException {}

}
