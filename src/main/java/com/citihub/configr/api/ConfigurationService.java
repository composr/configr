package com.citihub.configr.api;

import java.io.IOException;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.citihub.configr.namespace.Namespace;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ConfigurationService {

  private ConfigurationRepository configRepo;

  public ConfigurationService(@Autowired ConfigurationRepository configRepo) {
    this.configRepo = configRepo;
  }

  public Namespace fetchNamespace(String fullPath) {
    log.info("I found: {}", configRepo.findByNamespace(fullPath).orElse(null));

    return configRepo.findByNamespace(fullPath).orElse(null);
  }

  public Namespace storeNamespace(JsonParser p, String path)
      throws JsonMappingException, JsonParseException, IOException {

    JsonNode jsonTree = materialize(path, p);
    ObjectMapper mapper = new ObjectMapper();

    Namespace rootNamespace = mapper.convertValue(jsonTree, Namespace.class);

    // always save the namespace value, not the wrapper itself
    return configRepo.save(getNamespaceFromValueUnsafe(rootNamespace));
  }

  /**
   * Can be used when the rootNamespace is built with NamespaceDeserializer. The root will contain a
   * value that is a Map<String, Namespace> with a single entry. No type checking is done in this
   * method, hence unsafe.
   * 
   * @param rootNamespace
   * @return Namespace in the map
   */
  private Namespace getNamespaceFromValueUnsafe(Namespace rootNamespace) {
    return ((Map<String, Namespace>) rootNamespace.getValue()).values().iterator().next();
  }

  private JsonNode materialize(String path, JsonParser p) throws IOException {
    String[] pathTokens = path.split("/");

    JsonNode curRoot = p.getCodec().readTree(p);

    // Intentionally ignore the root of the URI, i.e. config, version, metadata
    for (int i = pathTokens.length - 1; i > 1; i--) {
      log.info("Creating representation for path token {}", pathTokens[i]);
      ObjectNode foo = JsonNodeFactory.instance.objectNode();
      foo.set(pathTokens[i], curRoot);
      curRoot = foo;
    }

    return curRoot;
  }

}
