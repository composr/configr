package com.citihub.configr.namespace;

import java.io.IOException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.google.common.base.Strings;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class NamespaceSerializer extends JsonSerializer<Namespace> {

  @Override
  public void serialize(Namespace namespace, JsonGenerator gen, SerializerProvider serializers)
      throws IOException {

    if (Strings.isNullOrEmpty(namespace.getKey()))
      writeArray(namespace, gen);
    else
      writeObject(namespace, gen);
  }

  private void writeObject(Namespace namespace, JsonGenerator gen) throws IOException {
    gen.writeStartObject();
    gen.writeFieldName(namespace.getKey());
    gen.writeObject(namespace.getValue());
    gen.writeEndObject();
  }

  private void writeArray(Namespace namespace, JsonGenerator gen) throws IOException {
    gen.writeStartArray();
    gen.writeObject(namespace.getValue());
    gen.writeEndArray();
  }

}
