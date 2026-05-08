package com.hireconnect.applicationservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;



@Configuration
public class FeignClientConfig {

	private static final String USER_ID_HEADER = "X-Auth-User-Id";
	private static final String EMAIL_HEADER = "X-Auth-User-Email";
	private static final String ROLE_HEADER = "X-Auth-User-Role";

	@Bean
	public RequestInterceptor requestInterceptor() {
		return requestTemplate -> {
			RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

			if (requestAttributes instanceof ServletRequestAttributes servletRequestAttributes) {
				HttpServletRequest currentRequest = servletRequestAttributes.getRequest();

				String userId = currentRequest.getHeader(USER_ID_HEADER);
				String email = currentRequest.getHeader(EMAIL_HEADER);
				String role = currentRequest.getHeader(ROLE_HEADER);

				if (userId != null) {
					requestTemplate.header(USER_ID_HEADER, userId);
				}

				if (email != null) {
					requestTemplate.header(EMAIL_HEADER, email);
				}

				if (role != null) {
					requestTemplate.header(ROLE_HEADER, role);
				}
			}
		};
	}
}