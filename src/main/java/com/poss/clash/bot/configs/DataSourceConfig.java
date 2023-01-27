package com.poss.clash.bot.configs;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.socialsignin.spring.data.dynamodb.repository.config.EnableDynamoDBRepositories;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClientBuilder;

import java.net.URI;

@Slf4j
@Configuration
@EnableConfigurationProperties({AwsConfig.class})
@EnableDynamoDBRepositories("com.poss.clash.bot.daos")
public class DataSourceConfig {

    @Bean
    public AwsBasicCredentials awsBasicCreds(AwsConfig awsConfig) {
        log.info("AwsConfig {}", awsConfig);
        return AwsBasicCredentials.create(awsConfig.getAccessKey(), awsConfig.getSecretKey());
    }

    @Bean
    public AmazonDynamoDB amazonDynamoDB(AwsBasicCredentials awsBasicCredentials, AwsConfig awsConfig) {
        log.info("DynamoDbConfig {}", awsConfig.getDynamoDb());
        return AmazonDynamoDBClientBuilder
        .standard()
        .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(awsConfig.getDynamoDb().getEndpoint(), "us-east-1"))
        .build();
    }
}
