package com.citihub.configr.storage;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import com.citihub.configr.mongostorage.MongoOperations;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class StoreOperationsFactory implements FactoryBean<StoreOperations> {

  private StoreOperations storeOperations;

  private MongoTemplate mongoTemplate;
  private ObjectMapper objectMapper;

  public StoreOperationsFactory(@Autowired MongoTemplate mongoTemplate,
      @Autowired ObjectMapper objectMapper) {
    this.mongoTemplate = mongoTemplate;
    this.objectMapper = objectMapper;
  }

  @Override
  public Class<?> getObjectType() {
    return StoreOperations.class;
  }

  @Override
  public StoreOperations getObject() {
    if (storeOperations == null)
      storeOperations = new MongoOperations(mongoTemplate, objectMapper);

    log.error("Creating store operations from the factory!!");
    return storeOperations;
  }

  @Override
  public boolean isSingleton() {
    return true;
  }
}
