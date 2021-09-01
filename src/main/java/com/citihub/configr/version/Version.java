package com.citihub.configr.version;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;

@Data
public class Version {

  private UUID id;
  private LocalDateTime created;
  private UUID predecessor;

  public Version() {
    id = UUID.randomUUID();
    created = LocalDateTime.now();
  }

  public Version(UUID predecessor) {
    this();
    this.predecessor = predecessor;
  }


}
