package com.poss.clash.bot.controllers;

import com.poss.clash.bot.openapi.api.TeamApi;
import com.poss.clash.bot.openapi.model.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
public class TeamController implements TeamApi {
    @Override
    public Mono<ResponseEntity<Team>> assignUserToTeam(Integer teamId, Mono<PositionDetails> positionDetails,
                                                       ServerWebExchange exchange) {
        return null;
    }

    @Override
    public Mono<ResponseEntity<Team>> createTeamBasedOnServerAndTournament(Integer serverId, String tournamentName,
                                                                           ServerWebExchange exchange) {
        return null;
    }

    @Override
    public Mono<ResponseEntity<Team>> createTeamBasedOnServerAndTournamentAndDay(Integer serverId,
                                                                                 String tournamentName,
                                                                                 String tournamentDay,
                                                                                 ServerWebExchange exchange) {
        return null;
    }

    @Override
    public Mono<ResponseEntity<Teams>> getTeamBasedOnServer(Integer serverId, ServerWebExchange exchange) {
        return null;
    }

    @Override
    public Mono<ResponseEntity<Teams>> getTeamsBasedOnServerAndTournament(Integer serverId, String tournamentName,
                                                                          ServerWebExchange exchange) {
        return null;
    }

    @Override
    public Mono<ResponseEntity<Teams>> getTeamsBasedOnServerAndTournamentAndDay(Integer serverId, String tournamentName,
                                                                                String tournamentDay,
                                                                                ServerWebExchange exchange) {
        return null;
    }

    @Override
    public Mono<ResponseEntity<Team>> removeUserFromTeam(Integer teamId, Integer discordId,
                                                         ServerWebExchange exchange) {
        return null;
    }

    @Override
    public Mono<ResponseEntity<Team>> retrieveTeamBasedOnId(Integer teamId, ServerWebExchange exchange) {
        return null;
    }

    @Override
    public Mono<ResponseEntity<Teams>> retrieveTeams(Boolean onlyActive, ServerWebExchange exchange) {
        return null;
    }

    @Override
    public Mono<ResponseEntity<Team>> updateTeamBasedOnServerAndTournament(Integer serverId, String tournamentName,
                                                                           Mono<PositionDetails> positionDetails,
                                                                           ServerWebExchange exchange) {
        return null;
    }

    @Override
    public Mono<ResponseEntity<Team>> updateTeamBasedOnServerAndTournamentAndDay(Integer serverId,
                                                                                 String tournamentName,
                                                                                 String tournamentDay,
                                                                                 Mono<PositionDetails> positionDetails,
                                                                                 ServerWebExchange exchange) {
        return null;
    }
}
