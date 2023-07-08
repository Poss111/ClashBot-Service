package com.poss.clash.bot.configs;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.kinesis.AmazonKinesisAsync;
import com.amazonaws.services.kinesis.AmazonKinesisAsyncClientBuilder;
import com.poss.clash.bot.configs.properties.AWSEndpointConfiguration;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@AllArgsConstructor
@Configuration
@Slf4j
public class AWSConfiguration {

  private final AWSEndpointConfiguration awsEndpointConfiguration;

  @Bean
  @Profile("local || k8s")
  AmazonKinesisAsync localAmazonKinesis(AWSCredentialsProvider awsCredentialsProvider) {
    log.info("Instantiating local stack Kinesis connection {}...", awsEndpointConfiguration);
    return AmazonKinesisAsyncClientBuilder
        .standard()
        .withCredentials(awsCredentialsProvider)
        .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(
            awsEndpointConfiguration.getUrl(),
            awsEndpointConfiguration.getSigningRegion()
        ))
        .build();
  }

  @Bean
  @Profile("!local && !integration && !k8s")
  AmazonKinesisAsync amazonKinesis(AWSCredentialsProvider awsCredentialsProvider) {
    return AmazonKinesisAsyncClientBuilder
        .standard()
        .withCredentials(awsCredentialsProvider)
        .build();
  }

}