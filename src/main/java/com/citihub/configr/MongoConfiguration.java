package com.citihub.configr;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import com.azure.identity.ManagedIdentityCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.google.common.base.Strings;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
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

  @Value("${mongodb.cert_subject}")
  private String mongoCertSubject;

  @Value("${mongodb.azure_kv_uri}")
  private String azureKVUri;

  @Value("${mongodb.azure_kv_cert_key}")
  private String azureKVCertKey;

  @Value("${mongodb.azure_kv_password_key}")
  private String azureKVPasswordKey;

  @Value("${mongodb.username}")
  private String username;

  @Value("${mongodb.password}")
  private String password;

  @Value("${mongodb.auth_db}")
  private String authDB;

  @Autowired
  private MappingMongoConverter mongoConverter;

  public @Bean MongoClient mongoClient() {
    return MongoClients.create(mongoSettings());
  }

  public MongoClientSettings mongoSettings() {
    if (!Strings.isNullOrEmpty(keyStorePath))
      return getX509Auth();
    else if (!Strings.isNullOrEmpty(username))
      return getBasicCredentialAuth();
    else if (!Strings.isNullOrEmpty(azureKVUri))
      return getX509FromAzure();
    else
      return null;
  }

  public @Bean MongoDatabaseFactory mongoDatabaseFactory() {
    return new SimpleMongoClientDatabaseFactory(mongoClient(), db);
  }

  public @Bean MongoTemplate mongoTemplate() {
    return new MongoTemplate(mongoDatabaseFactory(), mongoConverter);
  }

  private MongoClientSettings getBasicCredentialAuth() {
    log.info("Using basic auth with MongoDB");
    return MongoClientSettings.builder().applyConnectionString(new ConnectionString(uri))
        .credential(MongoCredential.createCredential(username, db, password.toCharArray())).build();
  }

  private MongoClientSettings getX509Auth() {
    log.info("Using x509 auth with MongoDB");
    System.setProperty("javax.net.ssl.keyStore", keyStorePath);
    System.setProperty("javax.net.ssl.keyStorePassword", keyStorePassword);

    return MongoClientSettings.builder().applyConnectionString(new ConnectionString(uri))
        .credential(MongoCredential.createMongoX509Credential(mongoCertSubject))
        .applyToSslSettings(b -> b.enabled(true)).build();
  }

  private MongoClientSettings getX509FromAzure() {
    log.info("Using x509 auth from Azure KV with MongoDB");
    try {
      File f = writeCertToFile();
      String password = fetchSecret(azureKVUri, azureKVPasswordKey);

      System.setProperty("javax.net.ssl.keyStore", f.getAbsolutePath());
      System.setProperty("javax.net.ssl.keyStorePassword", password);
    } catch (IOException e) {
      log.error(
          "Error building cert to file system for Mongo x509 connectivity. Cowardly refusing to continue.");
      throw new RuntimeException(e);
    }

    return MongoClientSettings.builder().applyConnectionString(new ConnectionString(uri))
        .credential(MongoCredential.createMongoX509Credential(mongoCertSubject))
        .applyToSslSettings(b -> b.enabled(true)).build();
  }

  File writeCertToFile() throws IOException {
    String certStr = fetchSecret(azureKVUri, azureKVCertKey).replaceAll("[\\r\\n]+", "");
    byte[] cert = Base64.getDecoder().decode(certStr);
    File f = File.createTempFile("mongo", "jks");
    FileOutputStream writer = new FileOutputStream(f);

    try {
      writer.write(cert);
    } finally {
      writer.close();
    }
    return f;
  }

  String fetchSecret(String kvURI, String key) {
    SecretClient client = buildClient(kvURI);
    return client.getSecret(key).getValue();
  }

  SecretClient buildClient(String kvURI) {
    return new SecretClientBuilder().vaultUrl(kvURI)
        .credential(new ManagedIdentityCredentialBuilder().build()).buildClient();
  }
}
