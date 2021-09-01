package com.citihub.configr.namespace;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonSerialize(using = NamespaceSerializer.class)
@JsonDeserialize(using = NamespaceDeserializer.class)
public class Namespace {

  @JsonIgnore
  private Metadata metadata;

  @Id
  private String _id;
  private String key;
  private Object value;

  public Namespace(String key, Object value) {
    this.key = key;
    this.value = value;
  }

  public Namespace(Metadata metadata, String key, Object value) {
    this.metadata = metadata;
    this.key = key;
    this.value = value;
  }
}
