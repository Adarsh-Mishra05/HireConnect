package com.hireconnect.auth.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 500)
    private String token;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "userId", nullable = false, unique = true)
    private UserCredential user;

    @Column(nullable = false)
    private LocalDateTime expiryDate;
}