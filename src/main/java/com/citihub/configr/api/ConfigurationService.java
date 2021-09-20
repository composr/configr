package com.citihub.configr.api;

import java.io.IOException;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.citihub.configr.mongostorage.MongoConfigRepository;
import com.citihub.configr.mongostorage.MongoNamespaceQueries;
import com.citihub.configr.namespace.Namespace;
import com.citihub.configr.version.Version;
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

  private MongoNamespaceQueries nsQueries;

  private MongoConfigRepository configRepo;

  private ObjectMapper mongoObjectMapper;

  private final int LEADING_SLASH_IDX = 1;

  public ConfigurationService(@Autowired MongoConfigRepository configRepo,
      @Autowired MongoNamespaceQueries nsQueries, 
      @Autowired ObjectMapper mongoObjectMapper) {
    this.configRepo = configRepo;
    this.nsQueries = nsQueries;
    this.mongoObjectMapper = mongoObjectMapper;
  }

  public Namespace fetchNamespace(String fullPath) {
    Namespace ns = nsQueries.findByPath(trimPath(fullPath));
    log.info("using path {}, I found: {}", trimPath(fullPath), ns);
    return ns;
  }

  public Namespace storeNamespace(JsonParser p, String path)
      throws JsonMappingException, JsonParseException, IOException {

    JsonNode jsonTree = materialize(trimPath(path), p);

    Namespace rootNamespace = mongoObjectMapper.convertValue(jsonTree, Namespace.class);

    // always save the namespace value, not the wrapper itself
    if (rootNamespace.getVersion() == null)
      rootNamespace.setVersion(new Version(""));
    return configRepo.save(getNamespaceFromValueUnsafe(rootNamespace));
  }

  private JsonNode materialize(String path, JsonParser p) throws IOException {
    String[] pathTokens = path.split("/");

    JsonNode curRoot = p.getCodec().readTree(p);

    // Intentionally ignore the root of the URI, i.e. config, version, metadata
    for (int i = pathTokens.length - 1; i > 0; i--) {
      log.info("Creating representation for path token {}", pathTokens[i]);
      ObjectNode foo = JsonNodeFactory.instance.objectNode();
      foo.set(pathTokens[i], curRoot);
      curRoot = foo;
    }

    return curRoot;
  }

  /**
   * Can be used when the rootNamespace is built with NamespaceDeserializer. The root will contain a
   * value that is a Map<String, Namespace> with a single entry. No type checking is done in this
   * method, hence unsafe.
   * 
   * @param rootNamespace
   * @return Namespace in the map
   */
  Namespace getNamespaceFromValueUnsafe(Namespace rootNamespace) {
    return ((Map<String, Namespace>) rootNamespace.getValue()).values().iterator().next();
  }

  String trimPath(String fullPath) {
    return fullPath.substring(fullPath.indexOf('/', LEADING_SLASH_IDX));
  }
}
