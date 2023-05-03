package com.poss.clash.bot.configs.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("aws.endpoint-configuration")
@Data
public class AWSEndpointConfiguration {

    private String url;
    private String signingRegion;

}
