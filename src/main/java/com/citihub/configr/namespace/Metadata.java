package com.citihub.configr.namespace;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Metadata {

  private String description;
  private String owner;
  private String somethingElse;

  private String[] tags;

}
