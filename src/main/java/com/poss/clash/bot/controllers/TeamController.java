package com.poss.clash.bot.controllers;

import com.poss.clash.bot.openapi.api.TeamsApi;
import com.poss.clash.bot.openapi.model.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
public class TeamController implements TeamsApi {
    @Override
    public Mono<ResponseEntity<Team>> assignUserToTeam(String teamId, Long discordId, Mono<PositionDetails> positionDetails, ServerWebExchange exchange) {
        return null;
    }

    @Override
    public Mono<ResponseEntity<Team>> createTeam(Mono<TeamRequired> teamRequired, ServerWebExchange exchange) {
        return null;
    }

    @Override
    public Mono<ResponseEntity<Team>> removeUserFromTeam(String teamId, Long discordId, ServerWebExchange exchange) {
        return null;
    }

    @Override
    public Mono<ResponseEntity<Team>> retrieveTeamBasedOnId(String teamId, ServerWebExchange exchange) {
        return null;
    }

    @Override
    public Mono<ResponseEntity<Teams>> retrieveTeams(Boolean inactive, Long discordId, Long serverId, String tournamentName, String tournamentDay, ServerWebExchange exchange) {
        return null;
    }

    @Override
    public Mono<ResponseEntity<Team>> updateTeam(String teamId, Mono<TeamOnlyRequired> teamOnlyRequired, ServerWebExchange exchange) {
        return null;
    }
}
