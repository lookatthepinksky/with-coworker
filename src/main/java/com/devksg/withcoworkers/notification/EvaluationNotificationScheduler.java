package com.devksg.withcoworkers.notification;

import com.devksg.withcoworkers.domain.User;
import com.devksg.withcoworkers.repository.AuthProviderRepository;
import com.devksg.withcoworkers.repository.EvaluationRepository;
import com.devksg.withcoworkers.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class EvaluationNotificationScheduler {

    private final UserRepository userRepository;
    private final EvaluationRepository evaluationRepository;
    private final AuthProviderRepository authProviderRepository;
    private final SqsProducerService sqsProducerService;

    // 매월 1일 오전 9시 - 평가 시작 안내

    @Scheduled(cron = "0 0 9 1 * *", zone = "Asia/Seoul")
    public void sendStartNotification() {
        sendNotifications(EmailNotificationType.START);
    }

    // 매월 5일 오전 9시 - 마감 2일 전 안내
    @Scheduled(cron = "0 0 9 5 * *", zone = "Asia/Seoul")
    public void sendReminderNotification() {
        sendNotifications(EmailNotificationType.REMINDER);
    }

    // 매월 7일 오전 9시 - 마감 당일 안내
    @Scheduled(cron = "0 0 9 7 * *", zone = "Asia/Seoul")
    public void sendDeadlineNotification() {
        sendNotifications(EmailNotificationType.DEADLINE);
    }

    private void sendNotifications(EmailNotificationType type) {
        LocalDate prevMonth = LocalDate.now().minusMonths(1);
        LocalDate targetMonth = prevMonth.withDayOfMonth(1);
        int evaluationYear = prevMonth.getYear();
        int evaluationMonth = prevMonth.getMonthValue();

        //개발
        List<User> users = userRepository.findAll().stream()
                .filter(u -> "kimshinejade@gmail.com".equals(u.getEmail()))
                .toList();
        //운영
        //List<User> users = (type == EmailNotificationType.START)
        //        ? userRepository.findAll()
        //        : evaluationRepository.findIncompleteEvaluators(targetMonth);

        log.info("[알림스케줄러] type={} 대상={}년{}월 사용자={}명 SQS 전송 시작",
                type, evaluationYear, evaluationMonth, users.size());

        users.forEach(user -> {
            Long authProviderId = authProviderRepository.findFirstByUser(user)
                    .map(ap -> ap.getId())
                    .orElse(null);

            EmailMessage message = new EmailMessage(
                    type,
                    user.getEmail(),
                    user.getName(),
                    evaluationYear,
                    evaluationMonth,
                    authProviderId
            );
            sqsProducerService.sendMessage(message);
        });

        log.info("[알림스케줄러] type={} SQS 메시지 {}건 전송 완료", type, users.size());
    }
}
