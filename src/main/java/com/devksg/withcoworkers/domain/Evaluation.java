package com.devksg.withcoworker.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "evaluations")
@Getter
@NoArgsConstructor
public class Evaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "evaluation_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluator_id", nullable = false)
    private User evaluator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluatee_id", nullable = false)
    private User evaluatee;

    private String comment;

    @Column(name = "target_month", nullable = false)
    private LocalDate targetMonth;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder
    public Evaluation(User evaluator, User evaluatee, String comment, LocalDate targetMonth) {
        this.evaluator = evaluator;
        this.evaluatee = evaluatee;
        this.comment = comment;
        this.targetMonth = targetMonth;
    }
}
