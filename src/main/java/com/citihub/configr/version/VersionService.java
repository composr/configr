package com.citihub.configr.version;

import java.util.List;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import com.citihub.configr.storage.StoreOperations;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class VersionService {

  private StoreOperations mongoOperations;

  public VersionService(@Autowired StoreOperations mongoOperations) {
    this.mongoOperations = mongoOperations;
  }

  public @NonNull List<Document> fetchVersions(String fullPath) {
    List<Document> ns = mongoOperations.listVersionsByPath(fullPath);
    log.info("using path {}, I found: {}", split(fullPath), ns);
    return ns;
  }

  String[] split(String fullPath) {
    return fullPath.substring(1).split("/");
  }
}
