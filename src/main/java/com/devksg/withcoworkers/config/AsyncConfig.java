package com.devksg.withcoworkers.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

@Configuration
@EnableAsync
@EnableScheduling
public class AsyncConfig implements SchedulingConfigurer {

    /**
     * 스케줄러 스레드 풀 - SQS 폴링(long-poll)과 cron 작업이 서로 블로킹하지 않도록 멀티스레드 구성
     */
    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {//"스프링아, 기본 싱글 스레드 말고 이 스레드 풀 5개짜리로 스케줄 작업 돌려줘"
        // ThreadPoolTaskScheduler : 시간에 맞춰 작업을 실행시키는 클래스 ,"언제" 실행할지 안다
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(5); // 스케줄러용 스레드 5개
        scheduler.setThreadNamePrefix("scheduler-");
        scheduler.initialize();
        taskRegistrar.setTaskScheduler(scheduler);
    }

    /**
     * 이메일 비동기 처리용 스레드 풀
     */
    @Bean(name = "emailTaskExecutor")
    public ThreadPoolTaskExecutor emailTaskExecutor() {
        //ThreadPoolTaskExecutor그냥 던져진 작업을 즉시 실행하는 클래스"언제" 모름. 받으면 바로 실행
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5); // 평소 유지 스레드
        executor.setMaxPoolSize(20); // 최대 스레드
        executor.setQueueCapacity(100); // 대기열
        executor.setThreadNamePrefix("email-");
        executor.initialize();
        return executor;
    }

    /*ThreadPoolTaskExecutor는 3단계로 동작.
    요청 들어옴
              ▼
              1단계: corePoolSize(5) 이하? → 스레드 바로 생성
              ▼
              2단계: 5개 다 차면? → queueCapacity(100) 대기열에 쌓음
              ▼
              3단계: 대기열도 꽉 차면? → maxPoolSize(20)까지 스레드 추가 생성
              ▼
              4단계: 20개도 꽉 차면? → 요청 거절 (RejectedExecutionException)
    즉 평소엔 5개로 처리하다가, 이메일이 폭발적으로 몰릴 때만 최대 20개까지 늘어나는 구조.*/

}
