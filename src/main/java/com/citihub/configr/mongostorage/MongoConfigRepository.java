package com.citihub.configr.mongostorage;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.citihub.configr.namespace.Namespace;

public interface MongoConfigRepository extends MongoRepository<Namespace, String> {


}
