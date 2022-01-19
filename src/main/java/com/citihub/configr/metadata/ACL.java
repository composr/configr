package com.citihub.configr.metadata;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ACL {

  private String role;

  private boolean read;
  private boolean write;
  private boolean delete;
}
