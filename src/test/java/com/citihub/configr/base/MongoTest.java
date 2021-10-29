package com.citihub.configr.base;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import com.citihub.configr.TestMongoConfiguration;

/**
 * Base class bootstrapping an embedded Mongo but not Spring
 */
@Tag("integration")
@Tag("data")
@DataMongoTest
@Import(TestMongoConfiguration.class)
@TestInstance(Lifecycle.PER_CLASS)
@ActiveProfiles("test")
public class MongoTest {
}
