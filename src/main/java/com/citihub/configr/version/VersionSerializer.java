package com.citihub.configr.version;

import java.io.IOException;
import com.citihub.configr.namespace.Namespace;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor
public class VersionSerializer extends JsonSerializer<Namespace> {

  @Override
  public void serialize(Namespace namespace, JsonGenerator gen, SerializerProvider serializers)
      throws IOException {
    log.info("Writing object {}", namespace.getKey());
    gen.writeObject(namespace.getVersion());
  }

}
