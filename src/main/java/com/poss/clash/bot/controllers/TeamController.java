package com.poss.clash.bot.controllers;

import com.poss.clash.bot.openapi.api.TeamApi;
import com.poss.clash.bot.openapi.model.CreateNewTeamRequest;
import com.poss.clash.bot.openapi.model.Team;
import com.poss.clash.bot.openapi.model.UpdateTeamRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class TeamController implements TeamApi {

    @Override
    public Mono<ResponseEntity<Team>> createNewTeam(Mono<CreateNewTeamRequest> createNewTeamRequest, ServerWebExchange exchange) {
        return null;
    }

    @Override
    public Mono<ResponseEntity<Flux<Team>>> getTeam(String server, String name, String tournament, String day, ServerWebExchange exchange) {
        return null;
    }

    @Override
    public Mono<ResponseEntity<Team>> removePlayerFromTeam(String name, String serverName, String tournament, String tournamentDay, String playerId, ServerWebExchange exchange) {
        return null;
    }

    @Override
    public Mono<ResponseEntity<Team>> updateTeam(Mono<UpdateTeamRequest> updateTeamRequest, ServerWebExchange exchange) {
        return null;
    }

}
