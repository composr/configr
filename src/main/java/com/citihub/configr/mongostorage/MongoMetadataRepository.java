package com.citihub.configr.mongostorage;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.citihub.configr.metadata.Metadata;

public interface MongoMetadataRepository extends MongoRepository<Metadata, String> {


}
