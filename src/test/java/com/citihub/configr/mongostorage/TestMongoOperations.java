package com.citihub.configr.mongostorage;

import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import com.citihub.configr.base.MongoTest;
import com.citihub.configr.namespace.Namespace;
import com.citihub.configr.version.Version;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TestMongoOperations extends MongoTest {

  private MongoOperations mongoOps;

  @Autowired
  private MongoTemplate mongoTemplate;

  @BeforeAll
  private void setup() {
    mongoOps = new MongoOperations(mongoTemplate, new ObjectMapper());
  }

  @Test
  public void testBadSaveRequest() throws Exception {
    mongoOps.findByPath("/x/y/z");
  }

  @Test
  public void testNoOldVersionToSave() throws Exception {
    mongoOps.saveAsOldVersion(Optional.ofNullable(null), null);
  }

  @Test
  public void testVersionAndSave() throws Exception {
    Namespace old = new Namespace("foo", "bar");
    old.setVersion(new Version("abc", "Willy Wonka"));

    mongoOps.saveAsOldVersion(Optional.ofNullable(old), new Namespace("foo", "bizz"));
  }

}
