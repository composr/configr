package com.citihub.configr.version;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import com.citihub.configr.exception.NotFoundException;
import com.citihub.configr.mongostorage.MongoNamespaceQueries;
import com.citihub.configr.namespace.Namespace;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class VersionService {

  private MongoNamespaceQueries nsQueries;

  public VersionService(@Autowired MongoNamespaceQueries nsQueries) {
    this.nsQueries = nsQueries;
  }

  public @NonNull Namespace fetchNamespace(String fullPath) {
    Namespace ns = findNamespaceOrThrowException(fullPath);
    log.info("using path {}, I found: {}", split(fullPath), ns);
    return ns;
  }

  String [] split(String fullPath) {
    return fullPath.substring(1).split("/");
  }

  Namespace findNamespaceOrThrowException(String fullPath) {
    Namespace ns = nsQueries.findByPath(fullPath);
    if( ns == null )
      throw new NotFoundException();
    return ns;
  }
}
