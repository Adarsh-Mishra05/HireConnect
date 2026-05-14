package com.hireconnect.apigateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
		"jwt.secret=test-secret-key-for-api-gateway-tests-123456"
})
class ApiGatewayApplicationTests {

	@Test
	void contextLoads() {
	}

}
