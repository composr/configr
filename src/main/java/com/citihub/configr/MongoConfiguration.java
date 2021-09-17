package com.citihub.configr;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import com.citihub.configr.mongostorage.MongoNamespaceDeserializer;
import com.citihub.configr.mongostorage.MongoNamespaceSerializer;
import com.citihub.configr.namespace.Namespace;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.common.base.Strings;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

@Profile("!test")
@Configuration
public class MongoConfiguration {

  @Value("${mongodb.db}")
  private String db;

  @Value("${mongodb.uri}")
  private String uri;

  @Value("${mongodb.username}")
  private String username;

  @Value("${mongodb.password}")
  private String password;

  @Value("${mongodb.auth_db}")
  private String authDB;

  public @Bean MongoClient mongoClient() {
    return Strings.isNullOrEmpty(username) ? getMongoClientNoAuth() : getMongoClientWithAuth();
  }

  private MongoClient getMongoClientNoAuth() {
    return MongoClients.create(
        MongoClientSettings.builder().applyConnectionString(new ConnectionString(uri)).build());
  }

  private MongoClient getMongoClientWithAuth() {
    return MongoClients
        .create(MongoClientSettings.builder().applyConnectionString(new ConnectionString(uri))
            .credential(MongoCredential.createCredential(username, db, password.toCharArray()))
            .build());
  }

  public @Bean MongoTemplate mongoTemplate() {
    return new MongoTemplate(mongoClient(), db);
  }

  public @Bean({"mongoObjectMapper"}) ObjectMapper mongoObjectMapper() {
    ObjectMapper mapper = new ObjectMapper();
    SimpleModule module = new SimpleModule();
    module.addSerializer(Namespace.class, new MongoNamespaceSerializer());
    module.addDeserializer(Namespace.class, new MongoNamespaceDeserializer());
    mapper.registerModule(module);

    return mapper;
  }
}
