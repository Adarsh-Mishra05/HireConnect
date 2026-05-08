package com.hireconnect.profileservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "resumes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Resume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Column(name = "file_size")
    private Long fileSize;

    @Lob
    @Column(name = "file_data", columnDefinition = "LONGBLOB", nullable = false)
    private byte[] fileData;

    @Column(name = "file_url")
    private String fileUrl;

    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;

    @Column(name = "extracted_name")
    private String extractedName;

    @Column(name = "extracted_email")
    private String extractedEmail;

    @Column(name = "extracted_phone")
    private String extractedPhone;

    @Column(name = "extracted_skills", columnDefinition = "TEXT")
    private String extractedSkills;

    @OneToOne
    @JoinColumn(name = "profile_id", nullable = false, unique = true)
    private Profile profile;

    @PrePersist
    public void onUpload() {
        this.uploadedAt = LocalDateTime.now();
    }
}
