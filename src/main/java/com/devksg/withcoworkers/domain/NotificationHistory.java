package com.devksg.withcoworkers.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification_history")
@Getter
@NoArgsConstructor
public class NotificationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "provider_id")
    private Long providerId;

    @Column(name = "recipient_email", nullable = false, length = 255)
    private String recipientEmail;

    @Column(name = "notification_type", nullable = false, length = 50)
    private String notificationType;

    @Column(name = "subject", nullable = false, length = 500)
    private String subject;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;

    @Builder
    public NotificationHistory(Long providerId, String recipientEmail, String notificationType,
                               String subject, String status, String errorMessage) {
        this.providerId = providerId;
        this.recipientEmail = recipientEmail;
        this.notificationType = notificationType;
        this.subject = subject;
        this.status = status;
        this.errorMessage = errorMessage;
        this.sentAt = LocalDateTime.now();
    }
}
