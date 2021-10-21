package com.citihub.configr.base;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Base class bootstrapping Spring and an embedded Mongo
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = true)
@TestInstance(Lifecycle.PER_CLASS)
@Tag("integration")
@ActiveProfiles("test")
public class SecureIntegrationTest {
}
