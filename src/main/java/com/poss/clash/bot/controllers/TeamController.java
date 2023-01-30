package com.poss.clash.bot.controllers;

import com.poss.clash.bot.openapi.api.TeamApi;
import com.poss.clash.bot.openapi.model.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class TeamController implements TeamApi {

    @Override
    public Mono<ResponseEntity<Team>> assignUserToFirstTeamBasedOnTournament(Mono<TeamTournamentDetails> teamTournamentDetails, ServerWebExchange exchange) {
        return null;
    }

    @Override
    public Mono<ResponseEntity<Team>> assignUserToTeam(Integer teamId, Mono<PositionDetails> positionDetails, ServerWebExchange exchange) {
        return null;
    }

    @Override
    public Mono<ResponseEntity<Team>> createNewTeam(Mono<CreateNewTeamRequest> createNewTeamRequest, ServerWebExchange exchange) {
        return null;
    }

    @Override
    public Mono<ResponseEntity<Team>> deleteTeam(String name, Integer serverId, String tournament, String tournamentDay, Integer discordId, ServerWebExchange exchange) {
        return null;
    }

    @Override
    public Mono<ResponseEntity<Teams>> getTeam(Integer serverId, String name, String tournament, String day, ServerWebExchange exchange) {
        return null;
    }

    @Override
    public Mono<ResponseEntity<Team>> removeUserFromTeam(Integer teamId, Integer discordId, ServerWebExchange exchange) {
        return null;
    }

    @Override
    public Mono<ResponseEntity<Team>> removeUserFromTeamBasedOnTournament(Mono<TeamTournamentDetails> teamTournamentDetails, ServerWebExchange exchange) {
        return null;
    }

    @Override
    public Mono<ResponseEntity<Team>> updateTeam(Mono<UpdateTeamRequest> updateTeamRequest, ServerWebExchange exchange) {
        return null;
    }

}
