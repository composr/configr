package com.citihub.configr.mongostorage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import com.citihub.configr.base.UnitTest;

public class TestMongoMetadataStore extends UnitTest {

  private MongoMetadataStore mongoStore;

  @BeforeAll
  private void setup() {
    mongoStore = new MongoMetadataStore(Mockito.mock(MongoMetadataRepository.class));
  }



  @Test
  public void testShaveNamespaceWithNormalNS() throws Exception {
    assertEquals(mongoStore.shaveNamespace("/x/y/z"), "/x/y");
  }

  @Test
  public void testShaveNamespaceWitOneSlug() throws Exception {
    assertEquals(mongoStore.shaveNamespace("/x"), "");
  }


}
