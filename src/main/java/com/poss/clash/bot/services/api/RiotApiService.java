package com.poss.clash.bot.services.api;

import com.poss.clash.bot.exceptions.ClashBotDependentApiException;
import com.poss.clash.bot.services.models.RiotClashTournament;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

@Service
@AllArgsConstructor
public class RiotApiService {

  private final WebClient riotWebClient;

  public Flux<RiotClashTournament> retrieveClashTournaments() {
    return riotWebClient
        .get()
        .uri("/lol/clash/v1/tournaments")
        .retrieve()
        .bodyToFlux(RiotClashTournament.class)
        .onErrorMap(error -> new ClashBotDependentApiException(
                        "Failed to retrieve Riot Clash Times.",
                        error,
                        HttpStatus.FAILED_DEPENDENCY
                    )
        );
  }

}
