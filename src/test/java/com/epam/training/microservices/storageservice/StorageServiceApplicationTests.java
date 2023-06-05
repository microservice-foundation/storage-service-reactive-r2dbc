package com.epam.training.microservices.storageservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(locations = "classpath:application.properties")
class StorageServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
