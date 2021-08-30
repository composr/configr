package com.citihub.configr.api;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface ConfigurationRepository extends MongoRepository<Namespace, String> {

}
