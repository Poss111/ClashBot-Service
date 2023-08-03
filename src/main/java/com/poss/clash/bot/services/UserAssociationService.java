package com.poss.clash.bot.services;

import com.poss.clash.bot.daos.UserAssociationDao;
import com.poss.clash.bot.daos.models.TournamentId;
import com.poss.clash.bot.daos.models.UserAssociation;
import com.poss.clash.bot.daos.models.UserAssociationKey;
import com.poss.clash.bot.openapi.model.Event;
import com.poss.clash.bot.source.TeamSource;
import com.poss.clash.bot.utils.TeamMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.text.MessageFormat;
import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class UserAssociationService {

  private final UserAssociationDao userAssociationDao;
  private final TournamentService tournamentService;
  private final TeamService teamService;
  private final UserService userService;
  private final TeamSource teamSource;
  private final TeamMapper teamMapper;

  public Mono<UserAssociation> retrieveUsersTeamOrTentativeQueueForTournament(
      String discordId, String tournamentName, String tournamentDay
  ) {
    return userAssociationDao
        .findById(UserAssociationKey
                      .builder()
                      .discordId(discordId)
                      .tournamentId(TournamentId
                                        .builder()
                                        .tournamentName(tournamentName)
                                        .tournamentDay(tournamentDay)
                                        .build())
                      .build())
        .log();
  }

  public Mono<UserAssociation> save(UserAssociation userAssociation) {
    return userAssociationDao.save(userAssociation);
  }

  public Mono<Void> delete(UserAssociationKey userAssociationKey) {
    return userAssociationDao.deleteById(userAssociationKey);
  }

  public Flux<Event> updateInvolvedTeams(String discordId) {
    return tournamentService
        .retrieveAllTournaments(true)
        .log()
        .flatMap(tournament -> userAssociationDao
            .findByUserAssociationKeyAndTentativeIdIsNull(UserAssociationKey
                                                              .builder()
                                                              .discordId(discordId)
                                                              .tournamentId(tournament.getTournamentId())
                                                              .build()))
        .checkpoint(MessageFormat.format("Finding User Association to update champion information for {0}", discordId))
        .log()
        .flatMap(userAssociation -> teamService
            .findTeamById(userAssociation.getTeamId())
            .checkpoint(
                MessageFormat.format("Pulling team {0} information for {1}", userAssociation.getTeamId(), discordId))
            .log()
            .flatMap(userService::enrichClashTeamWithUserDetails)
            .checkpoint(
                MessageFormat.format("Enriching team {0} information for {1}", userAssociation.getTeamId(), discordId))
            .log()
            .map(teamMapper::clashTeamToTeam)
            .log()
            .flatMap(teamSource::sendTeamUpdateEvent)
            .checkpoint(MessageFormat.format("Sending UPDATE event for team {0} for {1}", userAssociation.getTeamId(),
                                             discordId)));
  }

  public Flux<UserAssociation> retrieveUserAssociationsForATournament(List<TournamentId> tournamentIds) {
    return userAssociationDao.findByUserAssociationKey_TournamentIdIsIn(tournamentIds);
  }

}
