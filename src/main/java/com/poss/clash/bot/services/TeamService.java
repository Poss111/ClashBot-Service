package com.poss.clash.bot.services;

import com.poss.clash.bot.daos.TeamDao;
import com.poss.clash.bot.daos.models.BasePlayerRecord;
import com.poss.clash.bot.daos.models.ClashTeam;
import com.poss.clash.bot.exceptions.ClashBotDbException;
import com.poss.clash.bot.openapi.model.Role;
import com.poss.clash.bot.utils.IdUtils;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.text.MessageFormat;
import java.util.Map;
import java.util.Optional;

@Service
@AllArgsConstructor
public class TeamService {

    private final TeamDao teamDao;
    private final IdUtils idUtils;

    public Mono<ClashTeam> removeUserFromTeam(String teamId, Integer discordId) {
        return teamDao.findByTeamId_Id(teamId)
                        .map(team -> {
                            if (team.getPositions().values().stream().noneMatch(player -> discordId.equals(player.getDiscordId()))) {
                                throw new ClashBotDbException(MessageFormat.format("User {0} does not belong to Team {1}.", discordId, teamId), HttpStatus.BAD_REQUEST);
                            }
                            return team;
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


    public ClashTeam addUserToTeam(Integer id, Role role, ClashTeam team) {
        team.getPositions()
                .put(role, BasePlayerRecord.builder()
                        .discordId(id)
                        .build());
        return team;
    }

    private ClashTeam removeUserFromTeam(Integer discordId, ClashTeam team) {
        Optional<Role> role = team.getPositions()
                .entrySet().stream()
                .filter((entry) -> discordId.equals(entry.getValue().getDiscordId()))
                    .map(Map.Entry::getKey)
                    .findFirst();
        role.ifPresent(value -> team.getPositions().remove(value));
        return team;
    }

    public Mono<ClashTeam> createClashTeam(ClashTeam clashTeam) {
        String clashTeamId = idUtils.retrieveNewClashTeamId();
        clashTeam.getTeamId().setId(clashTeamId);
        return teamDao.save(clashTeam);
    }

    public Mono<ClashTeam> updateTeamName(String teamId, String newTeamName) {
        return teamDao.findByTeamId_Id(teamId)
                .flatMap(team -> teamDao.updateTeamName(teamId, newTeamName)
                        .thenReturn(team))
                .map(team -> {
                    team.setTeamName(newTeamName);
                    return team;
                });
    }

    public Mono<ClashTeam> findTeamById(String teamId) {
        return teamDao.findByTeamId_Id(teamId)
                .onErrorMap(error -> new ClashBotDbException("Failed to retrieve Clash Team record", error, HttpStatus.INTERNAL_SERVER_ERROR));
    }

    public Flux<ClashTeam> retrieveTeamBasedOnCriteria(Integer discordId, Integer serverId, String tournamentName, String tournamentDay) {
        if (null != discordId) {
            return teamDao.findAllTeamsThatUserBelongsTo(discordId);
        } else if (null != serverId && null != tournamentName && null != tournamentDay) {
            return teamDao.findAllByServerId_AndTeamId_TournamentId_TournamentName_AndTeamId_TournamentId_TournamentDay(
                    serverId,
                    tournamentName,
                    tournamentDay
            );
        } else if (null != serverId && null != tournamentName) {
          return teamDao.findAllByServerId_AndTeamId_TournamentId_TournamentName(
                  serverId,
                  tournamentName
          );
        } else if (null != serverId && null != tournamentDay)  {
            return teamDao.findAllByServerId_AndTeamId_TournamentId_TournamentDay(
                    serverId,
                    tournamentDay
            );
        } else if (null != tournamentName && null != tournamentDay) {
            return teamDao.findAllByTeamId_TournamentId_TournamentName_AndTeamId_TournamentId_TournamentName(
                    tournamentName,
                    tournamentDay
            );
        } else if (null != serverId) {
            return teamDao.findAllByServerId(serverId);
        } else if (null != tournamentName) {
            return teamDao.findAllByTeamId_TournamentId_TournamentName(tournamentName);
        } else if (null != tournamentDay) {
            return teamDao.findAllByTeamId_TournamentId_TournamentDay(tournamentDay);
        }
        return teamDao.findAll();
    }

}
