package com.devksg.withcoworkers.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProviderChain;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
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

        // 1. 기본적으로 EC2 IAM Role 등 표준 자격 증명을 찾는 프로바이더를 기반으로 둡니다.
        DefaultCredentialsProvider defaultProvider = DefaultCredentialsProvider.create();

        if (StringUtils.hasText(props.getAccessKey())
                && StringUtils.hasText(props.getSecretKey())
                && !props.getAccessKey().startsWith("${")) { // 플레이스홀더 주입 실패 방어 코드 추가

            // 2. 만약 설정에 실제 키 값이 명시되어 있다면 해당 키를 우선(1순위)으로 사용하고,
            // 실패 시 DefaultCredentialsProvider(IAM Role 등)로 넘어가도록 체인을 구성합니다.
            StaticCredentialsProvider staticProvider = StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(props.getAccessKey(), props.getSecretKey())
            );

            builder.credentialsProvider(
                    AwsCredentialsProviderChain.builder()
                            .addCredentialsProvider(staticProvider)
                            .addCredentialsProvider(defaultProvider)
                            .build()
            );
        } else {
            // 3. 키가 아예 없다면 깔끔하게 기본 체인(EC2 IAM Role)만 사용합니다.
            builder.credentialsProvider(defaultProvider);
        }

        // 로컬(LocalStack)일 때만 endpoint override 적용
        if (StringUtils.hasText(props.getEndpointOverride())) {
            builder.endpointOverride(URI.create(props.getEndpointOverride()));
        }

        return builder.build();
    }
}