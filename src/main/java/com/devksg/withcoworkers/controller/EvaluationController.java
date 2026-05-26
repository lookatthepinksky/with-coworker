package com.devksg.withcoworker.controller;

import com.devksg.withcoworker.domain.User;
import com.devksg.withcoworker.dto.EvaluationRequest;
import com.devksg.withcoworker.repository.EvaluationItemRepository;
import com.devksg.withcoworker.repository.UserRepository;
import com.devksg.withcoworker.service.EvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class EvaluationController {

    private final EvaluationService evaluationService;
    private final EvaluationItemRepository evaluationItemRepository;
    private final UserRepository userRepository;

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
    public ResponseEntity<Map<String, String>> getEvaluateTarget(@PathVariable Long id) {
        User target = userRepository.findById(id).orElseThrow();
        return ResponseEntity.ok(Map.of("name", target.getName()));
    }

    @PostMapping("/api/evaluations")
    public ResponseEntity<Void> submit(
        @AuthenticationPrincipal User user,
        @RequestBody EvaluationRequest request
    ) {
        evaluationService.submit(user, request);
        return ResponseEntity.ok().build();
    }
}
