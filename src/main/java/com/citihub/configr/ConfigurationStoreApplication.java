package com.citihub.configr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@SpringBootApplication
@EnableMongoAuditing
public class ConfigurationStoreApplication {

  public static void main(String... args) {
    SpringApplication.run(ConfigurationStoreApplication.class, args);
  }


}
