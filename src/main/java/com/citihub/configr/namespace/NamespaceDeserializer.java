package com.citihub.configr.namespace;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NamespaceDeserializer extends StdDeserializer<Namespace> {

  public NamespaceDeserializer() {
    super((Class<?>) null);
  }

  @Override
  public Namespace deserialize(JsonParser p, DeserializationContext ctxt)
      throws IOException, JsonProcessingException {

    JsonNode node = p.getCodec().readTree(p);

    if (node.isArray()) {
      return new Namespace(null, this.processArrayValue(p, node));
    } else if (node.isObject()) {
      Entry<String, JsonNode> entry = node.fields().next();
      log.error("What's up - i've got {} and {}", entry.getKey(), entry.getValue().asText());

      return new Namespace(entry.getKey(), handleValue(p, entry.getValue()));
    } else
      return null;
  }

  private Object handleValue(JsonParser p, JsonNode node) throws JsonProcessingException {
    if (node.isTextual()) {
      return node.asText();
    } else if (node.isArray()) {
      return processArrayValue(p, node);
    } else if (node.isObject()) {
      return p.getCodec().treeToValue(node, Namespace.class);
    } else
      return handleJsonValue(node);
  }

  private Object handleJsonValue(JsonNode node) {
    if (node.isBoolean())
      return node.asBoolean();
    if (node.isDouble() || node.isFloat())
      return node.asDouble();
    if (node.isInt())
      return node.asInt();
    if (node.isLong())
      return node.asLong();
    if (node.isNull())
      return null;

    return node.toString();
  }

  private Object processArrayValue(JsonParser p, JsonNode node) throws JsonProcessingException {
    List<Object> arr = new ArrayList<Object>();
    Iterator<JsonNode> iter = node.iterator();
    while (iter.hasNext()) {
      arr.add(handleValue(p, iter.next()));
    }
    return arr;
  }
}
