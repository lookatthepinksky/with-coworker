package com.devksg.withcoworkers.config;

import io.micrometer.common.KeyValue;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationFilter;
import org.springframework.ai.chat.observation.ChatModelObservationContext;
import org.springframework.stereotype.Component;

/**
 * AI 호출 내용을 Langfuse(모니터링 도구)에서 볼 수 있도록 OTel 스팬에 추가하는 필터.
 *
 * Spring AI의 ChatModel이 AI를 호출할 때마다 이 필터가 자동으로 실행되어
 * "어떤 프롬프트를 보냈는지(gen_ai.prompt)"와 "AI가 뭐라고 답했는지(gen_ai.completion)"를
 * 트레이싱 데이터에 끼워 넣는다.
 */
@Component
public class ChatModelCompletionContentObservationFilter implements ObservationFilter {

    /**
     * AI 호출 이벤트가 발생할 때마다 호출되는 메서드.
     * ChatModel 호출이 아닌 경우(HTTP 요청 등) 그냥 통과시키고,
     * ChatModel 호출인 경우에만 프롬프트/응답 내용을 스팬에 추가한다.
     */
    @Override
    public Observation.Context map(Observation.Context context) {
        // ChatModel 호출이 아니면 그냥 통과
        if (!(context instanceof ChatModelObservationContext chatModelContext)) {
            return context;
        }

        // AI에게 보낸 프롬프트(질문)를 스팬에 추가
        if (chatModelContext.getRequest() != null) {
            var instructions = chatModelContext.getRequest().getInstructions();
            if (instructions != null && !instructions.isEmpty()) {
                chatModelContext.addHighCardinalityKeyValue(
                        KeyValue.of("gen_ai.prompt", instructions.toString()));
            }
        }

        // AI가 돌려준 응답을 스팬에 추가
        if (chatModelContext.getResponse() != null) {
            var results = chatModelContext.getResponse().getResults();
            if (results != null && !results.isEmpty()) {
                chatModelContext.addHighCardinalityKeyValue(
                        KeyValue.of("gen_ai.completion", results.toString()));
            }
        }

        return chatModelContext;
    }
}
