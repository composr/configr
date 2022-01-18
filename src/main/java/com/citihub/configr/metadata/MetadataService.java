package com.citihub.configr.metadata;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.citihub.configr.storage.MetadataStore;

@Service
public class MetadataService {

  private MetadataStore metadataStore;

  public MetadataService(@Autowired MetadataStore metadataStore) {
    this.metadataStore = metadataStore;
  }

  public Optional<Metadata> getMetadataForNamespace(String namespace) {
    return metadataStore.getMetadata(namespace);
  }

  public Metadata setMetadataForNamespace(Metadata metadata, String namespace) {
    return metadataStore.saveMetadata(metadata, namespace);
  }

  public Metadata patchMetadataForNamespace(Metadata metadata, String namespace) {
    return metadataStore.patchMetadata(metadata, namespace);
  }

}
