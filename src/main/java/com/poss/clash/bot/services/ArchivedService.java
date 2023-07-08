package com.poss.clash.bot.services;

import com.poss.clash.bot.daos.*;
import com.poss.clash.bot.daos.models.*;
import com.poss.clash.bot.enums.ArchiveStatus;
import com.poss.clash.bot.exceptions.ClashBotControllerException;
import com.poss.clash.bot.exceptions.ClashBotDbException;
import com.poss.clash.bot.services.models.ArchiveResults;
import com.poss.clash.bot.utils.IdUtils;
import com.poss.clash.bot.utils.TeamMapper;
import com.poss.clash.bot.utils.TentativeMapper;
import com.poss.clash.bot.utils.TournamentMapper;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ArchivedService {

  private final TournamentService tournamentService;
  private final TeamService teamService;
  private final TentativeService tentativeService;
  private final TeamDao teamDao;
  private final TentativeDao tentativeDao;
  private final TournamentDao tournamentDao;
  private final ArchivedClashTeamDao archivedClashTeamDao;
  private final ArchivedClashTournamentDao archivedClashTournamentDao;
  private final ArchivedTentativeQueueDao archivedTentativeQueueDao;
  private final ArchiveExecutionDao archiveExecutionDao;
  private final TournamentMapper tournamentMapper;
  private final TeamMapper teamMapper;
  private final TentativeMapper tentativeMapper;
  private final IdUtils idUtils;

  public Flux<ArchivedTentativeQueue> retrieveArchivedTentativeQueues(
      String discordId, String serverId, String tournamentName, String tournamentDay
  ) {
    return Flux.error(new ClashBotControllerException("Not implemented yet.", HttpStatus.NOT_IMPLEMENTED));
  }

  public Flux<ArchivedClashTeam> retrieveArchivedTeamBasedOnCriteria(
      String discordId, String serverId, String tournamentName, String tournamentDay
  ) {
    return Flux.error(new ClashBotControllerException("Not implemented yet.", HttpStatus.NOT_IMPLEMENTED));
  }

  public Flux<ArchivedClashTournament> retrieveArchivedClashTournaments(String tournamentName, String tournamentDay) {
    return Flux.error(new ClashBotControllerException("Not implemented yet.", HttpStatus.NOT_IMPLEMENTED));
  }

  public Mono<ArchiveResults> archiveBasedOnInactiveTournaments() {
    return tournamentService
        .retrieveAllTournaments(false)
        .filter(ArchivedService::isInactiveTournament)
        .map(tournamentMapper::clashTournamentToArchivedClashTournament)
        .switchIfEmpty(Mono.error(new ClashBotDbException("No Tournaments found to archive.", HttpStatus.NOT_FOUND)))
        .collectList()
        .log()
        .flatMap(tournaments -> {
          List<TournamentId> tournamentIds = tournaments
              .stream()
              .map(ArchivedClashTournament::getTournamentId)
              .collect(Collectors.toList());
          ArchiveExecution archiveExecution = ArchiveExecution
              .builder()
              .UUID(idUtils.retrieveNewArchiveExecutionId())
              .clashTournaments(tournamentIds)
              .clashTournamentsArchived(tournamentIds.size())
              .status(ArchiveStatus.IN_PROGRESS)
              .build();
          return Mono
              .just(archiveExecution)
              .flatMap(archiveExecutionDao::save)
              .flatMap(savedExecution ->
                           Mono.zip(
                               Mono.just(tournaments),
                               archiveTeamsBasedOnTournamentIds(tournamentIds),
                               archiveTentativeQueuesBasedOnTournamentIds(tournamentIds),
                               Mono.just(savedExecution)));
        })
        .log()
        .checkpoint("Archived Clash Teams and Tentative Queues.")
        .flatMap(tuple -> archivedClashTournaments(tuple.getT1())
            .thenReturn(tuple))
        .flatMap(tuple -> {
          ArchiveExecution archiveExecution = tuple.getT4();
          archiveExecution.setTeamsArchived(tuple
                                                .getT2()
                                                .size());
          archiveExecution.setTentativeQueuesArchived(tuple
                                                          .getT3()
                                                          .size());
          archiveExecution.setStatus(ArchiveStatus.SUCCESSFUL);
          return archiveExecutionDao
              .save(tuple.getT4())
              .thenReturn(tuple);
        })
        .checkpoint("Archived Clash Tournaments")
        .map(tuple -> ArchiveResults
            .builder()
            .inactiveTournaments(tuple.getT1())
            .teamsArchived(tuple.getT2())
            .tentativeQueues(tuple.getT3())
            .build());
  }

  protected Mono<List<ArchivedClashTeam>> archiveTeamsBasedOnTournamentIds(List<TournamentId> tournamentIds) {
    return teamService
        .retrieveTeamsBasedOnListOfTournaments(tournamentIds)
        .map(teamMapper::teamToArchivedClashTeam)
        .flatMap(archivedClashTeamDao::save)
        .collectList()
        .flatMap(teams -> {
          List<TeamId> ids = teams
              .stream()
              .map(ArchivedClashTeam::getTeamId)
              .collect(Collectors.toList());
          return Mono
              .just(ids)
              .flatMap(teamDao::deleteAllById)
              .thenReturn(teams);
        });
  }

  protected Mono<List<ArchivedTentativeQueue>> archiveTentativeQueuesBasedOnTournamentIds(
      List<TournamentId> tournamentIds
  ) {
    return tentativeService
        .retrieveTentativeQueuesBasedOnTournaments(tournamentIds)
        .map(tentativeMapper::tentativeQueueToArchivedTentativeQueue)
        .flatMap(archivedTentativeQueueDao::save)
        .collectList()
        .flatMap(tentativeQueues -> {
          List<TentativeId> ids = tentativeQueues
              .stream()
              .map(ArchivedTentativeQueue::getTentativeId)
              .collect(Collectors.toList());
          return Mono
              .just(ids)
              .flatMap(tentativeDao::deleteAllById)
              .thenReturn(tentativeQueues);
        });
  }

  protected Mono<List<ArchivedClashTournament>> archivedClashTournaments(
      List<ArchivedClashTournament> archivedClashTournaments
  ) {
    return Flux
        .fromIterable(archivedClashTournaments)
        .flatMap(archivedClashTournamentDao::save)
        .collectList()
        .flatMap(tournaments -> {
          List<TournamentId> ids = tournaments
              .stream()
              .map(ArchivedClashTournament::getTournamentId)
              .collect(Collectors.toList());
          return Mono
              .just(ids)
              .flatMap(tournamentDao::deleteAllById)
              .thenReturn(tournaments);
        });
  }

  protected static boolean isInactiveTournament(ClashTournament clashTournament) {
    return Instant
        .now()
        .isAfter(clashTournament.getStartTime());
  }

}
