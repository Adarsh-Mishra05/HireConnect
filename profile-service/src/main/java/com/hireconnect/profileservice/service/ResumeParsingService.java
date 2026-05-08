package com.hireconnect.profileservice.service;

import java.util.List;

public interface ResumeParsingService {
    ParsedResumeDetails parsePdf(byte[] pdfBytes);

    record ParsedResumeDetails(
            String name,
            String email,
            String phone,
            List<String> skills
    ) {}
}
