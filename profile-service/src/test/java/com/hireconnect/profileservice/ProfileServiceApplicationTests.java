package com.hireconnect.profileservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"spring.cloud.discovery.enabled=false",
		"eureka.client.enabled=false",
		"spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration"
})
class ProfileServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
