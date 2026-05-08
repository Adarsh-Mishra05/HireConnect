package com.hireconnect.applicationservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import com.hireconnect.applicationservice.client.dto.CandidateProfilePreviewDto;
import com.hireconnect.applicationservice.client.dto.ProfileResponseDto;

@FeignClient(name = "profile-service")
public interface ProfileServiceClient {

    @GetMapping("/api/profiles/me")
    ProfileResponseDto getMyProfile();

    @GetMapping("/api/profiles/internal/candidates/{userId}/preview")
    CandidateProfilePreviewDto getCandidateProfilePreview(
            @RequestHeader("X-Auth-User-Id") String requesterUserId,
            @RequestHeader("X-Auth-User-Email") String requesterEmail,
            @RequestHeader("X-Auth-User-Role") String requesterRole,
            @PathVariable Long userId
    );
}
