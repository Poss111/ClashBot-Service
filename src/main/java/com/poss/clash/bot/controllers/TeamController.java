package com.poss.clash.bot.controllers;

import com.poss.clash.bot.exceptions.ClashBotControllerException;
import com.poss.clash.bot.openapi.api.TeamsApi;
import com.poss.clash.bot.openapi.model.*;
import com.poss.clash.bot.services.ArchivedService;
import com.poss.clash.bot.services.TeamService;
import com.poss.clash.bot.services.UserAssignmentService;
import com.poss.clash.bot.services.UserService;
import com.poss.clash.bot.utils.TeamMapper;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;

import static com.poss.clash.bot.constants.GlobalConstants.CAUSED_BY_KEY;

@RestController
@AllArgsConstructor
public class TeamController implements TeamsApi {

  private final UserAssignmentService userAssignmentService;
  private final TeamService teamService;
  private final ArchivedService archivedService;
  private final TeamMapper teamMapper;
  private final UserService userService;

  private Mono<TeamRequired> validatePlayerDetails(TeamRequired payload) {
    if (null == payload.getPlayerDetails()) {
      return Mono.error(
          new ClashBotControllerException("Missing user details to create team with.", HttpStatus.BAD_REQUEST));
    }
    return Mono.just(payload);
  }

  @Override
  public Mono<ResponseEntity<Team>> assignUserToTeam(
      String xCausedBy, String teamId, String discordId, Mono<PositionDetails> positionDetails,
      ServerWebExchange exchange
  ) {
    return positionDetails
        .flatMap(details -> userAssignmentService.assignUserToTeam(discordId, details.getRole(), teamId))
        .map(teamMapper::clashTeamToTeam)
        .map(ResponseEntity::ok)
        .contextWrite(ctx -> ctx.put(CAUSED_BY_KEY, xCausedBy));
  }

  @Override
  public Mono<ResponseEntity<Team>> createTeam(
      String xCausedBy, Mono<TeamRequired> teamRequired, ServerWebExchange exchange
  ) {
    return teamRequired
        .flatMap(this::validatePlayerDetails)
        .flatMap(teamDetails -> {
                   HashMap<Role, String> roleToId = new HashMap<>();
                   if (null != teamDetails
                       .getPlayerDetails()
                       .getTop()) {
                     roleToId.put(Role.TOP, teamDetails
                         .getPlayerDetails()
                         .getTop()
                         .getDiscordId());
                   }
                   if (null != teamDetails
                       .getPlayerDetails()
                       .getJg()) {
                     roleToId.put(Role.JG, teamDetails
                         .getPlayerDetails()
                         .getJg()
                         .getDiscordId());
                   }
                   if (null != teamDetails
                       .getPlayerDetails()
                       .getMid()) {
                     roleToId.put(Role.MID, teamDetails
                         .getPlayerDetails()
                         .getMid()
                         .getDiscordId());
                   }
                   if (null != teamDetails
                       .getPlayerDetails()
                       .getBot()) {
                     roleToId.put(Role.BOT, teamDetails
                         .getPlayerDetails()
                         .getBot()
                         .getDiscordId());
                   }
                   if (null != teamDetails
                       .getPlayerDetails()
                       .getSupp()) {
                     roleToId.put(Role.SUPP, teamDetails
                         .getPlayerDetails()
                         .getSupp()
                         .getDiscordId());
                   }
                   return userAssignmentService.createTeamAndAssignUser(roleToId,
                                                                        teamDetails.getName(),
                                                                        teamDetails.getServerId(),
                                                                        teamDetails
                                                                            .getTournament()
                                                                            .getTournamentName(),
                                                                        teamDetails
                                                                            .getTournament()
                                                                            .getTournamentDay());
                 }
        )
        .map(teamMapper::clashTeamToTeam)
        .map(ResponseEntity::ok)
        .contextWrite(ctx -> ctx.put(CAUSED_BY_KEY, xCausedBy));
  }

  @Override
  public Mono<ResponseEntity<Team>> removeUserFromTeam(
      String xCausedBy, String teamId, String discordId, ServerWebExchange exchange
  ) {
    return userAssignmentService
        .findAndRemoveUserFromTeam(discordId, teamId)
        .contextWrite(ctx -> ctx.put(CAUSED_BY_KEY, xCausedBy))
        .map(teamMapper::clashTeamToTeam)
        .map(ResponseEntity::ok);
  }

  @Override
  public Mono<ResponseEntity<Team>> retrieveTeamBasedOnId(String xCausedBy, String teamId, ServerWebExchange exchange) {
    return teamService
        .findTeamById(teamId)
        .contextWrite(ctx -> ctx.put(CAUSED_BY_KEY, xCausedBy))
        .map(teamMapper::clashTeamToTeam)
        .map(ResponseEntity::ok);
  }

  @Override
  public Mono<ResponseEntity<Teams>> retrieveTeams(
      String xCausedBy, Boolean archived, String discordId, String serverId, String tournamentName,
      String tournamentDay, ServerWebExchange exchange
  ) {
    Mono<List<Team>> monoOfTeams;
    if (archived) {
      monoOfTeams = archivedService
          .retrieveArchivedTeamBasedOnCriteria(
              discordId,
              serverId,
              tournamentName,
              tournamentDay)
          .map(teamMapper::archivedClashTeamToTeam)
          .collectList();
    } else {
      monoOfTeams = teamService
          .retrieveTeamBasedOnCriteria(
              discordId,
              serverId,
              tournamentName,
              tournamentDay)
          .log()
          .collectList()
          .flatMapMany(userService::enrichClashTeamsWithUserDetails)
          .log()
          .map(teamMapper::clashTeamToTeam)
          .collectList();
    }
    return monoOfTeams
        .map(list -> Teams
            .builder()
            .count(list.size())
            .teams(list)
            .build())
        .map(ResponseEntity::ok)
        .defaultIfEmpty(ResponseEntity
                            .notFound()
                            .build())
        .contextWrite(ctx -> ctx.put(CAUSED_BY_KEY, xCausedBy));
  }

  @Override
  public Mono<ResponseEntity<Team>> updateTeam(
      String xCausedBy, String teamId, Mono<TeamUpdate> teamUpdate, ServerWebExchange exchange
  ) {
    return teamUpdate
        .log()
        .flatMap(updateDetails -> teamService.updateTeamName(teamId, updateDetails.getTeamName()))
        .map(teamMapper::clashTeamToTeam)
        .map(ResponseEntity::ok)
        .defaultIfEmpty(ResponseEntity
                            .notFound()
                            .build())
        .contextWrite(ctx -> ctx.put(CAUSED_BY_KEY, xCausedBy));
  }

}
