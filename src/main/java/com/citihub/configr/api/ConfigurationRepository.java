package com.citihub.configr.api;

import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import com.citihub.configr.namespace.Namespace;

public interface ConfigurationRepository extends MongoRepository<Namespace, String> {

  public Optional<Namespace> findByKey(String key);

}
