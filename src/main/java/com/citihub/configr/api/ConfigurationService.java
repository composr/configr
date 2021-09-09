package com.citihub.configr.api;

import java.io.IOException;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.stereotype.Service;
import com.citihub.configr.namespace.MongoNamespaceQueries;
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

  private MongoNamespaceQueries nsQueries;
  
  private ConfigurationRepository configRepo;

  private final int LEADING_SLASH_IDX = 1;
  
  public ConfigurationService(@Autowired ConfigurationRepository configRepo,
      @Autowired MongoNamespaceQueries nsQueries) {
    this.configRepo = configRepo;
    this.nsQueries = nsQueries;
  }

  public Namespace fetchNamespace(String fullPath) {
    Namespace ns = nsQueries.findByPath(trimPath(fullPath));
    log.info("using path {}, I found: {}", trimPath(fullPath), ns);
    return ns;
  }

  public Namespace storeNamespace(JsonParser p, String path)
      throws JsonMappingException, JsonParseException, IOException {

    JsonNode jsonTree = materialize(trimPath(path), p);
    ObjectMapper mapper = new ObjectMapper();

    Namespace rootNamespace = mapper.convertValue(jsonTree, Namespace.class);

    // always save the namespace value, not the wrapper itself
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
