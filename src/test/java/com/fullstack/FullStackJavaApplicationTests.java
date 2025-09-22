package com.fullstack;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Main application test class
 * Demonstrates proper testing setup for Spring Boot application
 */
@SpringBootTest
@ActiveProfiles("test")
class FullStackJavaApplicationTests {

    @Test
    void contextLoads() {
        // This test ensures that the Spring application context loads successfully
        // It validates that all beans are properly configured and dependencies are resolved
    }
}
