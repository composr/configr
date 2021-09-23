package com.citihub.configr.mongostorage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import com.citihub.configr.namespace.Namespace;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MongoNamespaceDeserializer extends JsonDeserializer<Namespace> {

  public MongoNamespaceDeserializer() {
    super();
  }

  @Override
  public Namespace deserialize(JsonParser p, DeserializationContext ctxt)
      throws IOException, JsonProcessingException {
    JsonNode node = p.getCodec().readTree(p);
    
    Entry<String, JsonNode> entry = node.fields().next();

    Namespace ns = new Namespace(entry.getKey(), null, "/" + entry.getKey());

    return ns;
  }

  private Object deserializeNode(JsonNode node, Namespace parent) throws JsonProcessingException {
    if (node.isArray()) {
      return this.processArrayValue(node, parent);
    } else if (node.isObject()) {
      return this.processObjectValue(node, parent);
    } else
      return null;
  }

  private Object handleValue(JsonNode node, Namespace parent) throws JsonProcessingException {
    if (node.isTextual()) {
      return node.asText();
    } else if (node.isArray()) {
      return processArrayValue(node, parent);
    } else if (node.isObject()) {
      return deserializeNode(node, parent);
    } else {
      return handleJsonValue(node);
    }
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

  private Object processObjectValue(JsonNode node, Namespace parent)
      throws JsonProcessingException {
    Iterator<Entry<String, JsonNode>> fields = node.fields();
    Map<String, Namespace> value = new HashMap<>();

    while (fields.hasNext()) {
      Entry<String, JsonNode> entry = fields.next();

      String strNamespace = parent.getNamespace() + "/" + entry.getKey();

      log.error("Obj found! i've got parent {}, k:v {}:{} and a ns of {}", parent.getKey(),
          entry.getKey(), entry.getValue().asText(), strNamespace);

      Namespace currentNS = new Namespace(entry.getKey(), null, strNamespace);
      currentNS.setValue(handleValue(entry.getValue(), currentNS));
      value.put(entry.getKey(), currentNS);
    }
    return value;
  }

  private Object processArrayValue(JsonNode node, Namespace parent) throws JsonProcessingException {
    List<Object> arr = new ArrayList<Object>();
    Iterator<JsonNode> iter = node.iterator();
    while (iter.hasNext()) {
      JsonNode next = iter.next();

      log.error("Arr found! i've got parent {} and v {}", parent.getKey(), next.toPrettyString());

      arr.add(handleValue(next, parent));
    }
    return arr;
  }
}
