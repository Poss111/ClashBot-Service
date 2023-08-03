package com.poss.clash.bot.services;

import com.poss.clash.bot.daos.TeamDao;
import com.poss.clash.bot.daos.models.BasePlayerRecord;
import com.poss.clash.bot.daos.models.ClashTeam;
import com.poss.clash.bot.daos.models.TournamentId;
import com.poss.clash.bot.exceptions.ClashBotDbException;
import com.poss.clash.bot.openapi.model.Role;
import com.poss.clash.bot.source.TeamSource;
import com.poss.clash.bot.utils.IdUtils;
import com.poss.clash.bot.utils.TeamMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class TeamService {

  private final TeamDao teamDao;
  private final IdUtils idUtils;
  private final TeamMapper teamMapper;
  private final TeamSource teamSource;

  public Mono<ClashTeam> removeUserFromTeam(String teamId, String discordId) {
    return teamDao
        .findByTeamId_Id(teamId)
        .<ClashTeam>handle((team, sink) -> {
          if (team
              .getPositions()
              .values()
              .stream()
              .noneMatch(player -> discordId.equals(player.getDiscordId()))) {
            sink.error(new ClashBotDbException(
                MessageFormat.format("User {0} does not belong to Team {1}.", discordId, teamId),
                HttpStatus.BAD_REQUEST));
            return;
          }
          sink.next(team);
        })
        .map(team -> removeUserFromTeam(discordId, team))
        .log()
        .flatMap(teamDao::save)
        .log()
        .checkpoint(MessageFormat.format("Removed User {0} from Team {1}.", discordId, teamId));
  }

  public Mono<ClashTeam> upsertClashTeam(ClashTeam clashTeam) {
    return this.teamDao.save(clashTeam);
  }


  public ClashTeam addUserToTeam(String id, Role role, ClashTeam team) {
    ClashTeam clone = teamMapper.clone(team);
    clone
        .getPositions()
        .put(role, BasePlayerRecord
            .builder()
            .discordId(id)
            .build());
    return clone;
  }

  private ClashTeam removeUserFromTeam(String discordId, ClashTeam team) {
    Optional<Role> role = team
        .getPositions()
        .entrySet()
        .stream()
        .filter((entry) -> discordId.equals(entry
                                                .getValue()
                                                .getDiscordId()))
        .map(Map.Entry::getKey)
        .findFirst();
    role.ifPresent(value -> team
        .getPositions()
        .remove(value));
    return team;
  }

  public Mono<ClashTeam> createClashTeam(ClashTeam clashTeam) {
    String clashTeamId = idUtils.retrieveNewClashTeamId();
    clashTeam
        .getTeamId()
        .setId(clashTeamId);
    return teamDao.save(clashTeam);
  }

  public Mono<ClashTeam> updateTeamName(String teamId, String newTeamName) {
    return teamDao
        .findByTeamId_Id(teamId)
        .flatMap(team -> teamDao
            .updateTeamName(teamId, newTeamName)
            .thenReturn(team))
        .flatMap(team -> {
                   team.setTeamName(newTeamName);
                   return teamSource
                       .sendTeamUpdateEvent(teamMapper.clashTeamToTeam(team))
                       .thenReturn(team);
                 }
        );
  }

  public Mono<ClashTeam> findTeamById(String teamId) {
    return teamDao
        .findByTeamId_Id(teamId)
        .onErrorMap(error -> new ClashBotDbException("Failed to retrieve Clash Team record", error,
                                                     HttpStatus.INTERNAL_SERVER_ERROR));
  }

  public Flux<ClashTeam> retrieveTeamBasedOnCriteria(
      String discordId, String serverId, String tournamentName, String tournamentDay
  ) {
    if (StringUtils.isNotBlank(discordId)) {
      return teamDao.findAllTeamsThatUserBelongsTo(discordId);
    } else if (StringUtils.isNotBlank(serverId) && StringUtils.isNotBlank(tournamentName) &&
               StringUtils.isNotBlank(tournamentDay)) {
      return teamDao
          .findAllByServerId_AndTeamId_TournamentId_TournamentName_AndTeamId_TournamentId_TournamentDay(
              serverId,
              tournamentName,
              tournamentDay
          )
          .checkpoint(MessageFormat.format("Retrieve Teams based on serverId={0} tournamentName={1} tournamentDay={2}",
                                           serverId, tournamentName, tournamentDay));
    } else if (StringUtils.isNotBlank(serverId) && StringUtils.isNotBlank(tournamentName)) {
      return teamDao
          .findAllByServerId_AndTeamId_TournamentId_TournamentName(
              serverId,
              tournamentName
          )
          .checkpoint(MessageFormat.format("Retrieve Teams based on serverId={0} tournamentName={1}", serverId,
                                           tournamentName));
    } else if (StringUtils.isNotBlank(serverId) && StringUtils.isNotBlank(tournamentDay)) {
      return teamDao
          .findAllByServerId_AndTeamId_TournamentId_TournamentDay(
              serverId,
              tournamentDay
          )
          .checkpoint(
              MessageFormat.format("Retrieve Teams based on serverId={0} tournamentDay={1}", serverId, tournamentDay));
    } else if (StringUtils.isNotBlank(tournamentName) && StringUtils.isNotBlank(tournamentDay)) {
      return teamDao
          .findAllByTeamId_TournamentId_TournamentName_AndTeamId_TournamentId_TournamentDay(
              tournamentName,
              tournamentDay
          )
          .checkpoint(
              MessageFormat.format("Retrieve Teams based on tournamentName={0} tournamentDay={1}", tournamentName,
                                   tournamentDay));
    } else if (StringUtils.isNotBlank(serverId)) {
      return teamDao
          .findAllByServerId(serverId)
          .checkpoint(MessageFormat.format("Retrieve Teams based on serverId={0}", serverId));
    } else if (StringUtils.isNotBlank(tournamentName)) {
      return teamDao
          .findAllByTeamId_TournamentId_TournamentName(tournamentName)
          .checkpoint(MessageFormat.format("Retrieve Teams based on tournamentName={0}", tournamentName));
    } else if (StringUtils.isNotBlank(tournamentDay)) {
      return teamDao
          .findAllByTeamId_TournamentId_TournamentDay(tournamentDay)
          .checkpoint(MessageFormat.format("Retrieve Teams based on tournamentDay={0}", tournamentDay));
    }
    return teamDao.findAll();
  }

  public Flux<ClashTeam> retrieveTeamsBasedOnListOfTournaments(List<TournamentId> tournamentIds) {
    return teamDao.findAllByTeamId_TournamentIdIsIn(tournamentIds);
  }

}
