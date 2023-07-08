package com.poss.clash.bot.configs;

import com.poss.clash.bot.configs.properties.RiotProperties;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@Slf4j
@Configuration
@AllArgsConstructor
public class RiotApiConfig {

  private static final String X_RIOT_TOKEN = "X-Riot-Token";
  private final RiotProperties riotProperties;

  @Bean
  public WebClient riotWebClient() {
    log.info(
        "Setting up Riot WebClient url={} timeout={}",
        riotProperties.getUrl(),
        riotProperties.getTimeout()
    );
    return WebClient
        .builder()
        .clientConnector(new ReactorClientHttpConnector(HttpClient
                                                            .create()
                                                            .responseTimeout(riotProperties.getTimeout())))
        .baseUrl(riotProperties.getUrl())
        .defaultHeader(X_RIOT_TOKEN, riotProperties.getApiKey())
        .build();
  }

}
