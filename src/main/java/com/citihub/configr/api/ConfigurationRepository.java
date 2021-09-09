package com.citihub.configr.api;

import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import com.citihub.configr.namespace.Namespace;

public interface ConfigurationRepository extends MongoRepository<Namespace, String> {
  
  @Query("{$match: { $text: {$search: 0? }}")
  public Optional<Namespace> findNamespaceById(String id);

}
