package com.hireconnect.profileservice.service.impl;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.hireconnect.profileservice.service.ResumeParsingService.ParsedResumeDetails;

class ResumeParsingServiceImplTest {

    private ResumeParsingServiceImpl resumeParsingService;

    @BeforeEach
    void setUp() {
        resumeParsingService = new ResumeParsingServiceImpl();
    }

    @Test
    void parsePdf_InvalidBytes_ReturnsEmptyDetails() {
        byte[] invalidPdf = "Not a PDF".getBytes();
        ParsedResumeDetails details = resumeParsingService.parsePdf(invalidPdf);

        assertNotNull(details);
        assertNull(details.name());
        assertNull(details.email());
        assertNull(details.phone());
        assertTrue(details.skills().isEmpty());
    }
}
