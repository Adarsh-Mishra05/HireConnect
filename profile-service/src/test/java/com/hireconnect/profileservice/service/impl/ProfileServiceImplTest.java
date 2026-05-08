package com.hireconnect.profileservice.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import com.hireconnect.profileservice.client.ApplicationServiceClient;
import com.hireconnect.profileservice.client.JobServiceClient;
import com.hireconnect.profileservice.dto.request.ProfileRequestDto;
import com.hireconnect.profileservice.dto.response.ProfileResponseDto;
import com.hireconnect.profileservice.entity.Profile;
import com.hireconnect.profileservice.entity.Resume;
import com.hireconnect.profileservice.entity.Role;
import com.hireconnect.profileservice.exception.ProfileAlreadyExistsException;
import com.hireconnect.profileservice.exception.ProfileNotFoundException;
import com.hireconnect.profileservice.mapper.ProfileMapper;
import com.hireconnect.profileservice.repository.ProfileRepository;
import com.hireconnect.profileservice.repository.ResumeRepository;
import com.hireconnect.profileservice.security.AuthenticatedUser;

@ExtendWith(MockitoExtension.class)
public class ProfileServiceImplTest {

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private ProfileMapper profileMapper;

    @Mock
    private ResumeRepository resumeRepository;

    @Mock
    private JobServiceClient jobServiceClient;

    @Mock
    private ApplicationServiceClient applicationServiceClient;

    @InjectMocks
    private ProfileServiceImpl profileService;

    private AuthenticatedUser candidateUser;
    private AuthenticatedUser recruiterUser;
    private ProfileRequestDto requestDto;
    private Profile profile;
    private ProfileResponseDto responseDto;

    @BeforeEach
    void setUp() {
        candidateUser = new AuthenticatedUser(1L, Role.CANDIDATE);
        recruiterUser = new AuthenticatedUser(2L, Role.RECRUITER);

        requestDto = new ProfileRequestDto();
        requestDto.setFirstName("John");
        requestDto.setLastName("Doe");

        profile = new Profile();
        profile.setId(100L);
        profile.setUserId(1L);
        profile.setFirstName("John");

        responseDto = ProfileResponseDto.builder()
                .id(100L)
                .userId(1L)
                .firstName("John")
                .build();
    }

    @Test
    void createProfile_Success() {
        when(profileRepository.existsByUserId(1L)).thenReturn(false);
        when(profileMapper.toProfileEntity(requestDto, 1L, Role.CANDIDATE)).thenReturn(profile);
        when(profileRepository.save(any(Profile.class))).thenReturn(profile);
        when(profileMapper.toProfileResponseDto(profile)).thenReturn(responseDto);

        ProfileResponseDto response = profileService.createProfile(1L, Role.CANDIDATE, requestDto);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals("John", response.getFirstName());
    }

    @Test
    void createProfile_AlreadyExists_ThrowsException() {
        when(profileRepository.existsByUserId(1L)).thenReturn(true);

        assertThrows(ProfileAlreadyExistsException.class, () -> 
            profileService.createProfile(1L, Role.CANDIDATE, requestDto));
    }

    @Test
    void getProfileByUserId_Success() {
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(profileMapper.toProfileResponseDto(profile)).thenReturn(responseDto);

        ProfileResponseDto response = profileService.getProfileByUserId(candidateUser);

        assertNotNull(response);
        assertEquals(100L, response.getId());
    }

    @Test
    void getProfileByUserId_NotFound_ThrowsException() {
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.empty());

        assertThrows(ProfileNotFoundException.class, () -> 
            profileService.getProfileByUserId(candidateUser));
    }

    @Test
    void uploadResume_Success() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "resume.pdf", "application/pdf", "dummy content".getBytes());

        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(resumeRepository.findByProfile(profile)).thenReturn(Optional.empty());
        when(resumeRepository.save(any(Resume.class))).thenReturn(new Resume());

        String response = profileService.uploadResume(candidateUser, file);

        assertEquals("Resume uploaded successfully", response);
        verify(resumeRepository, times(1)).save(any(Resume.class));
    }

    @Test
    void getResumeForRecruiter_Success() {
        Resume resume = new Resume();
        resume.setFileName("resume.pdf");

        when(jobServiceClient.isJobOwnedByRecruiter(10L, 2L)).thenReturn(true);
        when(applicationServiceClient.hasCandidateAppliedToJob(1L, 10L)).thenReturn(true);
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(resumeRepository.findByProfile(profile)).thenReturn(Optional.of(resume));

        Resume response = profileService.getResumeForRecruiter(recruiterUser, 1L, 10L);

        assertNotNull(response);
        assertEquals("resume.pdf", response.getFileName());
    }

    @Test
    void getResumeForRecruiter_UnauthorizedJob_ThrowsException() {
        when(jobServiceClient.isJobOwnedByRecruiter(10L, 2L)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> 
            profileService.getResumeForRecruiter(recruiterUser, 1L, 10L));
    }
}
