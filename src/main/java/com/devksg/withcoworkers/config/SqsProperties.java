package com.devksg.withcoworkers.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.sqs")
@Getter
@Setter
public class SqsProperties {
    private String queueName = "evaluation-email-queue";
    private String region = "ap-northeast-2";
    private String endpointOverride;
    private String accessKey;
    private String secretKey;
}
