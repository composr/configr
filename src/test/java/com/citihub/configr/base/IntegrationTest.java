package com.citihub.configr.base;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import com.citihub.configr.TestMongoConfiguration;

/**
 * Base class bootstrapping Spring and an embedded Mongo
 */
@SpringBootTest({"authentication.enabled=false", "authorization.enabled=false"})
@WithMockUser(username = "bobbo")
@Import(TestMongoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
@TestInstance(Lifecycle.PER_CLASS)
@Tag("integration")
@ActiveProfiles("test")
public class IntegrationTest {

}
