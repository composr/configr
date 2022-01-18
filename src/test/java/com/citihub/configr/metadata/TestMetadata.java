package com.citihub.configr.metadata;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.citihub.configr.base.UnitTest;
import com.citihub.configr.metadata.Metadata.ValidationLevel;

public class TestMetadata extends UnitTest {

  Metadata copy;
  Set<String> testTagSet;
  Set<ACL> testACLSet;
  ACL testAcl;

  @BeforeEach
  public void setupEach() {
    testTagSet = new HashSet<String>();
    testTagSet.add("hi");
    testTagSet.add("my");

    testAcl = new ACL("testUser", true, false, false);
    testACLSet = new HashSet<ACL>();
    testACLSet.add(testAcl);

    copy = new Metadata("/x", "the", testACLSet, "empty schema", ValidationLevel.NONE, testTagSet);
  }

  @Test
  public void testMerge() {
    Metadata original = new Metadata("/x", "hi", new HashSet<ACL>(), "{}", ValidationLevel.LOOSE,
        new HashSet<String>());
    original.getTags().add("bobbo");

    original.merge(copy);
    assertThat(original.getDescription()).isEqualTo(copy.getDescription());
    assertThat(original.getSchema()).isEqualTo(copy.getSchema());
    assertThat(original.getValidationLevel()).isEqualTo(copy.getValidationLevel());
    assertThat(original.getAcls()).contains(testAcl);
    assertThat(original.getTags()).hasSize(3);
  }

  @Test
  public void testMergeSourceNulls() {
    Metadata original = new Metadata("/x", "hi", null, null, null, null);
    Metadata expected =
        new Metadata("/x", "the", testACLSet, "empty schema", ValidationLevel.NONE, testTagSet);

    original.merge(copy);
    assertThat(original).isEqualTo(expected);
  }

  @Test
  public void testMergeLotsOfNulls() {
    Metadata original = new Metadata("/x", null, null, null, null, null);
    Metadata expected = new Metadata("/x", null, null, null, null, null);

    original.merge(expected);
    assertThat(original).isEqualTo(expected);
  }
}
