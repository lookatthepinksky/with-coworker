package com.devksg.withcoworkers.controller;

import com.devksg.withcoworkers.domain.User;
import com.devksg.withcoworkers.dto.EvaluationRequest;
import com.devksg.withcoworkers.repository.EvaluationItemRepository;
import com.devksg.withcoworkers.service.EvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class EvaluationController {

    private final EvaluationService evaluationService;
    private final EvaluationItemRepository evaluationItemRepository;

    @GetMapping("/api/evaluation-items")
    public ResponseEntity<List<Map<String, Object>>> getItems() {
        List<Map<String, Object>> items = evaluationItemRepository.findAll().stream()
            .map(item -> Map.<String, Object>of(
                "id", item.getId(),
                "label", item.getLabel(),
                "description", item.getDescription() != null ? item.getDescription() : ""
            ))
            .toList();
        return ResponseEntity.ok(items);
    }

    @GetMapping("/api/evaluate/{id}")
    public ResponseEntity<Map<String, String>> getEvaluateTarget(
        @AuthenticationPrincipal User user,
        @PathVariable Long id
    ) {
        return ResponseEntity.ok(evaluationService.getEvaluateTarget(user, id));
    }

    @PostMapping("/api/evaluations")
    public ResponseEntity<Void> submit(
        @AuthenticationPrincipal User user,
        @Valid @RequestBody EvaluationRequest request
    ) {
        evaluationService.submit(user, request);
        return ResponseEntity.ok().build();
    }
}
