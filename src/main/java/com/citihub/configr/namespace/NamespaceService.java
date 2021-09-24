package com.citihub.configr.namespace;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import com.citihub.configr.exception.ConflictException;
import com.citihub.configr.exception.NotFoundException;
import com.citihub.configr.mongostorage.MongoConfigRepository;
import com.citihub.configr.mongostorage.MongoNamespaceQueries;
import com.citihub.configr.version.Version;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.hash.Hashing;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class NamespaceService {

  private MongoNamespaceQueries nsQueries;

  private MongoConfigRepository configRepo;

  private ObjectMapper objectMapper;

  public NamespaceService(@Autowired MongoConfigRepository configRepo,
      @Autowired MongoNamespaceQueries nsQueries, 
      @Autowired ObjectMapper objectMapper) {
    this.configRepo = configRepo;
    this.nsQueries = nsQueries;
    this.objectMapper = objectMapper;
  }

  public @NonNull Namespace fetchNamespace(String fullPath) {
    Namespace ns = findNamespaceOrThrowException(fullPath);
    log.info("using path {}, I found: {}", split(fullPath), ns);
    return ns;
  }

  public @NonNull Map<String, Object> fetchNamespaceBodyByPath(String fullPath) {
    Namespace ns = findNamespaceOrThrowException(fullPath);
    log.info("using path {}, I found: {}", split(fullPath), ns);

    if( ns.getValue() instanceof Map ) {
      return trimKeysFromResponseMap(split(fullPath), 
          (Map<String, Object>)ns.getValue());
    } else
      return Collections.singletonMap(ns.getKey(), ns.getValue());
  }

  Map<String, Object> trimKeysFromResponseMap(String [] split, Map<String, Object> result) {
    log.info("using path {}", (Object[])split);
    for( int i = 0; i < split.length-1; i++ ) {
      log.info("Removing key {}", split[i]);
      result = (Map<String, Object>)result.remove(split[i]);
    }

    result.keySet().removeIf(id -> !id.equals(split[split.length-1]));      
    return result;
  }

  public Namespace storeNamespace(Map<String, Object> json, String path, 
      boolean shouldMerge, boolean shouldReplace)
      throws JsonMappingException, JsonParseException, IOException {

    Namespace materialized = materialize(json, split(path));
    Optional<Namespace> extant = configRepo.findById(materialized.getNamespace());
    if( extant.isPresent() ) {
      return mergeReplaceSave(materialized, extant.get(), split(path), 
          shouldMerge, shouldReplace);
    } else {
      return versionAndSave(materialized, hashNamespace(materialized));
    }
  }

  private Namespace mergeReplaceSave(Namespace materialized, Namespace extant,
      String [] path, boolean shouldMergeTrees, boolean shouldReplace) throws JsonProcessingException {
    String oldHash = hashNamespace(extant);
    if( shouldMergeTrees )
      merge((Map<String, Object>)extant.getValue(), 
          (Map<String, Object>)materialized.getValue());
    else {
      if( !shouldReplace )
        if( wouldReplace((Map<String, Object>)extant.getValue(), 
          (Map<String, Object>)materialized.getValue(), path) )
          throw new ConflictException();
      
      replace((Map<String, Object>)extant.getValue(), 
          (Map<String, Object>)materialized.getValue(), path);
    }
    
    return compareAndSaveNamespace(extant, oldHash);
  }

  private Namespace compareAndSaveNamespace(Namespace extant, String oldHash) throws JsonProcessingException {
    String newHash = hashNamespace(extant);
    if(!oldHash.equals(newHash)) {
      return versionAndSave(extant, newHash);
    } else
      return extant;
  }

  private Namespace versionAndSave(Namespace ns, String newHash) {
    ns.setVersion(new Version(newHash));
    return configRepo.save(ns);
  }

  private String hashNamespace(Namespace extant) throws JsonProcessingException {
    return Hashing.sha256().hashString(
        objectMapper.writeValueAsString(extant.getValue())).toString();
  }

  String [] split(String fullPath) {
    return fullPath.substring(1).split("/");
  }

  Namespace materialize(Map<String, Object> json, String [] pathTokens) {
    Map<String, Object> curRoot = json;

    for (int i = pathTokens.length-1; i >= 0; i--) {
      log.info("Creating representation for path token {}", pathTokens[i]);
      Map<String, Object> newNodes = new HashMap<>();
      newNodes.put(pathTokens[i], curRoot);
      curRoot = newNodes;
    }

    return new Namespace(pathTokens[0], curRoot, "/" + pathTokens[0]);
  }

  Namespace findNamespaceOrThrowException(String fullPath) {
    Namespace ns = nsQueries.findByPath(fullPath);
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

  void replace(Map<String, Object> mapExtant, Map<String, Object> mapNew, String [] path) {
    if( path.length == 1 ) {//Wipe it from the root
      mapExtant.clear();
      mapExtant.putAll(mapNew);
    } else {
      Map<String, Object> curValue = mapExtant;
      for (int i = 0; i <= path.length-2; i++) {
        if( curValue != null )
          if( curValue.get(path[i]) instanceof Map )
            curValue = (Map<String, Object>)curValue.get(path[i]);
          else
            curValue = null;
        else
          break;
        log.info("Traversing with key {} with value {}", path[i], curValue);
      }

      if( curValue != null ) //I found where I'm going to do the replacement
        curValue.remove(path[path.length-1]);

      merge(mapExtant, mapNew);
    }
  }

  boolean wouldReplace(Map<String, Object> mapExtant, Map<String, Object> mapNew, String [] path) {
      Map<String, Object> curValue = mapExtant;
      for (int i = 0; i <= path.length-2; i++) {
        if( curValue == null )
          return false;
        else
          if( curValue.get(path[i]) instanceof Map )
            curValue = (Map<String, Object>)curValue.get(path[i]);
          else if( curValue.get(path[i]) == null)
            return false; //path doesn't exist, won't replace anything
          else
            return true; //would result in replacing a primitive/array
      }

      return curValue.containsKey(path[path.length-1]);
  }
}
