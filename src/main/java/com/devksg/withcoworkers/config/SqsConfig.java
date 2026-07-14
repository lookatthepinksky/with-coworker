package com.devksg.withcoworkers.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.SqsClientBuilder;

import java.net.URI;

@Configuration
@RequiredArgsConstructor
public class SqsConfig {

    private final SqsProperties props;

    @Bean
    public SqsClient sqsClient() {
        SqsClientBuilder builder = SqsClient.builder()
                .region(Region.of(props.getRegion()));

        // 키가 둘 다 있을 때만 static credentials (로컬/LocalStack)
        // 없으면 이 블록을 건너뛰어 SDK 기본 체인(EC2 IAM Role)이 자동 적용됨
        if (StringUtils.hasText(props.getAccessKey()) //빈 키를 넣지 않게 막아서, SDK가 Role로 넘어갈 길을 열어줌
                && StringUtils.hasText(props.getSecretKey())) {
            builder.credentialsProvider(
                    StaticCredentialsProvider.create(
                            AwsBasicCredentials.create(
                                    props.getAccessKey(), props.getSecretKey())
                    )
            );
        }

        // 로컬(LocalStack)일 때만 endpoint override 적용
        if (StringUtils.hasText(props.getEndpointOverride())) {
            builder.endpointOverride(URI.create(props.getEndpointOverride()));
        }

        return builder.build();
    }
}