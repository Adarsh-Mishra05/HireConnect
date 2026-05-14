package com.adminserver;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.cloud.discovery.enabled=false",
        "eureka.client.enabled=false",
        "spring.boot.admin.client.enabled=false"
})
class AdminServerApplicationTests {

	@Test
	void contextLoads() {
	}

}
