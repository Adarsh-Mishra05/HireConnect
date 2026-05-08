package com.hireconnect.profileservice.service.impl;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import com.hireconnect.profileservice.service.ResumeParsingService;

@Service
public class ResumeParsingServiceImpl implements ResumeParsingService {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\\b");
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("\\b(?:\\+?\\d{1,3}[-\\s]?)?(?:\\(?\\d{2,4}\\)?[-\\s]?)?\\d{3,5}[-\\s]?\\d{4,6}\\b");

    private static final List<String> COMMON_SKILLS = List.of(
            "java", "spring boot", "spring", "hibernate", "mysql", "postgresql", "mongodb",
            "javascript", "typescript", "angular", "react", "node", "python", "django",
            "kafka", "docker", "kubernetes", "aws", "azure", "git", "rest api", "microservices"
    );

    @Override
    public ParsedResumeDetails parsePdf(byte[] pdfBytes) {
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            String normalized = text == null ? "" : text.trim();

            String email = findFirstMatch(EMAIL_PATTERN, normalized);
            String phone = findFirstMatch(PHONE_PATTERN, normalized);
            String name = extractLikelyName(normalized, email);
            List<String> skills = extractSkills(normalized);

            return new ParsedResumeDetails(name, email, phone, skills);
        } catch (Exception ex) {
            return new ParsedResumeDetails(null, null, null, List.of());
        }
    }

    private String findFirstMatch(Pattern pattern, String text) {
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group().trim();
        }
        return null;
    }

    private String extractLikelyName(String text, String email) {
        if (text.isBlank()) {
            return null;
        }

        List<String> lines = Arrays.stream(text.split("\\R"))
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .limit(8)
                .toList();

        for (String line : lines) {
            String lower = line.toLowerCase(Locale.ROOT);
            if (email != null && lower.contains(email.toLowerCase(Locale.ROOT))) {
                continue;
            }
            if (lower.contains("@") || lower.contains("resume") || lower.contains("curriculum vitae")) {
                continue;
            }
            if (line.length() >= 2 && line.length() <= 50 && line.matches("[A-Za-z][A-Za-z .'-]{1,49}")) {
                return line;
            }
        }
        return null;
    }

    private List<String> extractSkills(String text) {
        String lower = text == null ? "" : text.toLowerCase(Locale.ROOT);
        Set<String> found = new LinkedHashSet<>();

        for (String skill : COMMON_SKILLS) {
            if (lower.contains(skill.toLowerCase(Locale.ROOT))) {
                found.add(skill);
            }
        }

        return found.stream().limit(12).toList();
    }
}
