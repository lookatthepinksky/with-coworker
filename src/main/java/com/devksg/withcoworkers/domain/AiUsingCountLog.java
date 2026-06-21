package com.devksg.withcoworkers.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "AI_USING_COUNT_LOG")
@Getter
@NoArgsConstructor
public class AiUsingCountLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ai_using_count_id", nullable = false)
    private Long aiUsingCountId;

    @Column(name = "input_comment", nullable = false)
    private String inputComment;

    @Column(name = "ai_result", nullable = false)
    private String aiResult;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public AiUsingCountLog(Long aiUsingCountId, String inputComment, String aiResult) {
        this.aiUsingCountId = aiUsingCountId;
        this.inputComment = inputComment;
        this.aiResult = aiResult;
        this.createdAt = LocalDateTime.now();
    }
}
