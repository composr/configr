package com.citihub.configr.api;

import static org.assertj.core.api.Assertions.assertThat;
import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.citihub.configr.namespace.Namespace;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestNamespaceSerializer {

  // @Autowired
  private ObjectMapper mapper = new ObjectMapper();

  @Test
  public void testDeserializeTrivialNamespace()
      throws JsonMappingException, JsonProcessingException {
    Namespace namespace = mapper.readValue("{ \"foo\": \"bar\" }", Namespace.class);
    assertThat(namespace.getKey()).isEqualTo("foo");
    assertThat(namespace.getValue()).isEqualTo("bar");
  }

  @Test
  public void testDeserializeComplexNamespace()
      throws JsonMappingException, JsonProcessingException {
    Namespace namespace = mapper.readValue(
        "{ \"foo\": { \"bar\": { \"baz\": [ { \"buzz\": \"bizz\" }, { \"foo2\": \"bar2\" } ] } } }",
        Namespace.class);
    log.info("{}", mapper.writeValueAsString(namespace));
    assertThat(namespace.getKey()).isEqualTo("foo");
    assertThat(namespace.getValue()).isEqualTo(new Namespace("bar", new Namespace("baz", Arrays
        .asList(new Namespace[] {new Namespace("buzz", "bizz"), new Namespace("foo2", "bar2")}))));
  }

  @Test
  public void testDeserializeArrayNamespace() throws JsonMappingException, JsonProcessingException {
    Namespace namespace = mapper.readValue(
        "[ \"foo\", 1, null, true, 1.5, " + Integer.MAX_VALUE + 2 + " ]", Namespace.class);
    log.info("{}", mapper.writeValueAsString(namespace));
    assertThat(namespace.getKey()).isNullOrEmpty();
    assertThat(namespace.getValue())
        .isEqualTo(Arrays.asList(new Object[] {"foo", 1, null, true, 1.5, 21474836472L}));
  }


  @Test
  public void testSerializeNamespace() throws JsonMappingException, JsonProcessingException {}

}
