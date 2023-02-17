package com.poss.clash.bot.controllers;

import com.poss.clash.bot.openapi.api.TeamsApi;
import com.poss.clash.bot.openapi.model.*;
import com.poss.clash.bot.services.TeamService;
import com.poss.clash.bot.services.UserAssignmentService;
import com.poss.clash.bot.utils.TeamMapper;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashMap;

@RestController
@AllArgsConstructor
public class TeamController implements TeamsApi {

    private final UserAssignmentService userAssignmentService;
    private final TeamService teamService;
    private final TeamMapper teamMapper;

    @Override
    public Mono<ResponseEntity<Team>> assignUserToTeam(String teamId, Long discordId, Mono<PositionDetails> positionDetails, ServerWebExchange exchange) {
        return positionDetails
                .flatMap(details -> userAssignmentService.assignUserToTeam(discordId.intValue(), details.getRole(), teamId))
                .map(teamMapper::clashTeamToTeam)
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<Team>> createTeam(Mono<TeamRequired> teamRequired, ServerWebExchange exchange) {
        return teamRequired
                .flatMap(teamDetails -> {
                    HashMap<Role, Integer> roleToId = new HashMap<>();
                    if (null != teamDetails.getPlayerDetails().getTop()) {
                        roleToId.put(Role.TOP, teamDetails.getPlayerDetails().getTop().getDiscordId());
                    } if (null != teamDetails.getPlayerDetails().getJg()) {
                        roleToId.put(Role.JG, teamDetails.getPlayerDetails().getJg().getDiscordId());
                    } if (null != teamDetails.getPlayerDetails().getMid()) {
                        roleToId.put(Role.MID, teamDetails.getPlayerDetails().getMid().getDiscordId());
                    } if (null != teamDetails.getPlayerDetails().getBot()) {
                        roleToId.put(Role.BOT, teamDetails.getPlayerDetails().getBot().getDiscordId());
                    } if (null != teamDetails.getPlayerDetails().getSupp()){
                        roleToId.put(Role.SUPP, teamDetails.getPlayerDetails().getSupp().getDiscordId());
                    }
                    return userAssignmentService.createTeamAndAssignUser(roleToId,
                            teamDetails.getName(),
                            teamDetails.getServerId(),
                            teamDetails.getTournament().getTournamentName(),
                            teamDetails.getTournament().getTournamentDay());
                    }
                )
                .map(teamMapper::clashTeamToTeam)
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<Team>> removeUserFromTeam(String teamId, Long discordId, ServerWebExchange exchange) {
        return userAssignmentService.findAndRemoveUserFromTeam(discordId.intValue(), teamId)
                .map(teamMapper::clashTeamToTeam)
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<Team>> retrieveTeamBasedOnId(String teamId, ServerWebExchange exchange) {
        return teamService.findTeamById(teamId)
                .map(teamMapper::clashTeamToTeam)
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<Teams>> retrieveTeams(Boolean archived, Long discordId, Long serverId, String tournamentName, String tournamentDay, ServerWebExchange exchange) {
        return teamService.retrieveTeamBasedOnCriteria(discordId.intValue(), serverId.intValue(), tournamentName, tournamentDay)
                .map(teamMapper::clashTeamToTeam)
                .collectList()
                .map(list -> Teams.builder()
                        .count(list.size())
                        .teams(list)
                        .build())
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<Team>> updateTeam(String teamId, Mono<TeamOnlyRequired> teamOnlyRequired, ServerWebExchange exchange) {
        return null;
    }
}
