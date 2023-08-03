package com.poss.clash.bot.services;

import com.poss.clash.bot.ClashBotTestingConfig;
import com.poss.clash.bot.daos.*;
import com.poss.clash.bot.daos.models.*;
import com.poss.clash.bot.enums.ArchiveStatus;
import com.poss.clash.bot.exceptions.ClashBotControllerException;
import com.poss.clash.bot.exceptions.ClashBotDbException;
import com.poss.clash.bot.services.models.ArchiveResults;
import com.poss.clash.bot.utils.*;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.publisher.PublisherProbe;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@Import(ClashBotTestingConfig.class)
public class ArchiveServiceTest {

  @InjectMocks
  ArchivedService archivedService;

  @Mock
  TournamentService tournamentService;

  @Mock
  TeamService teamService;

  @Mock
  TentativeService tentativeService;

  @Mock
  UserAssociationService userAssociationService;

  @Mock
  TeamDao teamDao;

  @Mock
  ArchivedClashTeamDao archivedClashTeamDao;

  @Mock
  TentativeDao tentativeDao;

  @Mock
  ArchivedTentativeQueueDao archivedTentativeQueueDao;

  @Mock
  TournamentDao tournamentDao;

  @Mock
  UserAssociationDao userAssociationDao;

  @Mock
  ArchivedClashTournamentDao archivedClashTournamentDao;

  @Mock
  ArchivedUserAssociationDao archivedUserAssociationDao;

  @Mock
  ArchiveExecutionDao archiveExecutionDao;

  @Mock
  IdUtils idUtils;

  @Spy
  TournamentMapper tournamentMapper = Mappers.getMapper(TournamentMapper.class);

  @Spy
  TeamMapper teamMapper = Mappers.getMapper(TeamMapper.class);

  @Spy
  TentativeMapper tentativeMapper = Mappers.getMapper(TentativeMapper.class);

  @Spy
  UserAssociationMapper userAssociationMapper = Mappers.getMapper(UserAssociationMapper.class);

  @Autowired
  EasyRandom easyRandom;

  @Test
  void test1() {
    StepVerifier
        .create(archivedService.retrieveArchivedTeamBasedOnCriteria(null, null, null, null))
        .expectError(ClashBotControllerException.class)
        .verify();
  }

  @Test
  void test2() {
    StepVerifier
        .create(archivedService.retrieveArchivedTentativeQueues(null, null, null, null))
        .expectError(ClashBotControllerException.class)
        .verify();
  }

  @Test
  void test3() {
    StepVerifier
        .create(archivedService.retrieveArchivedClashTournaments(null, null))
        .expectError(ClashBotControllerException.class)
        .verify();
  }

