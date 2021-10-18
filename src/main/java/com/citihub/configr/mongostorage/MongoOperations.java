package com.citihub.configr.mongostorage;

import java.io.IOException;
import java.util.Optional;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.TextIndexDefinition;
import org.springframework.data.mongodb.core.index.TextIndexDefinition.TextIndexDefinitionBuilder;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import com.citihub.configr.exception.SaveFailureException;
import com.citihub.configr.namespace.Namespace;
import com.citihub.configr.version.VersionedNamespace;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.diff.JsonDiff;
import com.google.common.hash.Hashing;
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

  @PostConstruct
  public void postConstruct() {
    log.info("Ensuring indexes are set.");

    ensureIndexes(mongoTemplate);
  }

  private void ensureIndexes(MongoTemplate mongoTemplate) {
    TextIndexDefinition textIndex =
        new TextIndexDefinitionBuilder().onField("namespace", 1f).build();

    mongoTemplate.indexOps(Namespace.class).ensureIndex(textIndex);
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

  public Namespace listVersionsByPath(String path) {
    String whereClause = "value" + path.replaceAll("\\/", "\\.");

    Query query = new Query();
    query.addCriteria(Criteria.where(whereClause).exists(true));
    return mongoTemplate.findOne(query, Namespace.class);
  }

  public Namespace findByPath(String path) {
    String whereClause = "value" + path.replaceAll("\\/", "\\.");

    Query query = new Query();
    query.addCriteria(Criteria.where(whereClause).exists(true));
    return mongoTemplate.findOne(query, Namespace.class);
  }
}
