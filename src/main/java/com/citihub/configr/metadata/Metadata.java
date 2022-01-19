package com.citihub.configr.metadata;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Metadata {

  public enum ValidationLevel {
    NONE, STRICT, LOOSE
  }

  private String description;
  private List<ACL> acls;
  private String schema;
  private ValidationLevel validationLevel;

  private String[] tags;

}
