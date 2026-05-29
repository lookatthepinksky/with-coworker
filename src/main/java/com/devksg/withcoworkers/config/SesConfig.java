package com.devksg.withcoworkers.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.SesClientBuilder;

import java.net.URI;

@Configuration
@RequiredArgsConstructor
public class SesConfig {

    private final SqsProperties props; // LocalStack endpoint/credentials 재사용 (동일 포트)

    @Bean
    public SesClient sesClient() {
        SesClientBuilder builder = SesClient.builder()
                .region(Region.of(props.getRegion()))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(props.getAccessKey(), props.getSecretKey())
                        )
                );

        if (StringUtils.hasText(props.getEndpointOverride())) {
            builder.endpointOverride(URI.create(props.getEndpointOverride()));
        }

        return builder.build();
    }
}
