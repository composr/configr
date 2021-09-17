package com.citihub.configr.mongostorage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.stereotype.Component;
import com.citihub.configr.namespace.Namespace;

@Component
public class MongoNamespaceQueries {

  private MongoTemplate mongoTemplate;

  public MongoNamespaceQueries(@Autowired MongoTemplate mongoTemplate) {
    this.mongoTemplate = mongoTemplate;
  }

  public Namespace findByPath(String path) {
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

  // public Namespace save(Namespace namespace) {
  // mongoTemplate.save(namespace.getVersion());
  // mongoTemplate.save(namespace);
  // }
}
