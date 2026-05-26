package com.devksg.withcoworker.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "evaluation_scores")
@Getter
@NoArgsConstructor
public class EvaluationScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "score_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluation_id", nullable = false)
    private Evaluation evaluation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private EvaluationItem item;

    @Column(nullable = false)
    private int score;

    @Builder
    public EvaluationScore(Evaluation evaluation, EvaluationItem item, int score) {
        this.evaluation = evaluation;
        this.item = item;
        this.score = score;
    }
}
