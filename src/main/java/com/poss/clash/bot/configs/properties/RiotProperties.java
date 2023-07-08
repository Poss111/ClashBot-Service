package com.poss.clash.bot.configs.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Data
@ConfigurationProperties("riot")
public class RiotProperties {

  private String url;
  private String apiKey;
  private Duration timeout = Duration.ofSeconds(15);

}
