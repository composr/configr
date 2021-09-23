package com.citihub.configr.api;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import com.citihub.configr.exception.NotFoundException;
import com.citihub.configr.mongostorage.MongoConfigRepository;
import com.citihub.configr.mongostorage.MongoNamespaceQueries;
import com.citihub.configr.namespace.Namespace;
import com.citihub.configr.version.Version;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.hash.Hashing;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ConfigurationService {

  private MongoNamespaceQueries nsQueries;

  private MongoConfigRepository configRepo;

  private ObjectMapper objectMapper;

  private final int LEADING_SLASH_IDX = 1;

  public ConfigurationService(@Autowired MongoConfigRepository configRepo,
      @Autowired MongoNamespaceQueries nsQueries, 
      @Autowired ObjectMapper objectMapper) {
    this.configRepo = configRepo;
    this.nsQueries = nsQueries;
    this.objectMapper = objectMapper;
  }

  public @NonNull Namespace fetchNamespace(String fullPath) {
    Namespace ns = findNamespaceOrThrowException(fullPath);
    log.info("using path {}, I found: {}", trimPath(fullPath), ns);
    return ns;
  }

  public @NonNull Map<String, Object> fetchNamespaceBodyByPath(String fullPath) {
    Namespace ns = findNamespaceOrThrowException(fullPath);
    log.info("using path {}, I found: {}", trimPath(fullPath), ns);
    
    if( ns.getValue() instanceof Map ) {
      return trimPathFromResponse(fullPath, (Map<String, Object>)ns.getValue());
    } else
      return Collections.singletonMap(ns.getKey(), ns.getValue());
  }

  Map<String, Object> trimPathFromResponse(String fullPath, Map<String, Object> result) {
    log.info("using path {}", trimPath(fullPath));
    String [] split = trimPath(fullPath).split("/");
    for( int i = 1; i < split.length-1; i++ ) {
      log.info("Removing key {}", split[i]);
      result = (Map<String, Object>)result.remove(split[i]);
    }

    result.keySet().removeIf(id -> !id.equals(split[split.length-1]));      
    return result;
  }
  
  public Namespace storeNamespace(Map<String, Object> json, String path, boolean mergeTrees)
      throws JsonMappingException, JsonParseException, IOException {

    Namespace materialized = materialize(json, trimPath(path));
    Optional<Namespace> extant = configRepo.findById(materialized.getNamespace());
    if( extant.isPresent() ) {

      if( mergeTrees )
        merge((Map<String, Object>)extant.get().getValue(), 
            (Map<String, Object>)materialized.getValue());

      String newHash = Hashing.sha256().hashString(
          objectMapper.writeValueAsString(extant.get().getValue())).toString();
      if( !extant.get().getVersion().getId().equals(newHash)) {
        extant.get().setVersion(new Version(newHash));
        log.info("Going to save {} to mongo", objectMapper.writeValueAsString(extant.get()));
        return configRepo.save(extant.get());
      } else
        return extant.get();
    } else {
      String newHash = Hashing.sha256().hashString(
          objectMapper.writeValueAsString(materialized.getValue())).toString();
      materialized.setVersion(new Version(
          Hashing.sha256().hashString(newHash).toString()));
      return configRepo.save(materialized);
    }
  }

  Namespace materialize(Map<String, Object> json, String path) {
    String[] pathTokens = path.split("/");

    Map<String, Object> curRoot = json;

    // Intentionally ignore the root of the URI, i.e. config, version, metadata
    for (int i = pathTokens.length - 1; i > 0; i--) {
      log.info("Creating representation for path token {}", pathTokens[i]);
      Map<String, Object> newNodes = new HashMap<>();
      newNodes.put(pathTokens[i], curRoot);
      curRoot = newNodes;
    }

    return new Namespace(pathTokens[1], curRoot, "/" + pathTokens[1]);
  }

  String trimPath(String fullPath) {
    return fullPath.substring(fullPath.indexOf('/', LEADING_SLASH_IDX));
  }

  Namespace findNamespaceOrThrowException(String fullPath) {
    Namespace ns = nsQueries.findByPath(trimPath(fullPath));
    if( ns == null )
      throw new NotFoundException();
    return ns;
  }
  
  void merge(Map<String, Object> mapExtant, Map<String, Object> mapNew) {
    for (String key : mapNew.keySet()) {
      if (mapExtant.containsKey(key) && mapExtant.get(key) instanceof Map) {
        merge((Map<String, Object>)mapExtant.get(key), 
              (Map<String, Object>)mapNew.get(key));
      } else
        mapExtant.put(key, mapNew.get(key));
    }
  }

}
