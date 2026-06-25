package com.example.propertyrentalmanagement;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "POSTGRES_URL", matches = ".+")
class PropertyRentalManagementApplicationTests {

    @Test
    void contextLoads() {
    }

}
