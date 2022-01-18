package com.citihub.configr.storage;

import java.util.Optional;
import com.citihub.configr.metadata.Metadata;

public interface MetadataStore {

  Metadata saveMetadata(Metadata metadata, String namespace);

  Metadata patchMetadata(Metadata metadata, String namespace);

  Optional<Metadata> getMetadata(String namespace);
}
