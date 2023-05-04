package com.poss.clash.bot.controllers;

import com.poss.clash.bot.daos.models.ClashTournament;
import com.poss.clash.bot.openapi.api.TournamentsApi;
import com.poss.clash.bot.openapi.model.DetailedTournament;
import com.poss.clash.bot.openapi.model.Tournaments;
import com.poss.clash.bot.services.TournamentService;
import com.poss.clash.bot.utils.TournamentMapper;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@AllArgsConstructor
public class TournamentsController implements TournamentsApi {

    private final TournamentService tournamentService;
    private final TournamentMapper tournamentMapper;

    @Override
    public Mono<ResponseEntity<DetailedTournament>> createTournament(String xCausedBy, Mono<DetailedTournament> detailedTournament, ServerWebExchange exchange) {
        return detailedTournament
                .map(tournamentMapper::detailedTournamentToClashTournament)
                .flatMap(tournamentService::saveTournament)
                .map(tournamentMapper::clashTournamentToDetailedTournament)
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<Tournaments>> getTournaments(String xCausedBy, String tournament, String day, Boolean upcomingOnly, ServerWebExchange exchange) {
        Flux<ClashTournament> tournamentFlux;
        if (StringUtils.isNotBlank(tournament) || StringUtils.isNotBlank(day)) {
            tournamentFlux = tournamentService.retrieveTournamentsByTournamentOrDay(tournament, day);
        } else {
            tournamentFlux = tournamentService.retrieveAllTournaments(upcomingOnly);
        }
        return tournamentFlux
                .map(tournamentMapper::clashTournamentToDetailedTournament)
                .collectList()
                .map(listOfTournaments -> Tournaments.builder()
                        .count(listOfTournaments.size())
                        .tournaments(listOfTournaments)
                        .build())
                .map(ResponseEntity::ok);
    }
    
}
