package com.hireconnect.profileservice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hireconnect.profileservice.dto.request.ProfileRequestDto;
import com.hireconnect.profileservice.dto.response.ProfileResponseDto;
import com.hireconnect.profileservice.entity.Role;
import com.hireconnect.profileservice.security.AuthenticatedUser;
import com.hireconnect.profileservice.service.ProfileService;

@WebMvcTest(ProfileController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProfileService profileService;

    @Autowired
    private ObjectMapper objectMapper;

    private AuthenticatedUser candidate;

    @BeforeEach
    void setUp() {
        candidate = new AuthenticatedUser(1L, Role.CANDIDATE);
    }

    private void setAuthentication(AuthenticatedUser user) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                user, null, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    void createProfile_Success() throws Exception {
        setAuthentication(candidate);
        ProfileRequestDto request = new ProfileRequestDto();
        request.setFirstName("John");
        
        ProfileResponseDto response = ProfileResponseDto.builder().build();
        when(profileService.createProfile(eq(1L), eq(Role.CANDIDATE), any())).thenReturn(response);

        mockMvc.perform(post("/api/profiles")
                .principal(new UsernamePasswordAuthenticationToken(candidate, null))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void getMyProfile_Success() throws Exception {
        setAuthentication(candidate);
        ProfileResponseDto response = ProfileResponseDto.builder().build();
        when(profileService.getProfileByUserId(any())).thenReturn(response);

        mockMvc.perform(get("/api/profiles/me")
                .principal(new UsernamePasswordAuthenticationToken(candidate, null)))
                .andExpect(status().isOk());
    }

    @Test
    void updateMyProfile_Success() throws Exception {
        setAuthentication(candidate);
        ProfileRequestDto request = new ProfileRequestDto();
        request.setFirstName("John");

        ProfileResponseDto response = ProfileResponseDto.builder().build();
        when(profileService.updateProfile(any(), any())).thenReturn(response);

        mockMvc.perform(put("/api/profiles/me")
                .principal(new UsernamePasswordAuthenticationToken(candidate, null))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void getCandidateProfilePreview_Success() throws Exception {
        when(profileService.getCandidateProfilePreviewByUserId(1L)).thenReturn(null);

        mockMvc.perform(get("/api/profiles/internal/candidates/1/preview"))
                .andExpect(status().isOk());
    }
}
