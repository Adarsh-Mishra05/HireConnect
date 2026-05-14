package com.hireconnect.applicationservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.hireconnect.applicationservice.client.JobServiceClient;
import com.hireconnect.applicationservice.client.ProfileServiceClient;
import com.hireconnect.applicationservice.producer.NotificationEventProducer;
import com.hireconnect.applicationservice.repository.JobApplicationRepository;

@SpringBootTest(properties = {
		"spring.cloud.discovery.enabled=false",
		"eureka.client.enabled=false",
		"spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration"
})
class ApplicationServiceApplicationTests {

	@MockBean
	private JobApplicationRepository jobApplicationRepository;

	@MockBean
	private JobServiceClient jobServiceClient;

	@MockBean
	private ProfileServiceClient profileServiceClient;

	@MockBean
	private NotificationEventProducer notificationEventProducer;

	@Test
	void contextLoads() {
	}

}
