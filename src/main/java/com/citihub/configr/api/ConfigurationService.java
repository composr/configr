package com.citihub.configr.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.citihub.configr.namespace.Namespace;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ConfigurationService {

  private ConfigurationRepository configRepo;

  public ConfigurationService(@Autowired ConfigurationRepository configRepo) {
    this.configRepo = configRepo;
  }

  public Namespace fetchNamespace(String fullPath) {
    log.info("I found: {}", configRepo.findByKey(fullPath).orElse(null));

    return configRepo.findByKey(fullPath).orElse(null);
  }

  public Namespace storeNamespace(Namespace namespace, String path) {
    // Tokenize path
    // Fetch or create namespaces associated with path
    // Insert or update
    Namespace conflated = conflate(namespace, path);
    try {
      log.info("Conflated is {}", new ObjectMapper().writeValueAsString(conflated));
    } catch (Exception e) {

    }
    return configRepo.save(conflated);
  }

  private Namespace conflate(Namespace namespace, String path) {
    String[] pathTokens = path.split("/");

    Namespace lastNamespace = namespace;

    // Intentionally ignore the root of the URI
    for (int i = pathTokens.length - 1; i > 1; i--)
      lastNamespace = new Namespace(pathTokens[i], lastNamespace);

    return lastNamespace;
  }

}
