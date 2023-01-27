package com.poss.clash.bot.configs;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
public class DynamoDbConfig {

    private String endpoint;

}