  @Test
  @DisplayName("archiveBasedOnInactiveTournaments - Should query for all inactive Tournaments then query for the matching teams, tentative queues, and user associations")
  void test_archiveBasedOnInactiveTournaments() {
    ClashTournament clashTournament = easyRandom.nextObject(ClashTournament.class);
    clashTournament.setStartTime(Instant
                                     .now()
                                     .minus(Duration.ofMinutes(30)));
    ClashTeam clashTeam = easyRandom.nextObject(ClashTeam.class);
    clashTournament.setTournamentId(clashTournament.getTournamentId());
    TentativeQueue tentativeQueue = easyRandom.nextObject(TentativeQueue.class);
    tentativeQueue
        .getTentativeId()
        .setTournamentId(clashTournament.getTournamentId());
    UserAssociation userAssociation = easyRandom.nextObject(UserAssociation.class);
    userAssociation
        .getUserAssociationKey()
        .setTournamentId(clashTournament.getTournamentId());
    List<ClashTeam> clashTeams = List.of(clashTeam);
    List<ArchivedClashTeam> expectedArchivedTeam = clashTeams
        .stream()
        .map(teamMapper::teamToArchivedClashTeam)
        .collect(Collectors.toList());
    List<TeamId> clashTeamIds = clashTeams
        .stream()
        .map(ClashTeam::getTeamId)
        .collect(Collectors.toList());
    List<TentativeQueue> tentativeQueues = List.of(tentativeQueue);
    List<ArchivedTentativeQueue> archivedTentativeQueues = tentativeQueues
        .stream()
        .map(tentativeMapper::tentativeQueueToArchivedTentativeQueue)
        .collect(Collectors.toList());
    List<ClashTournament> clashTournaments = List.of(clashTournament);
    List<ArchivedClashTournament> expectedArchivedClashTournaments = clashTournaments
        .stream()
        .map(tournamentMapper::clashTournamentToArchivedClashTournament)
        .collect(Collectors.toList());
    List<TournamentId> tournamentIds = List.of(clashTournament.getTournamentId());
    List<UserAssociation> userAssociations = List.of(userAssociation);
    List<ArchivedUserAssociation> archivedUserAssociations = userAssociations
        .stream()
        .map(userAssociationMapper::userAssociationToArchivedUserAssociation)
        .collect(Collectors.toList());
    List<UserAssociationKey> userAssociationKeys = userAssociations
        .stream()
        .map(UserAssociation::getUserAssociationKey)
        .collect(Collectors.toList());

    ArrayList<PublisherProbe> probes = new ArrayList<>();
    PublisherProbe<ClashTournament> clashTournamentProbe = PublisherProbe.of(
        Flux.fromIterable(clashTournaments));
    probes.add(clashTournamentProbe);
    when(tournamentService.retrieveAllTournaments(false))
        .thenReturn(clashTournamentProbe.flux());
    PublisherProbe<ClashTeam> clashTeamProbe = PublisherProbe.of(Flux.fromIterable(clashTeams));
    probes.add(clashTeamProbe);
    when(teamService.retrieveTeamsBasedOnListOfTournaments(tournamentIds))
        .thenReturn(clashTeamProbe.flux());
    PublisherProbe<ArchivedClashTeam> archivedClashTeamProbe = PublisherProbe.of(
        Mono.just(expectedArchivedTeam.get(0)));
    probes.add(archivedClashTeamProbe);
    when(archivedClashTeamDao.save(expectedArchivedTeam.get(0)))
        .thenReturn(archivedClashTeamProbe.mono());
    PublisherProbe<TentativeQueue> tentativeQueuePublisherProbe = PublisherProbe.of(
        Flux.fromIterable(List.of(tentativeQueue)));
    probes.add(tentativeQueuePublisherProbe);
    when(tentativeService.retrieveTentativeQueuesBasedOnTournaments(tournamentIds))
        .thenReturn(tentativeQueuePublisherProbe.flux());
    PublisherProbe<ArchivedTentativeQueue> archivedTentativeQueuePublisherProbe = PublisherProbe.of(
        Mono.just(archivedTentativeQueues.get(0)));
    probes.add(archivedTentativeQueuePublisherProbe);
    when(archivedTentativeQueueDao.save(archivedTentativeQueues.get(0)))
        .thenReturn(archivedTentativeQueuePublisherProbe.mono());
    PublisherProbe<UserAssociation> userAssociationPublisherProbe = PublisherProbe.of(
        Flux.fromIterable(userAssociations));
    probes.add(userAssociationPublisherProbe);
    when(userAssociationService.retrieveUserAssociationsForATournament(tournamentIds))
        .thenReturn(userAssociationPublisherProbe.flux());
    archivedUserAssociations
        .forEach(archivedUserAssociation -> {
          PublisherProbe<ArchivedUserAssociation> archivedUserAssociationPublisherProbe = PublisherProbe.of(
              Mono.just(archivedUserAssociation));
          probes.add(archivedUserAssociationPublisherProbe);
          when(archivedUserAssociationDao.save(archivedUserAssociation))
              .thenReturn(archivedUserAssociationPublisherProbe.mono());
        });
    PublisherProbe<Void> deleteUserAssociationPublisherProbe = PublisherProbe.empty();
    probes.add(deleteUserAssociationPublisherProbe);
    when(userAssociationDao.deleteAllById(userAssociationKeys))
        .thenReturn(deleteUserAssociationPublisherProbe.mono());
    PublisherProbe<ArchivedClashTournament> archivedClashTournamentPublisherProbe = PublisherProbe.of(
        Flux.fromIterable(expectedArchivedClashTournaments));
    when(archivedClashTournamentDao.save(expectedArchivedClashTournaments.get(0)))
        .thenReturn(archivedClashTournamentPublisherProbe.mono());
    probes.add(archivedClashTournamentPublisherProbe);
    PublisherProbe<Void> deleteClashTeamPublisherProbe = PublisherProbe.empty();
    when(teamDao.deleteAllById(clashTeamIds)).thenReturn(deleteClashTeamPublisherProbe.mono());
    probes.add(deleteClashTeamPublisherProbe);

    List<TentativeId> tentativeQueuesIds = tentativeQueues
        .stream()
        .map(TentativeQueue::getTentativeId)
        .collect(Collectors.toList());

    PublisherProbe<Void> deleteClashTentativeQueuePublisherProbe = PublisherProbe.empty();
    when(tentativeDao.deleteAllById(tentativeQueuesIds)).thenReturn(deleteClashTentativeQueuePublisherProbe.mono());
    probes.add(deleteClashTentativeQueuePublisherProbe);

    List<TournamentId> clashTournamentIds = expectedArchivedClashTournaments
        .stream()
        .map(ArchivedClashTournament::getTournamentId)
        .collect(Collectors.toList());

    PublisherProbe<Void> deleteClashTournamentProbe = PublisherProbe.empty();
    when(tournamentDao.deleteAllById(clashTournamentIds)).thenReturn(deleteClashTournamentProbe.mono());
    probes.add(deleteClashTournamentProbe);

    String aeId = easyRandom.nextObject(String.class);
    when(idUtils.retrieveNewArchiveExecutionId())
        .thenReturn(aeId);

    ArchiveExecution startArchiveExecution = ArchiveExecution
        .builder()
        .UUID(aeId)
        .clashTournaments(clashTournamentIds)
        .clashTournamentsArchived(clashTournamentIds.size())
        .status(ArchiveStatus.IN_PROGRESS)
        .build();

    ArchiveExecution endArchiveExecution = ArchiveExecution
        .builder()
        .UUID(aeId)
        .clashTournaments(clashTournamentIds)
        .clashTournamentsArchived(clashTournaments.size())
        .teamsArchived(clashTeams.size())
        .tentativeQueuesArchived(tentativeQueues.size())
        .status(ArchiveStatus.SUCCESSFUL)
        .build();

    PublisherProbe<ArchiveExecution> archiveExecutionProbe = PublisherProbe.of(Mono.just(startArchiveExecution));
    when(archiveExecutionDao.save(startArchiveExecution))
        .thenReturn(archiveExecutionProbe.mono());
    probes.add(archiveExecutionProbe);

    PublisherProbe<ArchiveExecution> endArchiveExecutionProbe = PublisherProbe.of(Mono.just(endArchiveExecution));
    when(archiveExecutionDao.save(endArchiveExecution))
        .thenReturn(endArchiveExecutionProbe.mono());
    probes.add(endArchiveExecutionProbe);

    StepVerifier
        .create(archivedService.archiveBasedOnInactiveTournaments())
        .expectNext(ArchiveResults
                        .builder()
                        .teamsArchived(expectedArchivedTeam)
                        .tentativeQueues(archivedTentativeQueues)
                        .inactiveTournaments(expectedArchivedClashTournaments)
                        .build())
        .verifyComplete();

    probes.forEach(PublisherProbe::assertWasSubscribed);
  }

