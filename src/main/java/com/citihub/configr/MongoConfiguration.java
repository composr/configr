package com.citihub.configr;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

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
    return MongoClients
        .create(MongoClientSettings.builder().applyConnectionString(new ConnectionString(uri))
            .credential(MongoCredential.createCredential(username, db, password.toCharArray()))
            .build());
  }

  public @Bean MongoTemplate mongoTemplate() {
    return new MongoTemplate(mongoClient(), db);
  }
}
