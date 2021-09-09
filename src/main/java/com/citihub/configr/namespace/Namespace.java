package com.citihub.configr.namespace;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonKey;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
@Document
@JsonSerialize(using = NamespaceSerializer.class)
@JsonDeserialize(using = NamespaceDeserializer.class)
public class Namespace {

  @JsonIgnore
  private Metadata metadata;

  @Id
  private String namespace;

  @JsonKey
  private String key;

  @JsonValue
  private Object value;

  public Namespace() {
    this("", null);
  }

  public Namespace(String key, Object value) {
    this(key, value, "");
  }

  public Namespace(String key, Object value, String fullNamespace) {
    this.key = key;
    this.value = value;
    this.namespace = fullNamespace;
  }

}
