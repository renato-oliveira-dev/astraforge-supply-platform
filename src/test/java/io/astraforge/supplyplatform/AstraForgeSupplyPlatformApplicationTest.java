package io.astraforge.supplyplatform;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class AstraForgeSupplyPlatformApplicationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void testApplicationContextShouldLoad() {
        assertThat(applicationContext)
                .as("Spring application context")
                .isNotNull();
    }
}
