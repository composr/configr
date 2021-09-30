package com.citihub.configr.mongostorage;

import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import com.citihub.configr.MongoConfiguration;
import com.citihub.configr.namespace.Namespace;
import com.citihub.configr.version.Version;
import com.fasterxml.jackson.databind.ObjectMapper;

@Tag("integration")
@Tag("data")
@DataMongoTest
@TestInstance(Lifecycle.PER_CLASS)
public class TestMongoOperations {

  private static final String POST_SAVE_CONTENT =
      "{\"x\":{\"y\":{\"z\":{ \"foo\": { \"bar\": { \"baz\": [ { \"buzz\": \"bizz\" }, { \"foo2\": \"bar2\" } ] } } }}}}";

  private static final String SAMPLE_CONTENT =
      "{ \"foo\": { \"bar\": { \"baz\": [ { \"buzz\": \"bizz\" }, { \"foo2\": \"bar2\" } ] } } }";

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
