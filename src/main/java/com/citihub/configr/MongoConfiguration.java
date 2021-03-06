package com.citihub.configr;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoClientFactoryBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import com.google.common.base.Strings;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.client.MongoClient;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@Profile("!test")
public class MongoConfiguration {

  @Value("${mongodb.db}")
  private String db;

  @Value("${mongodb.uri}")
  private String uri;

  @Value("${mongodb.mongo_keystore_path}")
  private String keyStorePath;

  @Value("${mongodb.mongo_keystore_password}")
  private String keyStorePassword;

  @Value("${mongodb.username}")
  private String username;

  @Value("${mongodb.password}")
  private String password;

  @Value("${mongodb.auth_db}")
  private String authDB;

  @Autowired
  private MongoClient mongoClient;

  @Autowired
  private MappingMongoConverter mongoConverter;

  public @Bean MongoClientFactoryBean mongo() {
    MongoClientFactoryBean mongo = new MongoClientFactoryBean();
    mongo.setConnectionString(new ConnectionString(uri));
    if (!Strings.isNullOrEmpty(keyStorePath))
      setX509Auth(mongo);
    else if (!Strings.isNullOrEmpty(username))
      setBasicCredentialAuth(mongo);

    return mongo;
  }

  public @Bean MongoDatabaseFactory mongoDatabaseFactory() {
    return new SimpleMongoClientDatabaseFactory(mongoClient, db);
  }

  public @Bean MongoTemplate mongoTemplate() {
    return new MongoTemplate(mongoDatabaseFactory(), mongoConverter);
  }

  private void setBasicCredentialAuth(MongoClientFactoryBean mongo) {
    mongo.setCredential(new MongoCredential[] {
        MongoCredential.createCredential(username, db, password.toCharArray())});
  }

  private void setX509Auth(MongoClientFactoryBean mongo) {
    System.setProperty("javax.net.ssl.keyStore", keyStorePath);
    System.setProperty("javax.net.ssl.keyStorePassword", keyStorePassword);

    mongo.setMongoClientSettings(
        MongoClientSettings.builder().credential(MongoCredential.createMongoX509Credential())
            .applyToSslSettings(b -> b.enabled(true)).build());
  }

}
