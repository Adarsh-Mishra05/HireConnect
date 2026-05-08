package com.hireconnect.analyticsservice.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import com.hireconnect.analyticsservice.dto.response.AdminAnalyticsResponseDto;
import com.hireconnect.analyticsservice.dto.response.RecruiterAnalyticsResponseDto;
import com.hireconnect.analyticsservice.enums.Role;
import com.hireconnect.analyticsservice.security.AuthenticatedUser;
import com.hireconnect.analyticsservice.service.AnalyticsService;

@WebMvcTest(AnalyticsController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable security filters for simple controller testing
public class AnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AnalyticsService analyticsService;

    private AuthenticatedUser recruiterUser;
    private AuthenticatedUser adminUser;

    @BeforeEach
    void setUp() {
        recruiterUser = new AuthenticatedUser(1L, "recruiter@example.com", Role.RECRUITER);
        adminUser = new AuthenticatedUser(2L, "admin@example.com", Role.ADMIN);
    }

    private void setAuthentication(AuthenticatedUser user) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                user, null, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    void getMyRecruiterAnalytics_Success() throws Exception {
        setAuthentication(recruiterUser);
        RecruiterAnalyticsResponseDto response = RecruiterAnalyticsResponseDto.builder().build();
        when(analyticsService.getRecruiterAnalytics(1L)).thenReturn(response);

        mockMvc.perform(get("/api/analytics/recruiter/me")
                .principal(new UsernamePasswordAuthenticationToken(recruiterUser, null)))
                .andExpect(status().isOk());
    }

    @Test
    void getMyRecruiterAnalytics_Unauthorized_ThrowsException() throws Exception {
        setAuthentication(adminUser); // Admin trying to access recruiter endpoint

        mockMvc.perform(get("/api/analytics/recruiter/me")
                .principal(new UsernamePasswordAuthenticationToken(adminUser, null)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getPlatformAnalytics_Success() throws Exception {
        setAuthentication(adminUser);
        AdminAnalyticsResponseDto response = AdminAnalyticsResponseDto.builder().build();
        when(analyticsService.getPlatformAnalytics()).thenReturn(response);

        mockMvc.perform(get("/api/analytics/admin")
                .principal(new UsernamePasswordAuthenticationToken(adminUser, null)))
                .andExpect(status().isOk());
    }

    @Test
    void getPlatformAnalytics_Unauthorized_ThrowsException() throws Exception {
        setAuthentication(recruiterUser);

        mockMvc.perform(get("/api/analytics/admin")
                .principal(new UsernamePasswordAuthenticationToken(recruiterUser, null)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void exportPlatformAnalyticsCsv_Success() throws Exception {
        setAuthentication(adminUser);
        String csvContent = "Total Jobs,Total Users\n10,20";
        when(analyticsService.exportPlatformAnalyticsCsv()).thenReturn(csvContent);

        mockMvc.perform(get("/api/analytics/admin/export")
                .principal(new UsernamePasswordAuthenticationToken(adminUser, null)))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=platform-analytics.csv"))
                .andExpect(jsonPath("$").value(csvContent));
    }

    @Test
    void exportPlatformAnalyticsCsv_Unauthorized_ThrowsException() throws Exception {
        setAuthentication(recruiterUser);

        mockMvc.perform(get("/api/analytics/admin/export")
                .principal(new UsernamePasswordAuthenticationToken(recruiterUser, null)))
                .andExpect(status().isUnauthorized());
    }
}
