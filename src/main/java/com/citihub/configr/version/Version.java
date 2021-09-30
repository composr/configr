package com.citihub.configr.version;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Version {

  private String id;
  private LocalDateTime created;
  private String creator;

  public Version(String id, String creator) {
    this.id = id;
    this.creator = creator;
    created = LocalDateTime.now();
  }

  public Version(Version version) {
    this.id = version.getId();
    this.created = version.getCreated();
    this.creator = version.creator;
  }
}
