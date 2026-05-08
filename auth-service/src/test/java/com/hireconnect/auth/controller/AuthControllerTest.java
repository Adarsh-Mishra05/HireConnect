package com.hireconnect.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hireconnect.auth.dto.request.ForgotPasswordRequest;
import com.hireconnect.auth.dto.request.LoginRequest;
import com.hireconnect.auth.dto.request.RefreshTokenRequest;
import com.hireconnect.auth.dto.request.RegisterRequest;
import com.hireconnect.auth.dto.request.ResetPasswordRequest;
import com.hireconnect.auth.dto.response.AuthResponse;
import com.hireconnect.auth.dto.response.TokenValidationResponse;
import com.hireconnect.auth.entity.Role;
import com.hireconnect.auth.security.SecurityConfig;
import com.hireconnect.auth.service.AuthService;

@WebMvcTest(
    controllers = AuthController.class,
    excludeAutoConfiguration = {
        DataSourceAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class
    },
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = SecurityConfig.class
    )
)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
@TestPropertySource(properties = {
    "spring.datasource.url=",
    "spring.jpa.hibernate.ddl-auto=none",
    "eureka.client.enabled=false",
    "spring.cloud.discovery.enabled=false",
    "auth.oauth-success-redirect-url=http://localhost:4200/oauth-success",
    "auth.oauth-failure-redirect-url=http://localhost:4200/oauth-failure",
    "app.jwt.secret=test-secret-key-for-unit-testing-only-32chars",
    "app.jwt.expiration=3600000"
})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    private AuthResponse authResponse;

    @BeforeEach
    void setUp() {
        authResponse = AuthResponse.builder()
                .accessToken("dummy-access-token")
                .refreshToken("dummy-refresh-token")
                .build();
    }

    @Test
    void register_ShouldReturnCreated() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("john.doe@example.com");
        request.setPassword("password123");
        request.setRole(Role.CANDIDATE);

        when(authService.register(any(RegisterRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").value("dummy-access-token"));
    }

    @Test
    void login_ShouldReturnOk() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("john.doe@example.com");
        request.setPassword("password123");

        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("dummy-access-token"));
    }

    @Test
    void refreshToken_ShouldReturnOk() throws Exception {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("dummy-refresh-token");

        when(authService.refreshToken(any(RefreshTokenRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("dummy-access-token"));
    }

    @Test
    void forgotPassword_ShouldReturnOk() throws Exception {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("john.doe@example.com");

        doNothing().when(authService).forgotPassword(any(ForgotPasswordRequest.class));

        mockMvc.perform(post("/api/v1/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("OTP sent successfully to your email"));
    }

    @Test
    void resetPassword_ShouldReturnOk() throws Exception {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setEmail("john.doe@example.com");
        request.setOtp("123456");
        request.setNewPassword("newPassword123");

        doNothing().when(authService).resetPassword(any(ResetPasswordRequest.class));

        mockMvc.perform(post("/api/v1/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Password reset successfully"));
    }

    @Test
    void validateToken_ShouldReturnOk() throws Exception {
        TokenValidationResponse response = TokenValidationResponse.builder()
                .valid(true)
                .userId(1L)
                .role(Role.CANDIDATE)
                .build();

        when(authService.validateToken(any(String.class))).thenReturn(response);

        mockMvc.perform(get("/api/v1/auth/validate")
                .header("Authorization", "Bearer dummy-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true));
    }
}
