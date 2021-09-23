package com.citihub.configr.version;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Version {

  private String id;
  private LocalDateTime created;
  
  public Version(String id) {
    this.id = id;
    created = LocalDateTime.now();
  }

  public Version(Version version) {
    this.id = version.getId();
    this.created = version.getCreated();
  }
}
