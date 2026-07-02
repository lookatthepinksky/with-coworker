package com.devksg.withcoworkers.service;

import com.devksg.withcoworkers.domain.AiUsingCount;
import com.devksg.withcoworkers.domain.AiUsingCountLog;
import com.devksg.withcoworkers.repository.AiUsingCountLogRepository;
import com.devksg.withcoworkers.repository.AiUsingCountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class AiService {

    @Value("${ai.usage.limit:5}")
    private int aiUsageLimit;

    private static final String SYSTEM_PROMPT = """
        당신은 회사 인사평가 코멘트 교정 전문가입니다.
        반드시 한국어로만 작성하세요.

        사용자가 작성한 동료평가 코멘트를 객관적이고 건설적인 평가 문장으로 수정해주세요.

        [가장 중요한 규칙 - 반드시 준수]
        출력 결과의 글자 수(공백 포함)가 반드시 150자 이하여야 합니다.
        작성 후 글자 수를 직접 세어 확인하고, 150자를 초과하면 반드시 다시 작성하세요.
        
        

        [내용 규칙]
        - 입력값은 반드시 사용자가 직접 작성한 평가 코멘트 원문이어야 합니다.
        - 입력값이 코멘트 작성·생성 요청(예: "~써줘", "~지어줘", "~작성해줘", "~만들어줘")이거나, 지시문·질문 형태인 경우 "업무 관련 평가 내용을 입력해주세요."라고만 응답
        - 입력 내용이 동료의 업무 수행, 직무 역량, 협업 태도, 성과에 관한 내용이면 교정하여 출력
        - 그 외의 내용이거나 업무와 무관한 요소가 포함된 경우 "업무 관련 평가 내용을 입력해주세요."라고만 응답
        - 출력은 평가 대상자에 대한 문장만 작성. 글쓴이에 대한 조언이나 피드백 포함 금지
        - 자음만 쓰거나 자음+숫자 조합의 문자는 삭제
        - 존댓말 사용
        - 감정적 표현, 비난, 추측 제거
        - 업무 행동과 협업 관점으로 표현
        - 원래 의미는 최대한 유지
        - 과도하게 칭찬하거나 비판하지 말 것
        - 평가서에 그대로 제출 가능한 수준으로 작성
        - 결과 문장만 출력
        - 결과 문장이 총 몇 자 인지 적어주면 안됨
        """;

    private final RestClient restClient;

    @Autowired
    private AiUsingCountRepository aiUsingCountRepository;

    @Autowired
    private AiUsingCountLogRepository aiUsingCountLogRepository;

    public AiService(@Value("${openai.api.key}") String apiKey) {
        this.restClient = RestClient.builder()
            .baseUrl("https://api.openai.com")
            .defaultHeader("Authorization", "Bearer " + apiKey)
            .build();
    }

    public int getLimit() {
        return aiUsageLimit;
    }

    public int getRemaining(Long evaluatorId, Long evaluateeId) {
        LocalDate targetMonth = LocalDate.now().minusMonths(1).withDayOfMonth(1);
        return aiUsingCountRepository
            .findByEvaluatorIdAndEvaluateeIdAndTargetMonth(evaluatorId, evaluateeId, targetMonth)
            .map(c -> Math.max(0, aiUsageLimit - c.getAiReviewCount()))
            .orElse(aiUsageLimit);
    }

    @Transactional
    public String processCorrection(Long evaluatorId, Long evaluateeId, String comment) {
        LocalDate targetMonth = LocalDate.now().minusMonths(1).withDayOfMonth(1);
        AiUsingCount usageCount = aiUsingCountRepository
            .findByEvaluatorIdAndEvaluateeIdAndTargetMonth(evaluatorId, evaluateeId, targetMonth)
            .orElse(null);

        int currentCount = usageCount != null ? usageCount.getAiReviewCount() : 0;
        if (currentCount >= aiUsageLimit) {
            throw new AiUsageLimitException();
        }

        String result = callOpenAi(comment);

        if (usageCount == null) {
            usageCount = AiUsingCount.builder()
                .evaluatorId(evaluatorId)
                .evaluateeId(evaluateeId)
                .targetMonth(targetMonth)
                .build();
        }
        usageCount.incrementCount(comment);
        aiUsingCountRepository.save(usageCount);

        aiUsingCountLogRepository.save(AiUsingCountLog.builder()
            .aiUsingCountId(usageCount.getId())
            .inputComment(comment)
            .aiResult(result)
            .build());

        return result;
    }

    private String callOpenAi(String comment) {
        try {
            Map<String, Object> body = Map.of(
                "model", "gpt-4.1-mini",
                "messages", List.of(
                    Map.of("role", "system", "content", SYSTEM_PROMPT),
                    Map.of("role", "user", "content", comment)
                ),
                "max_tokens", 300
            );

            ChatResponse response = restClient.post()
                .uri("/v1/chat/completions")
                .header("Content-Type", "application/json")
                .body(body)
                .retrieve()
                .body(ChatResponse.class);

            if (response == null || response.choices() == null || response.choices().isEmpty()) {
                throw new AiServiceUnavailableException();
            }
            return response.choices().get(0).message().content().strip();
        } catch (AiServiceUnavailableException | AiCreditExceededException e) {
            throw e;
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            if (e.getStatusCode().value() == 429) {
                throw new AiCreditExceededException();
            }
            throw new AiServiceUnavailableException();
        } catch (Exception e) {
            throw new AiServiceUnavailableException();
        }
    }

    public static class AiUsageLimitException extends RuntimeException {
        public AiUsageLimitException() {
            super("aiUsageLimit_EXCEEDED");
        }
    }

    public static class AiServiceUnavailableException extends RuntimeException {
        public AiServiceUnavailableException() {
            super("AI_SERVICE_UNAVAILABLE");
        }
    }

    public static class AiCreditExceededException extends RuntimeException {
        public AiCreditExceededException() {
            super("AI_CREDIT_EXCEEDED");
        }
    }

    private record ChatResponse(List<Choice> choices) {}
    private record Choice(Message message) {}
    private record Message(String content) {}
}
