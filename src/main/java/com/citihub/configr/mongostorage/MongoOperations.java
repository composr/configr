package com.citihub.configr.mongostorage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import com.citihub.configr.exception.NotFoundException;
import com.citihub.configr.exception.SaveFailureException;
import com.citihub.configr.namespace.Namespace;
import com.citihub.configr.version.VersionedNamespace;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.diff.JsonDiff;
import com.google.common.hash.Hashing;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class MongoOperations {

  private static final String VERSION_COLLECTION_PREFIX = "ns_versions_";

  private MongoTemplate mongoTemplate;
  private ObjectMapper objectMapper;

  public MongoOperations(@Autowired MongoTemplate mongoTemplate,
      @Autowired ObjectMapper objectMapper) {
    this.mongoTemplate = mongoTemplate;
    this.objectMapper = objectMapper;
  }

  public void saveAsOldVersion(Optional<Namespace> oldVersion, Namespace newVersion) {
    oldVersion.ifPresent(ns -> saveOldVersion(ns, newVersion));
  }

  void saveOldVersion(Namespace ns, Namespace newVersion) {
    try {
      JsonNode oldTree = objectMapper.readTree(objectMapper.writeValueAsString(ns));
      JsonNode newTree = objectMapper.readTree(objectMapper.writeValueAsString(newVersion));

      String patch = objectMapper.writeValueAsString(JsonDiff.asJson(oldTree, newTree));

      String collectionName =
          VERSION_COLLECTION_PREFIX + Hashing.sha256().hashString(ns.getNamespace()).toString();

      mongoTemplate.save(new VersionedNamespace(ns, patch), collectionName);
    } catch (IOException e) {
      e.printStackTrace();
      throw new SaveFailureException();
    }
  }

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

  public Namespace findByPath(String path) {
    String whereClause = "value" + path.replaceAll("\\/", "\\.");

    Query query = new Query();
    query.addCriteria(Criteria.where(whereClause).exists(true));
    return mongoTemplate.findOne(query, Namespace.class);
  }
}
