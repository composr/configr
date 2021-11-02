package com.citihub.configr;

import java.net.InetSocketAddress;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;

@Configuration
@Profile("test")
public class TestMongoConfiguration {

  @Bean
  public MongoTemplate mongoTemplate(MongoDatabaseFactory mongoDatabaseFactory,
      MongoConverter mongoConverter) {
    return new MongoTemplate(mongoDatabaseFactory, mongoConverter);
  }

  @Bean
  public MongoDatabaseFactory mongoDbFactory(MongoServer mongoServer) {
    InetSocketAddress serverAddress = mongoServer.getLocalAddress();
    return new SimpleMongoClientDatabaseFactory(
        "mongodb://" + serverAddress.getHostName() + ":" + serverAddress.getPort() + "/test");
  }

  @Bean(destroyMethod = "shutdown")
  public MongoServer mongoServer() {
    MongoServer mongoServer = new MongoServer(new MemoryBackend());
    mongoServer.bind();
    return mongoServer;
  }

}
