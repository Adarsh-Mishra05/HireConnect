package com.hireconnect.serviceregistry;

import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.appinfo.InstanceInfo;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@SpringBootTest(properties = {
        "spring.cloud.discovery.enabled=false",
        "eureka.client.enabled=false"
})
class ServiceRegistryApplicationTests {

    @Test
    void contextLoads() {
    }

    @TestConfiguration
    static class ServiceRegistryTestConfiguration {

        @Bean
        ApplicationInfoManager applicationInfoManager() {
            InstanceInfo instanceInfo = InstanceInfo.Builder.newBuilder()
                    .setAppName("SERVICE-REGISTRY")
                    .setInstanceId("service-registry-test")
                    .setVIPAddress("service-registry")
                    .build();
            return new ApplicationInfoManager(instanceInfo);
        }
    }
}
