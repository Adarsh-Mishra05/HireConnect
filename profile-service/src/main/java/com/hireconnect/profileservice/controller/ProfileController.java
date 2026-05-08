package com.hireconnect.profileservice.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.hireconnect.profileservice.dto.request.ProfileRequestDto;
import com.hireconnect.profileservice.dto.response.CandidateProfilePreviewDto;
import com.hireconnect.profileservice.dto.response.ProfileResponseDto;
import com.hireconnect.profileservice.entity.Resume;
import com.hireconnect.profileservice.security.AuthenticatedUser;
import com.hireconnect.profileservice.service.ProfileService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
@Slf4j
public class ProfileController {

    private final ProfileService profileService;

    @PostMapping
    public ResponseEntity<ProfileResponseDto> createProfile(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody ProfileRequestDto requestDto
    ) {
        log.info("Received create profile request for userId={} with role={}", user.getUserId(), user.getRole());

        ProfileResponseDto response = profileService.createProfile(
                user.getUserId(),
                user.getRole(),
                requestDto
        );

        log.info("Profile created successfully for userId={}", user.getUserId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/me")
    public ResponseEntity<ProfileResponseDto> getMyProfile(
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        log.info("Received get my profile request for userId={} role={}", user.getUserId(), user.getRole());

        ProfileResponseDto response = profileService.getProfileByUserId(user);

        log.info("Profile fetched successfully for userId={}", user.getUserId());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/me")
    public ResponseEntity<ProfileResponseDto> updateMyProfile(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody ProfileRequestDto requestDto
    ) {
        log.info("Received update profile request for userId={}", user.getUserId());

        ProfileResponseDto response = profileService.updateProfile(
                user,
                requestDto
        );

        log.info("Profile updated successfully for userId={}", user.getUserId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/internal/candidates/{userId}/preview")
    public ResponseEntity<CandidateProfilePreviewDto> getCandidateProfilePreview(
            @PathVariable Long userId
    ) {
        log.info("Received candidate profile preview request for userId={}", userId);

        CandidateProfilePreviewDto response = profileService.getCandidateProfilePreviewByUserId(userId);

        log.info("Candidate profile preview fetched successfully for userId={}", userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/resume/upload")
    public ResponseEntity<String> uploadResume(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        log.info("Received resume upload request for userId={}", user.getUserId());
        return ResponseEntity.ok(profileService.uploadResume(user, file));
    }

    @GetMapping("/resume/my")
    public ResponseEntity<byte[]> downloadMyResume(
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        log.info("Received own resume download request for userId={}", user.getUserId());

        Resume resume = profileService.getMyResume(user);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + resume.getFileName() + "\"")
                .contentType(MediaType.parseMediaType(resume.getContentType()))
                .body(resume.getFileData());
    }

    @GetMapping("/resume/recruiter/{candidateId}/{jobId}")
    public ResponseEntity<byte[]> downloadResumeForRecruiter(
            @PathVariable Long candidateId,
            @PathVariable Long jobId,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        log.info("Received recruiter resume download request for recruiterId={}, candidateId={}, jobId={}",
                user.getUserId(), candidateId, jobId);

        Resume resume = profileService.getResumeForRecruiter(user, candidateId, jobId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + resume.getFileName() + "\"")
                .contentType(MediaType.parseMediaType(resume.getContentType()))
                .body(resume.getFileData());
    }
}