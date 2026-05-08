package com.hireconnect.profileservice.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.hireconnect.profileservice.dto.request.ProfileRequestDto;
import com.hireconnect.profileservice.entity.Profile;
import com.hireconnect.profileservice.entity.Role;

class ProfileMapperTest {

    private ProfileMapper profileMapper;

    @BeforeEach
    void setUp() {
        profileMapper = new ProfileMapper();
    }

    @Test
    void toProfileEntity_Success() {
        ProfileRequestDto request = new ProfileRequestDto();
        request.setFirstName("John");
        request.setLastName("Doe");

        Profile entity = profileMapper.toProfileEntity(request, 1L, Role.CANDIDATE);

        assertNotNull(entity);
        assertEquals("John", entity.getFirstName());
        assertEquals("Doe", entity.getLastName());
        assertEquals(1L, entity.getUserId());
        assertEquals(Role.CANDIDATE, entity.getRole());
    }

    @Test
    void toProfileEntity_NullRequest_ReturnsNull() {
        assertNull(profileMapper.toProfileEntity(null, 1L, Role.CANDIDATE));
    }

    @Test
    void updateProfileEntity_Success() {
        Profile existingProfile = Profile.builder()
                .firstName("Old")
                .lastName("Name")
                .build();
        
        ProfileRequestDto request = new ProfileRequestDto();
        request.setFirstName("New");
        request.setLastName("Name");

        profileMapper.updateProfileEntity(existingProfile, request);

        assertEquals("New", existingProfile.getFirstName());
        assertEquals("Name", existingProfile.getLastName());
    }

    @Test
    void toProfileResponseDto_Success() {
        Profile profile = Profile.builder()
                .id(100L)
                .firstName("John")
                .role(Role.CANDIDATE)
                .build();

        var response = profileMapper.toProfileResponseDto(profile);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals("John", response.getFirstName());
    }
}
