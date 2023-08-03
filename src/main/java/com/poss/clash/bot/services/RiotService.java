package com.poss.clash.bot.services;

import com.poss.clash.bot.daos.models.ClashTournament;
import com.poss.clash.bot.services.api.RiotApiService;
import com.poss.clash.bot.utils.TournamentMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@AllArgsConstructor
public class RiotService {

  private final RiotApiService riotApiService;
  private final TournamentService tournamentService;
  private final TournamentMapper tournamentMapper;

  public Flux<ClashTournament> retrieveAndPersistTournaments() {
    return riotApiService
        .retrieveClashTournaments()
        .log()
        .map(tournamentMapper::riotClashTournamentToClashTournament)
        .log()
        .flatMap(tournamentService::saveTournament);
  }

}
