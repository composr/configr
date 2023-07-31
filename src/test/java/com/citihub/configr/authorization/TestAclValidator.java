package com.citihub.configr.authorization;

import javax.servlet.http.HttpServletRequest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import com.citihub.configr.metadata.MetadataService;

public class TestAclValidator {

  AclValidator validator;

  @BeforeEach
  public void setup() {
    validator = new AclValidator(Mockito.mock(HttpServletRequest.class),
        Mockito.mock(MetadataService.class));
  }

  @Test
  public void testRootURITrims() {
    Assertions.assertThat(validator.getTrimmedPath("/")).isEqualTo("/");
  }

  @Test
  public void testNullURITrims() {
    Assertions.assertThat(validator.getTrimmedPath(null)).isEqualTo(null);
  }

  @Test
  public void testRootURIWithResourceTrims() {
    Assertions.assertThat(validator.getTrimmedPath("/foo")).isEqualTo("/foo");
  }

  @Test
  public void testStripURI() {
    Assertions.assertThat(validator.getTrimmedPath("/configuration/bar/baz")).isEqualTo("/bar/baz");
  }
}
