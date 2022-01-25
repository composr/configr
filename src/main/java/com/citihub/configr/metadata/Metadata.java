package com.citihub.configr.metadata;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import com.google.common.base.Strings;
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

  public String id;

  private String description;
  private Set<ACL> acls;

  private Map<String, Object> schema;
  private ValidationLevel validationLevel;

  private Set<String> tags;

  public void merge(Metadata copy) {
    if (!Strings.isNullOrEmpty(copy.getDescription()))
      this.description = copy.getDescription();

    if (copy.getSchema() != null)
      this.schema = copy.getSchema();

    if (copy.getValidationLevel() != null)
      this.validationLevel = copy.getValidationLevel();

    nullSafeMergeSets(this, copy);
  }

  private void nullSafeMergeSets(Metadata to, Metadata from) {
    if (to.getAcls() == null) {
      if (from.getAcls() != null)
        to.setAcls(new HashSet<ACL>(from.getAcls()));
    } else if (from.getAcls() != null)
      to.getAcls().addAll(from.getAcls());

    if (to.getTags() == null) {
      if (from.getTags() != null)
        to.setTags(new HashSet<String>(from.getTags()));
    } else if (from.getTags() != null)
      to.getTags().addAll(from.getTags());
  }
}