  @Test
  @DisplayName("archiveBasedOnInactiveTournaments - If no tournaments are found, then it should return a 404.")
  void test404() {
    ArrayList<PublisherProbe> probes = new ArrayList<>();
    PublisherProbe<ClashTournament> clashTournamentProbe = PublisherProbe.of(Flux.empty());
    probes.add(clashTournamentProbe);
    when(tournamentService.retrieveAllTournaments(false))
        .thenReturn(clashTournamentProbe.flux());

    StepVerifier
        .create(archivedService.archiveBasedOnInactiveTournaments())
        .expectError(ClashBotDbException.class)
        .verify();

    probes.forEach(PublisherProbe::assertWasSubscribed);
  }

  @Test
  @DisplayName("archiveTeamsBasedOnTournamentIds - Should save Clash teams to Archive Clash teams and delete them from Clash team table based on a Tournament Ids")
  void test5() {
    TournamentId tournamentId = easyRandom.nextObject(TournamentId.class);
    List<TournamentId> tournamentIds = List.of(tournamentId);
    ClashTeam clashTeam = easyRandom.nextObject(ClashTeam.class);
    clashTeam
        .getTeamId()
        .setTournamentId(tournamentId);
    ClashTeam clashTeamTwo = easyRandom.nextObject(ClashTeam.class);
    clashTeamTwo
        .getTeamId()
        .setTournamentId(tournamentId);
    List<ClashTeam> clashTeams = List.of(clashTeam, clashTeamTwo);
    List<ArchivedClashTeam> archivedClashTeams = clashTeams
        .stream()
        .map(teamMapper::teamToArchivedClashTeam)
        .collect(Collectors.toList());
    ArrayList<PublisherProbe> probes = new ArrayList<>();

    PublisherProbe<ClashTeam> clashTeamPublisherProbe = PublisherProbe.of(
        Flux.fromIterable(clashTeams));
    probes.add(clashTeamPublisherProbe);
    when(teamService.retrieveTeamsBasedOnListOfTournaments(tournamentIds))
        .thenReturn(clashTeamPublisherProbe.flux());

    for (ArchivedClashTeam archived :
        archivedClashTeams) {
      PublisherProbe<ArchivedClashTeam> archivedPublisherProbe = PublisherProbe.of(Mono.just(archived));
      when(archivedClashTeamDao.save(archived)).thenReturn(archivedPublisherProbe.mono());
      probes.add(archivedPublisherProbe);
    }

    List<TeamId> ids = clashTeams
        .stream()
        .map(ClashTeam::getTeamId)
        .collect(Collectors.toList());

    PublisherProbe<Void> deleteClashTeamPublisherProbe = PublisherProbe.empty();
    when(teamDao.deleteAllById(ids)).thenReturn(deleteClashTeamPublisherProbe.mono());
    probes.add(deleteClashTeamPublisherProbe);

    StepVerifier
        .create(archivedService.archiveTeamsBasedOnTournamentIds(tournamentIds))
        .expectNext(archivedClashTeams)
        .verifyComplete();

    probes.forEach(PublisherProbe::assertWasSubscribed);
  }

