package com.devksg.withcoworkers.service;

import com.devksg.withcoworkers.domain.AiUsingCount;
import com.devksg.withcoworkers.domain.AiUsingCountLog;
import com.devksg.withcoworkers.repository.AiUsingCountLogRepository;
import com.devksg.withcoworkers.repository.AiUsingCountRepository;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.errors.APIConnectionException;
import com.openai.errors.AuthenticationException;
import com.openai.errors.OpenAIException;
import com.openai.errors.RateLimitException;
import com.openai.models.ChatCompletionCreateParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;

@Service
public class AiService {

    private static final Logger log = LoggerFactory.getLogger(AiService.class);

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

    private final OpenAIClient openAIClient;

    @Autowired
    private AiUsingCountRepository aiUsingCountRepository;

    @Autowired
    private AiUsingCountLogRepository aiUsingCountLogRepository;

    public AiService(@Value("${openai.api.key}") String apiKey) {
        this.openAIClient = OpenAIOkHttpClient.builder()
            .apiKey(apiKey)
            .connectTimeout(Duration.ofSeconds(5))
            .readTimeout(Duration.ofSeconds(15))
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
            var completion = openAIClient.chat().completions().create(
                ChatCompletionCreateParams.builder()
                    .model("gpt-4.1-mini")
                    .addSystemMessage(SYSTEM_PROMPT)
                    .addUserMessage(comment)
                    .maxTokens(300)
                    .build()
            );

            String content = completion.choices().get(0).message().content().orElse(null);
            if (content == null || content.isBlank()) {
                throw new AiServiceUnavailableException();
            }
            return content.strip();
        } catch (AiServiceUnavailableException | AiCreditExceededException | AiTimeoutException | AiAuthException e) {
            throw e;
        } catch (RateLimitException e) {
            // OpenAI 계정 크레딧 소진 (429)
            throw new AiCreditExceededException();
        } catch (AuthenticationException e) {
            // API 키 인증 실패 (401) - 운영자 확인 필요
            log.error("[AI AUTH ERROR] OpenAI API 키 인증 실패. 키 만료 또는 잘못된 키 확인 필요. status=401");
            throw new AiAuthException();
        } catch (APIConnectionException e) {
            // 타임아웃 또는 네트워크 단절
            throw new AiTimeoutException();
        } catch (OpenAIException e) {
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

    public static class AiTimeoutException extends RuntimeException {
        public AiTimeoutException() {
            super("AI_TIMEOUT");
        }
    }

    public static class AiAuthException extends RuntimeException {
        public AiAuthException() {
            super("AI_AUTH_ERROR");
        }
    }
}
