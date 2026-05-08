package com.hireconnect.profileservice.service;

import org.springframework.web.multipart.MultipartFile;

import com.hireconnect.profileservice.dto.request.ProfileRequestDto;
import com.hireconnect.profileservice.dto.response.CandidateProfilePreviewDto;
import com.hireconnect.profileservice.dto.response.ProfileResponseDto;
import com.hireconnect.profileservice.entity.Role;
import com.hireconnect.profileservice.entity.Resume;
import com.hireconnect.profileservice.security.AuthenticatedUser;

// [Disha Gujar] : Service interface defining the business logic contract for user profile management.
// Covers profile creation and update for candidates and recruiters, profile retrieval by user ID,
// candidate preview lookup for recruiters, and resume upload/download with authorization checks.
public interface ProfileService {

    ProfileResponseDto createProfile(Long userId, Role role, ProfileRequestDto requestDto);

    ProfileResponseDto getProfileByUserId(AuthenticatedUser user);

    ProfileResponseDto getProfileByUserIdInternal(Long userId);

    ProfileResponseDto updateProfile(AuthenticatedUser user, ProfileRequestDto requestDto);

    CandidateProfilePreviewDto getCandidateProfilePreviewByUserId(Long userId);

    String uploadResume(AuthenticatedUser user, MultipartFile file);

    Resume getMyResume(AuthenticatedUser user);

    Resume getResumeForRecruiter(AuthenticatedUser user, Long candidateId, Long jobId);
}