  @Test
  @DisplayName("archiveTentativeQueueBasedOnTournamentIds - Should save Tentative Queue to Archive Tentative Queue and delete them from Tentative Queue table based on a Tournament Ids")
  void test6() {
    TournamentId tournamentId = easyRandom.nextObject(TournamentId.class);
    List<TournamentId> tournamentIds = List.of(tournamentId);
    TentativeQueue tentativeQueue = easyRandom.nextObject(TentativeQueue.class);
    tentativeQueue
        .getTentativeId()
        .setTournamentId(tournamentId);
    TentativeQueue tentativeQueueTwo = easyRandom.nextObject(TentativeQueue.class);
    tentativeQueueTwo
        .getTentativeId()
        .setTournamentId(tournamentId);
    List<TentativeQueue> tentativeQueues = List.of(tentativeQueue, tentativeQueueTwo);
    List<ArchivedTentativeQueue> archivedTentativeQueues = tentativeQueues
        .stream()
        .map(tentativeMapper::tentativeQueueToArchivedTentativeQueue)
        .collect(Collectors.toList());
    ArrayList<PublisherProbe> probes = new ArrayList<>();

    PublisherProbe<TentativeQueue> tentativeQueuePublisherProbe = PublisherProbe.of(
        Flux.fromIterable(tentativeQueues));
    probes.add(tentativeQueuePublisherProbe);
    when(tentativeService.retrieveTentativeQueuesBasedOnTournaments(tournamentIds))
        .thenReturn(tentativeQueuePublisherProbe.flux());

    for (ArchivedTentativeQueue archived :
        archivedTentativeQueues) {
      PublisherProbe<ArchivedTentativeQueue> archivedPublisherProbe = PublisherProbe.of(Mono.just(archived));
      when(archivedTentativeQueueDao.save(archived)).thenReturn(archivedPublisherProbe.mono());
      probes.add(archivedPublisherProbe);
    }

    List<TentativeId> ids = tentativeQueues
        .stream()
        .map(TentativeQueue::getTentativeId)
        .collect(Collectors.toList());

    PublisherProbe<Void> deleteClashTentativeQueuePublisherProbe = PublisherProbe.empty();
    when(tentativeDao.deleteAllById(ids)).thenReturn(deleteClashTentativeQueuePublisherProbe.mono());
    probes.add(deleteClashTentativeQueuePublisherProbe);

    StepVerifier
        .create(archivedService.archiveTentativeQueuesBasedOnTournamentIds(tournamentIds))
        .expectNext(archivedTentativeQueues)
        .verifyComplete();

    probes.forEach(PublisherProbe::assertWasSubscribed);
  }

