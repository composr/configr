package com.citihub.configr;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoClientFactoryBean;
import com.google.common.base.Strings;
import com.mongodb.ConnectionString;
import com.mongodb.MongoCredential;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@Profile("!test")
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

  public @Bean MongoClientFactoryBean mongo() {
    MongoClientFactoryBean mongo = new MongoClientFactoryBean();
    mongo.setConnectionString(new ConnectionString(uri));
    if (!Strings.isNullOrEmpty(username))
      mongo.setCredential(new MongoCredential[] {
          MongoCredential.createCredential(username, db, password.toCharArray())});
    return mongo;
  }

}
