package com.devksg.withcoworkers.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(unique = true)
    private String email;

    @Column(nullable = false)
    private String name;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public User(String email, String name) {
        this.email = email;
        this.name = name;
        this.createdAt = LocalDateTime.now();
    }

    public User(Long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }
}
