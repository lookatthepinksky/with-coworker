package com.devksg.withcoworkers.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "AI_USING_COUNT")
@Getter
@NoArgsConstructor
public class AiUsingCount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "evaluator_id", nullable = false)
    private Long evaluatorId;

    @Column(name = "evaluatee_id", nullable = false)
    private Long evaluateeId;

    @Column(name = "target_month", nullable = false)
    private LocalDate targetMonth;

    @Column(name = "comment")
    private String comment;

    @Column(name = "ai_review_count", nullable = false)
    private int aiReviewCount = 0;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    public AiUsingCount(Long evaluatorId, Long evaluateeId, LocalDate targetMonth) {
        this.evaluatorId = evaluatorId;
        this.evaluateeId = evaluateeId;
        this.targetMonth = targetMonth;
        this.aiReviewCount = 0;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void incrementCount(String latestComment) {
        this.aiReviewCount++;
        this.comment = latestComment;
        this.updatedAt = LocalDateTime.now();
    }
}
