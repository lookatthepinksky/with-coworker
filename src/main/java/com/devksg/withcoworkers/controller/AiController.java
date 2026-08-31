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

        try {
            Long evaluateeId = Long.parseLong(body.get("evaluateeId"));
            String result = aiService.processCorrection(user.getId(), evaluateeId, comment);
            boolean rejected = result.contains("업무 관련 평가 내용을 입력해주세요");
            return ResponseEntity.ok(Map.of("result", result, "rejected", String.valueOf(rejected)));
        } catch (NumberFormatException e) {
            // evaluateeId가 누락됐거나 숫자가 아닌 값이 들어온 경우
            return ResponseEntity.badRequest().body(Map.of("error", "INVALID_EVALUATEE_ID"));
        } catch (AiService.AiUsageLimitException e) {
            // 이번 달 AI 사용 횟수 초과
            return ResponseEntity.status(429).body(Map.of("error", "LIMIT_EXCEEDED"));
        } catch (AiService.AiTimeoutException e) {
            // OpenAI 서버 응답 지연 (연결 5초, 읽기 15초 초과)
            return ResponseEntity.status(504).body(Map.of("error", "TIMEOUT"));
        } catch (AiService.AiAuthException e) {
            // OpenAI API 키 인증 실패 (401) - 운영자 확인 필요, 유저에겐 서비스 불가로 표시
            return ResponseEntity.status(503).body(Map.of("error", "SERVICE_UNAVAILABLE"));
        } catch (AiService.AiServiceUnavailableException e) {
            // OpenAI 서버 오류 또는 응답 파싱 실패
            return ResponseEntity.status(503).body(Map.of("error", "SERVICE_UNAVAILABLE"));
        } catch (AiService.AiCreditExceededException e) {
            // OpenAI 계정 크레딧 소진 (429) - 충전 필요
            return ResponseEntity.status(503).body(Map.of("error", "CREDIT_EXCEEDED"));
        }
    }
}
