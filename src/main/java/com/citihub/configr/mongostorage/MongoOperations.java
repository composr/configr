package com.citihub.configr.mongostorage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import com.citihub.configr.exception.ConflictException;
import com.citihub.configr.exception.NotFoundException;
import com.citihub.configr.exception.SaveFailureException;
import com.citihub.configr.namespace.Namespace;
import com.citihub.configr.storage.MapOperations;
import com.citihub.configr.storage.StoreOperations;
import com.citihub.configr.version.Version;
import com.citihub.configr.version.VersionedNamespace;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.diff.JsonDiff;
import com.google.common.hash.Hashing;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MongoOperations implements StoreOperations {

  private static final String VERSION_COLLECTION_PREFIX = "ns_versions_";

  private MongoTemplate mongoTemplate;
  private ObjectMapper objectMapper;

  public MongoOperations(@Autowired MongoTemplate mongoTemplate,
      @Autowired ObjectMapper objectMapper) {
    this.mongoTemplate = mongoTemplate;
    this.objectMapper = objectMapper;
  }

  Optional<Namespace> findById(String id) {
    return Optional.ofNullable(mongoTemplate.findById(id, Namespace.class));
  }

  Namespace storeNamespace(Namespace ns) {
    return mongoTemplate.save(ns);
  }

  void saveAsOldVersion(Optional<Namespace> oldVersion, Namespace newVersion) {
    oldVersion.ifPresent(ns -> saveOldVersion(ns, newVersion));
  }

  void saveOldVersion(Namespace ns, Namespace newVersion) {
    try {
      JsonNode oldTree = objectMapper.readTree(objectMapper.writeValueAsString(ns));
      JsonNode newTree = objectMapper.readTree(objectMapper.writeValueAsString(newVersion));

      String patch = objectMapper.writeValueAsString(JsonDiff.asJson(newTree, oldTree));

      String collectionName =
          VERSION_COLLECTION_PREFIX + Hashing.sha256().hashString(ns.getNamespace()).toString();

      mongoTemplate.save(new VersionedNamespace(ns, patch), collectionName);
    } catch (IOException e) {
      e.printStackTrace();
      throw new SaveFailureException();
    }
  }

  @Override
  public List<Document> listVersionsByPath(String path) {
    log.info("Looking for collection named collection_{}", path);
    String collectionName =
        VERSION_COLLECTION_PREFIX + Hashing.sha256().hashString(path).toString();
    log.info("And in hash-form", collectionName);
    if (mongoTemplate.collectionExists(collectionName)) {
      MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);

      List<Document> results = new ArrayList<Document>();
      collection.find().sort(new BasicDBObject("created", 1)).limit(10).iterator()
          .forEachRemaining(vns -> results.add(vns));
      return results;
    } else
      throw new NotFoundException();
  }

  @Override
  public Namespace deleteByPath(String path) throws JsonProcessingException {
    Namespace ns = findByPath(path);
    if (ns == null)
      throw new NotFoundException();

    String[] pathSplit = path.substring(1).split("/");

    Object result = MapOperations.delete((Map<String, Object>) ns.getValue(), pathSplit);

    if (result != null) {
      log.debug("I deleted {} and am going to save {} back to path {}", result, ns, path);
      return saveNamespace(pathSplit, false, true, ns);
    } else
      throw new NotFoundException();
  }

  @Override
  public Namespace findByPath(String path) {
    String whereClause = "value" + path.replaceAll("\\/", "\\.");

    Query query = new Query();
    query.addCriteria(Criteria.where(whereClause).exists(true));
    return mongoTemplate.findOne(query, Namespace.class);
  }

  @Override
  public Namespace materialize(Map<String, Object> json, String[] pathTokens) {
    Map<String, Object> curRoot = json;

    for (int i = pathTokens.length - 1; i >= 0; i--) {
      log.info("Creating representation for path token {}", pathTokens[i]);
      Map<String, Object> newNodes = new HashMap<>();
      newNodes.put(pathTokens[i], curRoot);
      curRoot = newNodes;
    }

    return new Namespace(pathTokens[0], curRoot, "/" + pathTokens[0]);
  }

  @Override
  public Namespace saveNamespace(String[] pathSplit, boolean shouldMerge, boolean shouldReplace,
      Namespace materialized) throws JsonProcessingException {
    Optional<Namespace> extant = findById(materialized.getNamespace());
    if (extant.isPresent()) {
      Namespace existing = findById(materialized.getNamespace()).get();
      return mergeReplaceSave(materialized, extant.get(), existing, pathSplit, shouldMerge,
          shouldReplace);
    } else {
      return versionAndSave(materialized, null, hashNamespace(materialized));
    }
  }


  private Namespace mergeReplaceSave(Namespace materialized, Namespace newNS, Namespace existing,
      String[] path, boolean shouldMergeTrees, boolean shouldReplace)
      throws JsonProcessingException {
    String oldHash = hashNamespace(newNS);
    if (shouldMergeTrees)
      MapOperations.merge((Map<String, Object>) newNS.getValue(),
          (Map<String, Object>) materialized.getValue());
    else {
      if (!shouldReplace)
        if (MapOperations.wouldReplace((Map<String, Object>) newNS.getValue(),
            (Map<String, Object>) materialized.getValue(), path))
          throw new ConflictException();

      MapOperations.replace((Map<String, Object>) newNS.getValue(),
          (Map<String, Object>) materialized.getValue(), path);
    }

    return compareAndSaveNamespace(newNS, existing, oldHash);
  }

  private Namespace compareAndSaveNamespace(Namespace newNS, Namespace existing, String oldHash)
      throws JsonProcessingException {
    String newHash = hashNamespace(newNS);
    if (!oldHash.equals(newHash)) {
      return versionAndSave(newNS, existing, newHash);
    } else
      return newNS;
  }

  Namespace versionAndSave(Namespace ns, Namespace oldVersion, String newHash) {
    saveAsOldVersion(Optional.ofNullable(oldVersion), ns);
    ns.setVersion(new Version(newHash, "Willy Wonka"));
    return storeNamespace(ns);
  }

  private String hashNamespace(Namespace extant) throws JsonProcessingException {
    return Hashing.sha256().hashString(objectMapper.writeValueAsString(extant.getValue()))
        .toString();
  }



}
