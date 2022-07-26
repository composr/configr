package com.citihub.configr.namespace;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import com.citihub.configr.exception.NotFoundException;
import com.citihub.configr.exception.SaveFailureException;
import com.citihub.configr.metadata.SchemaValidationResult;
import com.citihub.configr.schema.SchemaValidationService;
import com.citihub.configr.storage.StoreOperations;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class NamespaceService {

  private StoreOperations mongoOperations;

  private SchemaValidationService schemaValidationService;

  public NamespaceService(@Autowired StoreOperations mongoOperations,
      @Autowired SchemaValidationService schemaValidationService) {
    this.mongoOperations = mongoOperations;
    this.schemaValidationService = schemaValidationService;
  }

  public Namespace deleteNamespace(String fullPath) {
    try {
      return mongoOperations.deleteByPath(fullPath);
    } catch (JsonProcessingException e) {
      throw new SaveFailureException();
    }
  }

  public @NonNull Namespace getNamespace(String fullPath) {
    Namespace ns = findNamespaceOrThrowException(fullPath);
    log.info("using path {}, I found: {}", split(fullPath), ns);
    return ns;
  }

  public @NonNull Map<String, Object> getNamespaceValue(String fullPath) {
    Namespace ns = findNamespaceOrThrowException(fullPath);
    log.info("using path {}, I found: {}", split(fullPath), ns);

    if (ns.getValue() instanceof Map) {
      return trimKeysFromResponseMap(split(fullPath), (Map<String, Object>) ns.getValue());
    } else
      return Collections.singletonMap(ns.getKey(), ns.getValue());
  }

  Namespace findNamespaceOrThrowException(String fullPath) {
    Namespace ns = mongoOperations.findByPath(fullPath);
    if (ns == null)
      throw new NotFoundException();
    return ns;
  }

  Map<String, Object> trimKeysFromResponseMap(String[] split, Map<String, Object> result) {
    log.info("using path {}", (Object[]) split);
    for (int i = 0; i < split.length - 1; i++) {
      log.info("Removing key {}", split[i]);
      result = (Map<String, Object>) result.remove(split[i]);
    }

    result.keySet().removeIf(id -> !id.equals(split[split.length - 1]));
    return result;
  }

  public Namespace storeNamespaceValue(Map<String, Object> json, String path, boolean shouldMerge,
      boolean shouldReplace) throws JsonMappingException, JsonParseException, IOException {

    Namespace materialized = mongoOperations.materialize(json, split(path));

    validateAgainstSchema(materialized);

    return mongoOperations.saveNamespace(split(path), shouldMerge, shouldReplace, materialized);
  }

  private void validateAgainstSchema(Namespace materialized) throws JsonProcessingException {
    Optional<SchemaValidationResult> result =
        schemaValidationService.getValidationReport(materialized);

    if (result.isPresent() && !result.get().isSuccess())
      ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getResponse()
          .addHeader("X-Schema-Validity", "false");
  }

  String[] split(String fullPath) {
    return fullPath.substring(1).split("/");
  }


}
