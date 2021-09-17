package com.citihub.configr.mongostorage;

import java.io.IOException;
import com.citihub.configr.namespace.Namespace;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor
public class MongoNamespaceSerializer extends JsonSerializer<Namespace> {

  @Override
  public void serialize(Namespace namespace, JsonGenerator gen, SerializerProvider serializers)
      throws IOException {
    log.info("Writing object {}", namespace.getKey());
    if (namespace.getValue() instanceof Namespace) {
      log.info("Found a namespace {}", namespace.getKey());
      writeObjectValue(namespace.getVersion(), gen);
      writeObjectValue(namespace.getValue(), gen);
    } else {
      log.info("NaNS, writing {}", namespace.getValue());
      gen.writeObject(namespace.getValue());
    }
  }

  private void writeObjectValue(Object value, JsonGenerator gen) throws IOException {
    gen.writeObject(value);
  }

}
