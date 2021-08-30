package com.citihub.configr.api;

import lombok.Data;

@Data
public class Namespace {

  private Metadata metadata;
  private String key;
  private Object value;

}
