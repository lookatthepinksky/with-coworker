package com.devksg.withcoworker.notification;

import com.devksg.withcoworker.config.SqsProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;

@Service           // 스프링이 이 클래스를 서비스 빈으로 등록 → 다른 클래스에서 주입해서 쓸 수 있음
@RequiredArgsConstructor  // final 필드를 파라미터로 받는 생성자를 Lombok이 자동 생성
@Slf4j             // log.info(), log.error() 등 로그 객체를 자동으로 만들어줌
public class SqsProducerService {

    private final SqsClient sqsClient;         // AWS SQS와 실제로 통신하는 클라이언트 객체
    private final SqsProperties sqsProperties;  // application.properties에서 읽어온 SQS 설정값 (큐 이름, 리전 등)
    private final ObjectMapper objectMapper;    // 자바 객체 ↔ JSON 변환 담당

    private volatile String queueUrl;  // SQS 큐의 실제 URL. volatile = 멀티스레드 환경에서 항상 최신값을 읽도록 보장

    // 외부에서 호출하는 메서드 - EmailMessage 객체를 받아서 SQS 큐에 넣음
    public void sendMessage(EmailMessage message) {
        try {
            // EmailMessage 자바 객체를 JSON 문자열로 변환 (SQS는 문자열만 전송 가능)
            // 예: {"type":"START","userEmail":"abc@gmail.com","userName":"홍길동", ...}
            String body = objectMapper.writeValueAsString(message);

            // SQS에 메시지 전송
            // r -> r.queueUrl(...).messageBody(...) 는 전송 옵션을 람다로 설정하는 AWS SDK 방식
            sqsClient.sendMessage(r -> r
                    .queueUrl(getQueueUrl())  // 어느 큐에 넣을지 URL 지정
                    .messageBody(body));       // 넣을 내용(JSON 문자열)

            log.debug("[SQS전송] type={} to={}", message.type(), message.userEmail());

        } catch (JsonProcessingException e) {
            // JSON 변환 자체가 실패한 경우 (거의 발생 안 함)
            log.error("[SQS전송실패] 직렬화 오류: {}", e.getMessage());
            throw new RuntimeException("SQS 메시지 직렬화 실패", e);
        }
    }

    // SQS 큐의 URL을 가져오는 메서드
    // 큐 이름(evaluation-email-queue)으로 AWS에 물어봐서 실제 URL을 받아옴
    private String getQueueUrl() {
        if (queueUrl == null) {                    // URL을 아직 한 번도 안 가져온 경우에만 진입
            synchronized (this) {                  // 멀티스레드 동시 진입 방지 - 한 번에 한 스레드만 실행
                if (queueUrl == null) {            // synchronized 안에서 한 번 더 체크 (다른 스레드가 먼저 세팅했을 수도 있으니)
                    queueUrl = sqsClient
                            .getQueueUrl(r -> r.queueName(sqsProperties.getQueueName()))  // 큐 이름으로 URL 조회
                            .queueUrl();           // 응답에서 URL 문자열 추출
                    log.info("[SQS] Queue URL 초기화: {}", queueUrl);
                }
            }
        }
        return queueUrl;  // 이미 가져온 URL이 있으면 그걸 바로 반환 (매번 AWS에 물어보지 않음)
    }
}