  @Test
  @DisplayName("archivedClashTournaments - Should save Clash Tournaments to Archived Clash Tournaments and delete them from Clash Tournaments table")
  void test7() {
    ArchivedClashTournament archivedClashTournament = easyRandom.nextObject(ArchivedClashTournament.class);
    ArchivedClashTournament archivedClashTournament1 = easyRandom.nextObject(ArchivedClashTournament.class);
    List<ArchivedClashTournament> archivedClashTournaments = List.of(archivedClashTournament, archivedClashTournament1);
    ArrayList<PublisherProbe> probes = new ArrayList<>();

    for (ArchivedClashTournament archived :
        archivedClashTournaments) {
      PublisherProbe<ArchivedClashTournament> archivedClashTournamentPublisherProbe = PublisherProbe.of(
          Mono.just(archived));
      probes.add(archivedClashTournamentPublisherProbe);
      when(archivedClashTournamentDao.save(archived))
          .thenReturn(archivedClashTournamentPublisherProbe.mono());
    }

    List<TournamentId> ids = archivedClashTournaments
        .stream()
        .map(ArchivedClashTournament::getTournamentId)
        .collect(Collectors.toList());

    PublisherProbe<Void> deleteClashTournamentProbe = PublisherProbe.empty();
    when(tournamentDao.deleteAllById(ids)).thenReturn(deleteClashTournamentProbe.mono());
    probes.add(deleteClashTournamentProbe);

    StepVerifier
        .create(archivedService.archivedClashTournaments(archivedClashTournaments))
        .expectNext(archivedClashTournaments)
        .verifyComplete();

    probes.forEach(PublisherProbe::assertWasSubscribed);
  }

  @Test
  @DisplayName("archiveUserAssociationsBasedOnTournamentIds - Should save User associations to Archive User associations and delete them from User associations table based on a Tournament Ids")
  void test_archiveUserAssociationsBasedOnTournamentIds() {
    UserAssociation userAssociation = easyRandom.nextObject(UserAssociation.class);
    UserAssociation userAssociation2 = easyRandom.nextObject(UserAssociation.class);
    List<UserAssociation> userAssociationsToArchive = List.of(userAssociation, userAssociation2);
    ArrayList<PublisherProbe> probes = new ArrayList<>();

    List<UserAssociationKey> userAssociationKeys = userAssociationsToArchive
        .stream()
        .map(UserAssociation::getUserAssociationKey)
        .collect(Collectors.toList());

    List<TournamentId> tournamentIds = userAssociationKeys
        .stream()
        .map(UserAssociationKey::getTournamentId)
        .collect(Collectors.toList());

    List<ArchivedUserAssociation> archivedUserAssociations = userAssociationsToArchive
        .stream()
        .map(userAssociationMapper::userAssociationToArchivedUserAssociation)
        .collect(Collectors.toList());

    PublisherProbe<UserAssociation> userAssociationPublisherProbe = PublisherProbe.of(
        Flux.fromIterable(userAssociationsToArchive));
    when(userAssociationService.retrieveUserAssociationsForATournament(tournamentIds))
        .thenReturn(userAssociationPublisherProbe.flux());
    probes.add(userAssociationPublisherProbe);

    for (ArchivedUserAssociation archivedUserAssociation :
        archivedUserAssociations) {
      PublisherProbe<ArchivedUserAssociation> archivedUserAssociationPublisherProbe = PublisherProbe.of(
          Mono.just(archivedUserAssociation));
      probes.add(archivedUserAssociationPublisherProbe);
      when(archivedUserAssociationDao.save(archivedUserAssociation))
          .thenReturn(archivedUserAssociationPublisherProbe.mono());
    }

    PublisherProbe<Void> deleteUserAssociationProbes = PublisherProbe.empty();
    when(userAssociationDao.deleteAllById(userAssociationKeys)).thenReturn(deleteUserAssociationProbes.mono());
    probes.add(deleteUserAssociationProbes);

    StepVerifier
        .create(archivedService.archiveUserAssociationsBasedOnTournamentIds(tournamentIds))
        .expectNext(archivedUserAssociations)
        .verifyComplete();
    probes.forEach(PublisherProbe::assertWasSubscribed);
  }

  @Nested
  class IsInactiveTournament {

    @Test
    @DisplayName("isInactiveTournament - Should return false if the startTime of the Tournament is after the current time")
    void test5() {
      ClashTournament clashTournament = easyRandom.nextObject(ClashTournament.class);
      clashTournament.setStartTime(Instant
                                       .now()
                                       .plus(Duration.ofMinutes(30)));
      assertFalse(archivedService.isInactiveTournament(clashTournament));
    }

    @Test
    @DisplayName("isInactiveTournament - Should return true if the startTime of the Tournament is before the current time")
    void test6() {
      ClashTournament clashTournament = easyRandom.nextObject(ClashTournament.class);
      clashTournament.setStartTime(Instant
                                       .now()
                                       .minus(Duration.ofMinutes(30)));
      assertTrue(archivedService.isInactiveTournament(clashTournament));
    }

  }

}
