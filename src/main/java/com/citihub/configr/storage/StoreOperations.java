package com.citihub.configr.storage;

import java.util.List;
import java.util.Map;
import org.bson.Document;
import com.citihub.configr.namespace.Namespace;
import com.fasterxml.jackson.core.JsonProcessingException;

public interface StoreOperations {

  // Optional<Namespace> findById(String id);

  // Namespace storeNamespace(Namespace ns);

  // void saveAsOldVersion(Optional<Namespace> oldVersion, Namespace newVersion);

  List<Document> listVersionsByPath(String path);

  Namespace findByPath(String path);

  Namespace materialize(Map<String, Object> json, String[] pathTokens);

  Namespace saveNamespace(String[] path, boolean shouldMerge, boolean shouldReplace,
      Namespace materialized) throws JsonProcessingException;
}
