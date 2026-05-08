package com.hireconnect.profileservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "recruiter_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecruiterDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(name = "website")
    private String website;

    @Column(name = "company_description", columnDefinition = "TEXT")
    private String companyDescription;

    @Column(name = "designation")
    private String designation;

    @OneToOne
    @JoinColumn(name = "profile_id", nullable = false, unique = true)
    private Profile profile;
}