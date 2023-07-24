package com.citihub.configr.mongostorage;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import com.citihub.configr.metadata.ACL;
import com.citihub.configr.metadata.Metadata;
import com.citihub.configr.metadata.Metadata.ValidationLevel;
import com.citihub.configr.storage.MetadataStore;
import com.google.common.base.Stopwatch;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service("metadataStore")
public class MongoMetadataStore implements MetadataStore {

  public static final String CASCADED_DESCRIPTION = "Synthesized metadata to hold cascaded ACLs";

  private MongoMetadataRepository mongoRepository;

  @Value("${authorization.disableAggregation:false}")
  private boolean disableACLAggregation;

  public MongoMetadataStore(@Autowired MongoMetadataRepository mongoRepository) {
    this.mongoRepository = mongoRepository;
  }

  @Override
  public Optional<Metadata> getMetadata(String namespace) {
    if (disableACLAggregation)
      return fetchMetadata(namespace);

    Set<ACL> aggregatedAcls = aggregateAcls(namespace);
    Optional<Metadata> metadata = fetchMetadata(namespace);
    if (metadata.isPresent()) {
      if (metadata.get().getAcls() != null)
        metadata.get().getAcls().addAll(aggregatedAcls);
      else
        metadata.get().setAcls(aggregatedAcls);
    } else if (!aggregatedAcls.isEmpty()) {
      metadata = Optional.of(new Metadata(namespace, CASCADED_DESCRIPTION, aggregatedAcls, null,
          ValidationLevel.NONE, null));
    }

    return metadata;
  }

  @Override
  @CacheEvict(key = "namespace")
  public Metadata patchMetadata(Metadata metadata, String namespace) {
    Optional<Metadata> current = getMetadata(namespace);
    if (current.isEmpty())
      return mongoRepository.save(metadata);
    else {
      Metadata merged = current.get();
      merged.merge(metadata);
      return mongoRepository.save(merged);
    }
  }

  @Override
  @CacheEvict(key = "namespace")
  public Metadata saveMetadata(Metadata metadata, String namespace) {
    return mongoRepository.save(metadata);
  }

  Set<ACL> aggregateAcls(String namespace) {
    Stopwatch watch = new Stopwatch();
    watch.start();
    String token = namespace;
    Set<ACL> acls = new HashSet<>();
    while ((token = shaveNamespace(token)) != "") {
      Optional<Metadata> curMetadata = fetchMetadata(token);
      if (curMetadata.isPresent() && curMetadata.get().getAcls() != null)
        acls.addAll(curMetadata.get().getAcls());
    }

    watch.stop();
    log.info("ACL Aggregation took {}ms", watch.elapsedMillis());
    return acls;
  }

  @Cacheable(key = "namespace")
  Optional<Metadata> fetchMetadata(String namespace) {
    return mongoRepository.findById(namespace);
  }

  /**
   * Takes in a URI segment, e.g. /how/are/you and returns one segment shaved off, e.g. /how/are
   * 
   * @param namespace
   * @return namespace with segment shaved off
   */
  String shaveNamespace(String namespace) {
    return namespace.substring(0, namespace.lastIndexOf("/"));
  }
}
