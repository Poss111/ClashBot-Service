package com.poss.clash.bot.controllers;

import com.poss.clash.bot.openapi.api.TournamentsApi;
import com.poss.clash.bot.openapi.model.Tournament;
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
    public Mono<ResponseEntity<Tournament>> createTournament(Mono<Tournament> tournament, ServerWebExchange exchange) {
        return tournament
                .map(tournamentMapper::tournamentToClashTournament)
                .flatMap(tournamentService::saveTournament)
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<Tournaments>> getTournaments(String tournament, String day, Boolean upcoming, ServerWebExchange exchange) {
        Flux<Tournament> tournamentFlux;
        if (StringUtils.isNotBlank(tournament) || StringUtils.isNotBlank(day)) {
            tournamentFlux = tournamentService.retrieveTournamentsByTournamentOrDay(tournament, day);
        } else {
            tournamentFlux = tournamentService.retrieveAllTournaments(upcoming);
        }
        return tournamentFlux
                .collectList()
                .map(listOfTournaments -> Tournaments.builder()
                        .tournaments(listOfTournaments)
                        .build())
                .map(ResponseEntity::ok);
    }

}
