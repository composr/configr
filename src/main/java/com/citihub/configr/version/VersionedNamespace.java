package com.citihub.configr.version;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import com.citihub.configr.namespace.Namespace;
import com.google.common.hash.Hashing;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
@Document
public class VersionedNamespace {

  @Id
  String id;
  LocalDateTime created;
  String user;

  String patchToSource;

  public VersionedNamespace(Namespace ns, String patchToSource) {
    this.id = Hashing.sha256()
        .hashString(ns.getVersion().getId()
            + String.valueOf(ns.getVersion().getCreated().toEpochSecond(ZoneOffset.UTC)))
        .toString();
    this.created = ns.getVersion().getCreated();
    this.patchToSource = patchToSource;
  }

}
