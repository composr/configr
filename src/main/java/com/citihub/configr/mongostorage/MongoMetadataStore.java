package com.citihub.configr.mongostorage;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.citihub.configr.metadata.Metadata;
import com.citihub.configr.storage.MetadataStore;

@Service("metadataStore")
public class MongoMetadataStore implements MetadataStore {

  private MongoMetadataRepository mongoRepository;

  public MongoMetadataStore(@Autowired MongoMetadataRepository mongoRepository) {
    this.mongoRepository = mongoRepository;
  }

  @Override
  public Optional<Metadata> getMetadata(String namespace) {
    return mongoRepository.findById(namespace);
  }

  @Override
  public Metadata patchMetadata(Metadata metadata, String namespace) {
    Optional<Metadata> current = getMetadata(namespace);
    if (current.isEmpty())
      return mongoRepository.save(metadata);
    else {
      System.out.println("Current is " + current.get());
      Metadata merged = current.get();
      merged.merge(metadata);
      System.out.println("Merged is now " + merged);
      return mongoRepository.save(merged);
    }
  }

  @Override
  public Metadata saveMetadata(Metadata metadata, String namespace) {
    return mongoRepository.save(metadata);
  }

}
