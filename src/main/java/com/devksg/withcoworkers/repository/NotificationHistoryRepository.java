package com.devksg.withcoworker.repository;

import com.devksg.withcoworker.domain.NotificationHistory;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface NotificationHistoryRepository extends JpaRepository<NotificationHistory, Long> {

    // 이번 달 동일 타입 발송 성공 이력 존재 여부 (중복 발송 방지)
    @Query("SELECT CASE WHEN COUNT(n) > 0 THEN true ELSE false END FROM NotificationHistory n " +
           "WHERE n.recipientEmail = :email AND n.notificationType = :type " +
           "AND n.status = 'SUCCESS' AND n.sentAt >= :monthStart")
    boolean existsSuccessThisMonth(@Param("email") String email,
                                   @Param("type") String type,
                                   @Param("monthStart") LocalDateTime monthStart);
}
