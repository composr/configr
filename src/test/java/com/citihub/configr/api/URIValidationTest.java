package com.citihub.configr.api;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import com.citihub.configr.base.UnitTest;

public class URIValidationTest extends UnitTest {

  public URIValidationInterceptor interceptor;

  @BeforeAll
  public void setup() {
    interceptor = new URIValidationInterceptor();
  }

  @Test
  public void testQuery() throws Exception {
    assertThat(interceptor.isValidURI("/query?jsonPath=$..*")).isTrue();
  }

  @Test
  public void testFound() throws Exception {
    assertThat(interceptor.isValidURI("/foo/bar/baz")).isFalse();
  }

  @Test
  public void testNoURI() throws Exception {
    assertThat(interceptor.isValidURI("")).isFalse();
  }

  @Test
  public void testEmptyURI() throws Exception {
    assertThat(interceptor.isValidURI("/")).isFalse();
  }

  @Test
  public void testMissingNamespaceURI() throws Exception {
    assertThat(interceptor.isValidURI("/configuration")).isFalse();
  }

  @Test
  public void testMissingNamesapceButWithTrailingSlash() throws Exception {
    assertThat(interceptor.isValidURI("/metadata/")).isFalse();
  }

  @Test
  public void testGoodURI() throws Exception {
    assertThat(interceptor.isValidURI("/version/apples/and/oranges")).isTrue();
  }

  @Test
  public void testSwagger() throws Exception {
    assertThat(interceptor.isValidURI("/swagger-ui.html")).isTrue();
  }

  @Test
  public void testSwaggerUIRoutes() throws Exception {
    assertThat(interceptor.isValidURI("/swagger-ui/index.html")).isTrue();
  }

  @Test
  public void testAPIDocsRoute() throws Exception {
    assertThat(interceptor.isValidURI("/v3/api-docs/swagger-config")).isTrue();
  }
}
