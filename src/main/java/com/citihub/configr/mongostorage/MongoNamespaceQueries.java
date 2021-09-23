package com.citihub.configr.mongostorage;

import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.data.mongodb.core.index.TextIndexDefinition;
import org.springframework.data.mongodb.core.index.TextIndexDefinition.TextIndexDefinitionBuilder;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.stereotype.Component;
import com.citihub.configr.namespace.Namespace;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class MongoNamespaceQueries {

  private MongoTemplate mongoTemplate;

  public MongoNamespaceQueries(@Autowired MongoTemplate mongoTemplate) {
    this.mongoTemplate = mongoTemplate;
  }

  public Namespace findByPath(String path) {
    String whereClause = "value" + path.replaceAll("\\/", "\\.");
    
    Query query = new Query();
    query.addCriteria(Criteria.where(whereClause).exists(true));
    return mongoTemplate.findOne(query, Namespace.class);
  }
  
  private Namespace findByTextMatch(String path) {
    MatchOperation filterNamespace =
        Aggregation.match(TextCriteria.forDefaultLanguage().matching(path));

    TypedAggregation<Namespace> aggregation =
        Aggregation.newAggregation(Namespace.class, filterNamespace);
    AggregationResults<Namespace> result = mongoTemplate.aggregate(aggregation, Namespace.class);
    if (result.iterator().hasNext())
      return result.iterator().next();
    else
      return null; // Nothing found
  }

  @PostConstruct
  public void postConstruct() {
    log.info("Ensuring indexes are set.");
    try {
      ensureIndexes(mongoTemplate);
    } catch (NullPointerException e) {
      log.error("DB is null - expected during test runs but this is a "
          + "horrible hack.");
    }
  }
 
  private void ensureIndexes(MongoTemplate mongoTemplate) {
    TextIndexDefinition textIndex = new TextIndexDefinitionBuilder()
        .onField("_id")
        .build();

    mongoTemplate.indexOps(Namespace.class).ensureIndex(textIndex);
  }
  
}
