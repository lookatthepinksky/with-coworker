package com.devksg.withcoworkers.controller;

import com.devksg.withcoworkers.domain.User;
import com.devksg.withcoworkers.service.AiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    @GetMapping("/api/ai/usage")
    public ResponseEntity<Map<String, Integer>> getUsage(
        @AuthenticationPrincipal User user,
        @RequestParam Long evaluateeId
    ) {
        int remaining = aiService.getRemaining(user.getId(), evaluateeId);
        return ResponseEntity.ok(Map.of("remaining", remaining, "limit", aiService.getLimit()));
    }

    @PostMapping("/api/ai/correct-comment")
    public ResponseEntity<Map<String, String>> correctComment(
        @AuthenticationPrincipal User user,
        @RequestBody Map<String, String> body
    ) {
        String comment = body.get("comment");
        if (comment == null || comment.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        if (comment.trim().length() > 150) {
            return ResponseEntity.badRequest().body(Map.of("error", "TOO_LONG"));
        }

        Long evaluateeId = Long.parseLong(body.get("evaluateeId"));

        try {
            String result = aiService.processCorrection(user.getId(), evaluateeId, comment);
            boolean rejected = result.contains("업무 관련 평가 내용을 입력해주세요");
            return ResponseEntity.ok(Map.of("result", result, "rejected", String.valueOf(rejected)));
        } catch (AiService.AiUsageLimitException e) {
            return ResponseEntity.status(429).body(Map.of("error", "LIMIT_EXCEEDED"));
        } catch (AiService.AiServiceUnavailableException e) {
            return ResponseEntity.status(503).body(Map.of("error", "SERVICE_UNAVAILABLE"));
        } catch (AiService.AiCreditExceededException e) {
            return ResponseEntity.status(503).body(Map.of("error", "CREDIT_EXCEEDED"));
        }
    }
}
