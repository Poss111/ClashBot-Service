package com.poss.clash.bot.services;

import com.poss.clash.bot.ClashBotTestingConfig;
import com.poss.clash.bot.daos.TentativeDao;
import com.poss.clash.bot.daos.models.*;
import com.poss.clash.bot.utils.IdUtils;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.publisher.PublisherProbe;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@Import(ClashBotTestingConfig.class)
class TentativeServiceTest {

  @InjectMocks
  TentativeService tentativeService;

  @Mock
  TentativeDao tentativeDao;

  @Mock
  UserAssociationService userAssociationService;

  @Mock
  IdUtils idUtils;

  @Autowired
  EasyRandom easyRandom;

  @Nested
  @DisplayName("Query")
  class Query {

    @Test
    @DisplayName("findById - should be able to filter by Tentative Id")
    void test_findById_shouldAcceptATentativeQueueIdToFilterBy() {
      TentativeQueue tentativeQueue = easyRandom.nextObject(TentativeQueue.class);

      when(tentativeDao.findByTentativeId_TentativeId(tentativeQueue
                                                          .getTentativeId()
                                                          .getTentativeId()))
          .thenReturn(Mono.just(tentativeQueue));

      StepVerifier
          .create(tentativeService.findById(tentativeQueue
                                                .getTentativeId()
                                                .getTentativeId()))
          .expectNext(tentativeQueue)
          .verifyComplete();
    }

    @Test
    @DisplayName("retrieveTentativeQueues - Filter by Discord Id")
    void test_retrieveTentativeQueues_ifDiscordIdIsPassed_shouldInvokeFilterByDiscordId() {
      String discordId = easyRandom.nextObject(String.class);

      List<TentativeQueue> tentativeQueues = List.of(easyRandom.nextObject(TentativeQueue.class));

      when(tentativeDao.findByDiscordIdsContaining(discordId))
          .thenReturn(Mono
                          .just(tentativeQueues)
                          .flatMapMany(Flux::fromIterable));

      StepVerifier
          .create(tentativeService.retrieveTentativeQueues(discordId, null, null, null))
          .expectNext(tentativeQueues.get(0))
          .verifyComplete();

      verify(tentativeDao, times(1))
          .findByDiscordIdsContaining(discordId);
    }

    @Test
    @DisplayName("retrieveTentativeQueues - Filter by Discord Id, Server Id, Tournament name and Tournament day")
    void test_retrieveTentativeQueues_ifDiscordIdServerIdTournamentNameAndTournamentDayArePassed_shouldInvokeFilterByDiscordIdServerIdTournamentNameAndTournamentDay() {
      String discordId = easyRandom.nextObject(String.class);
      String serverId = easyRandom.nextObject(String.class);

      List<TentativeQueue> tentativeQueues = List.of(easyRandom.nextObject(TentativeQueue.class));

      TournamentId tournamentId = tentativeQueues
          .get(0)
          .getTentativeId()
          .getTournamentId();

      when(
          tentativeDao.findByTentativeId_ServerId_AndTentativeId_TournamentId_TournamentName_AndTentativeId_TournamentId_TournamentDay_AndDiscordIdsContaining(
              serverId,
              tournamentId.getTournamentName(),
              tournamentId.getTournamentDay(),
              discordId))
          .thenReturn(Mono
                          .just(tentativeQueues)
                          .flatMapMany(Flux::fromIterable));

      StepVerifier
          .create(tentativeService.retrieveTentativeQueues(discordId, serverId, tournamentId.getTournamentName(),
                                                           tournamentId.getTournamentDay()))
          .expectNext(tentativeQueues.get(0))
          .verifyComplete();

      verify(tentativeDao, times(1))
          .findByTentativeId_ServerId_AndTentativeId_TournamentId_TournamentName_AndTentativeId_TournamentId_TournamentDay_AndDiscordIdsContaining(
              serverId,
              tournamentId.getTournamentName(),
              tournamentId.getTournamentDay(),
              discordId
          );
    }

    @Test
    @DisplayName("retrieveTentativeQueues - Filter by Discord Id, Tournament name and Tournament day")
    void test_retrieveTentativeQueues_ifDiscordIdTournamentNameAndTournamentDayArePassed_shouldInvokeFilterByDiscordIdTournamentNameAndTournamentDay() {
      String discordId = easyRandom.nextObject(String.class);

      List<TentativeQueue> tentativeQueues = List.of(easyRandom.nextObject(TentativeQueue.class));

      TournamentId tournamentId = tentativeQueues
          .get(0)
          .getTentativeId()
          .getTournamentId();

      when(
          tentativeDao.findByTentativeId_TournamentId_TournamentName_AndTentativeId_TournamentId_TournamentDay_AndDiscordIdsContaining(
              tournamentId.getTournamentName(),
              tournamentId.getTournamentDay(),
              discordId))
          .thenReturn(Mono
                          .just(tentativeQueues)
                          .flatMapMany(Flux::fromIterable));

      StepVerifier
          .create(tentativeService.retrieveTentativeQueues(discordId, null, tournamentId.getTournamentName(),
                                                           tournamentId.getTournamentDay()))
          .expectNext(tentativeQueues.get(0))
          .verifyComplete();

      verify(tentativeDao, times(1))
          .findByTentativeId_TournamentId_TournamentName_AndTentativeId_TournamentId_TournamentDay_AndDiscordIdsContaining(
              tournamentId.getTournamentName(),
              tournamentId.getTournamentDay(),
              discordId
          );
    }

    @Test
    @DisplayName("retrieveTentativeQueues - Filter by Discord Id, Server Id and Tournament day")
    void test_retrieveTentativeQueues_ifDiscordIdServerIdAndTournamentDayArePassed_shouldInvokeFilterByDiscordIdServerIdAndTournamentDay() {
      String discordId = easyRandom.nextObject(String.class);
      String serverId = easyRandom.nextObject(String.class);

      List<TentativeQueue> tentativeQueues = List.of(easyRandom.nextObject(TentativeQueue.class));

      TournamentId tournamentId = tentativeQueues
          .get(0)
          .getTentativeId()
          .getTournamentId();

      when(tentativeDao.findByTentativeId_ServerId_AndTentativeId_TournamentId_TournamentDay_AndDiscordIdsContaining(
          serverId,
          tournamentId.getTournamentDay(),
          discordId))
          .thenReturn(Mono
                          .just(tentativeQueues)
                          .flatMapMany(Flux::fromIterable));

      StepVerifier
          .create(tentativeService.retrieveTentativeQueues(discordId, serverId, null, tournamentId.getTournamentDay()))
          .expectNext(tentativeQueues.get(0))
          .verifyComplete();

      verify(tentativeDao, times(1))
          .findByTentativeId_ServerId_AndTentativeId_TournamentId_TournamentDay_AndDiscordIdsContaining(
              serverId,
              tournamentId.getTournamentDay(),
              discordId
          );
    }

    @Test
    @DisplayName("retrieveTentativeQueues - Filter by Discord Id, Server Id and Tournament name")
    void test_retrieveTentativeQueues_ifDiscordIdServerIdAndTournamentNameArePassed_shouldInvokeFilterByDiscordIdServerIdAndTournamentName() {
      String discordId = easyRandom.nextObject(String.class);
      String serverId = easyRandom.nextObject(String.class);

      List<TentativeQueue> tentativeQueues = List.of(easyRandom.nextObject(TentativeQueue.class));

      TournamentId tournamentId = tentativeQueues
          .get(0)
          .getTentativeId()
          .getTournamentId();

      when(tentativeDao.findByTentativeId_ServerId_AndTentativeId_TournamentId_TournamentName_AndDiscordIdsContaining(
          serverId,
          tournamentId.getTournamentName(),
          discordId))
          .thenReturn(Mono
                          .just(tentativeQueues)
                          .flatMapMany(Flux::fromIterable));

      StepVerifier
          .create(tentativeService.retrieveTentativeQueues(discordId, serverId, tournamentId.getTournamentName(), null))
          .expectNext(tentativeQueues.get(0))
          .verifyComplete();

      verify(tentativeDao, times(1))
          .findByTentativeId_ServerId_AndTentativeId_TournamentId_TournamentName_AndDiscordIdsContaining(
              serverId,
              tournamentId.getTournamentName(),
              discordId
          );
    }

    @Test
    @DisplayName("retrieveTentativeQueues - Filter by Discord Id and Server Id")
    void test_retrieveTentativeQueues_ifDiscordIdAndServerIdArePassed_shouldInvokeFilterByDiscordIdAndServerId() {
      String discordId = easyRandom.nextObject(String.class);
      String serverId = easyRandom.nextObject(String.class);

      List<TentativeQueue> tentativeQueues = List.of(easyRandom.nextObject(TentativeQueue.class));
      when(tentativeDao.findByTentativeId_ServerId_AndDiscordIdsContaining(
          serverId,
          discordId))
          .thenReturn(Mono
                          .just(tentativeQueues)
                          .flatMapMany(Flux::fromIterable));

      StepVerifier
          .create(tentativeService.retrieveTentativeQueues(discordId, serverId, null, null))
          .expectNext(tentativeQueues.get(0))
          .verifyComplete();

      verify(tentativeDao, times(1))
          .findByTentativeId_ServerId_AndDiscordIdsContaining(
              serverId,
              discordId
          );
    }

    @Test
    @DisplayName("retrieveTentativeQueues - Filter by Discord Id and Tournament name")
    void test_retrieveTentativeQueues_ifDiscordIdAndTournamentNameArePassed_shouldInvokeFilterByDiscordIdAndTournamentName() {
      String discordId = easyRandom.nextObject(String.class);

      List<TentativeQueue> tentativeQueues = List.of(easyRandom.nextObject(TentativeQueue.class));

      TournamentId tournamentId = tentativeQueues
          .get(0)
          .getTentativeId()
          .getTournamentId();

      when(tentativeDao.findByTentativeId_TournamentId_TournamentName_AndDiscordIdsContaining(
          tournamentId.getTournamentName(),
          discordId))
          .thenReturn(Mono
                          .just(tentativeQueues)
                          .flatMapMany(Flux::fromIterable));

      StepVerifier
          .create(tentativeService.retrieveTentativeQueues(discordId, null, tournamentId.getTournamentName(), null))
          .expectNext(tentativeQueues.get(0))
          .verifyComplete();

      verify(tentativeDao, times(1))
          .findByTentativeId_TournamentId_TournamentName_AndDiscordIdsContaining(
              tournamentId.getTournamentName(),
              discordId
          );
    }

    @Test
    @DisplayName("retrieveTentativeQueues - Filter by Discord Id and Tournament day")
    void test_retrieveTentativeQueues_ifDiscordIdAndTournamentDayArePassed_shouldInvokeFilterByDiscordIdAndTournamentDay() {
      String discordId = easyRandom.nextObject(String.class);

      List<TentativeQueue> tentativeQueues = List.of(easyRandom.nextObject(TentativeQueue.class));

      TournamentId tournamentId = tentativeQueues
          .get(0)
          .getTentativeId()
          .getTournamentId();

      when(tentativeDao.findByTentativeId_TournamentId_TournamentDay_AndDiscordIdsContaining(
          tournamentId.getTournamentDay(),
          discordId))
          .thenReturn(Mono
                          .just(tentativeQueues)
                          .flatMapMany(Flux::fromIterable));

      StepVerifier
          .create(tentativeService.retrieveTentativeQueues(discordId, null, null, tournamentId.getTournamentDay()))
          .expectNext(tentativeQueues.get(0))
          .verifyComplete();

      verify(tentativeDao, times(1))
          .findByTentativeId_TournamentId_TournamentDay_AndDiscordIdsContaining(
              tournamentId.getTournamentDay(),
              discordId
          );
    }

    @Test
    @DisplayName("retrieveTentativeQueues - Filter by Server Id, Tournament name and Tournament day")
    void test_retrieveTentativeQueues_ifServerIdTournamentNameAndTournamentDayArePassed_shouldInvokeFilterByServerIdTournamentNameAndTournamentDay() {
      String serverId = easyRandom.nextObject(String.class);

      List<TentativeQueue> tentativeQueues = List.of(easyRandom.nextObject(TentativeQueue.class));

      TournamentId tournamentId = tentativeQueues
          .get(0)
          .getTentativeId()
          .getTournamentId();
      when(
          tentativeDao.findByTentativeId_ServerId_AndTentativeId_TournamentId_TournamentName_AndTentativeId_TournamentId_TournamentDay(
              serverId,
              tournamentId.getTournamentName(),
              tournamentId.getTournamentDay()))
          .thenReturn(Mono
                          .just(tentativeQueues)
                          .flatMapMany(Flux::fromIterable));

      StepVerifier
          .create(tentativeService.retrieveTentativeQueues(null, serverId, tournamentId.getTournamentName(),
                                                           tournamentId.getTournamentDay()))
          .expectNext(tentativeQueues.get(0))
          .verifyComplete();

      verify(tentativeDao, times(1))
          .findByTentativeId_ServerId_AndTentativeId_TournamentId_TournamentName_AndTentativeId_TournamentId_TournamentDay(
              serverId,
              tournamentId.getTournamentName(),
              tournamentId.getTournamentDay()
          );
    }

    @Test
    @DisplayName("retrieveTentativeQueues - Filter by Server Id and Tournament name")
    void test_retrieveTentativeQueues_ifServerIdAndTournamentNameArePassed_shouldInvokeFilterByServerIdAndTournamentName() {
      String serverId = easyRandom.nextObject(String.class);

      List<TentativeQueue> tentativeQueues = List.of(easyRandom.nextObject(TentativeQueue.class));

      TournamentId tournamentId = tentativeQueues
          .get(0)
          .getTentativeId()
          .getTournamentId();
      when(tentativeDao.findByTentativeId_ServerId_AndTentativeId_TournamentId_TournamentName(
          serverId,
          tournamentId.getTournamentName()))
          .thenReturn(Mono
                          .just(tentativeQueues)
                          .flatMapMany(Flux::fromIterable));

      StepVerifier
          .create(tentativeService.retrieveTentativeQueues(null, serverId, tournamentId.getTournamentName(), null))
          .expectNext(tentativeQueues.get(0))
          .verifyComplete();

      verify(tentativeDao, times(1))
          .findByTentativeId_ServerId_AndTentativeId_TournamentId_TournamentName(
              serverId,
              tournamentId.getTournamentName()
          );
    }

    @Test
    @DisplayName("retrieveTentativeQueues - Filter by Server Id and Tournament day")
    void test_retrieveTentativeQueues_ifServerIdAndTournamentDayArePassed_shouldInvokeFilterByServerIdAndTournamentDay() {
      String serverId = easyRandom.nextObject(String.class);

      List<TentativeQueue> tentativeQueues = List.of(easyRandom.nextObject(TentativeQueue.class));

      TournamentId tournamentId = tentativeQueues
          .get(0)
          .getTentativeId()
          .getTournamentId();
      when(tentativeDao.findByTentativeId_ServerId_AndTentativeId_TournamentId_TournamentDay(
          serverId,
          tournamentId.getTournamentDay()))
          .thenReturn(Mono
                          .just(tentativeQueues)
                          .flatMapMany(Flux::fromIterable));

      StepVerifier
          .create(tentativeService.retrieveTentativeQueues(null, serverId, null, tournamentId.getTournamentDay()))
          .expectNext(tentativeQueues.get(0))
          .verifyComplete();

      verify(tentativeDao, times(1))
          .findByTentativeId_ServerId_AndTentativeId_TournamentId_TournamentDay(
              serverId,
              tournamentId.getTournamentDay()
          );
    }

    @Test
    @DisplayName("retrieveTentativeQueues - Filter by Tournament name and Tournament day")
    void test_retrieveTentativeQueues_ifTournamentNameAndTournamentDayArePassed_shouldInvokeFilterByTournamentNameAndTournamentDay() {
      List<TentativeQueue> tentativeQueues = List.of(easyRandom.nextObject(TentativeQueue.class));

      TournamentId tournamentId = tentativeQueues
          .get(0)
          .getTentativeId()
          .getTournamentId();
      when(tentativeDao.findByTentativeId_TournamentId_TournamentName_AndTentativeId_TournamentId_TournamentDay(
          tournamentId.getTournamentName(),
          tournamentId.getTournamentDay()))
          .thenReturn(Mono
                          .just(tentativeQueues)
                          .flatMapMany(Flux::fromIterable));

      StepVerifier
          .create(tentativeService.retrieveTentativeQueues(null, null, tournamentId.getTournamentName(),
                                                           tournamentId.getTournamentDay()))
          .expectNext(tentativeQueues.get(0))
          .verifyComplete();

      verify(tentativeDao, times(1))
          .findByTentativeId_TournamentId_TournamentName_AndTentativeId_TournamentId_TournamentDay(
              tournamentId.getTournamentName(),
              tournamentId.getTournamentDay()
          );
    }

    @Test
    @DisplayName("retrieveTentativeQueues - Filter by Server Id")
    void test_retrieveTentativeQueues_ifServerIdIsPassed_shouldInvokeFilterByServerId() {
      String serverId = easyRandom.nextObject(String.class);

      List<TentativeQueue> tentativeQueues = List.of(easyRandom.nextObject(TentativeQueue.class));

      when(tentativeDao.findByTentativeId_ServerId(
          serverId))
          .thenReturn(Mono
                          .just(tentativeQueues)
                          .flatMapMany(Flux::fromIterable));

      StepVerifier
          .create(tentativeService.retrieveTentativeQueues(null, serverId, null, null))
          .expectNext(tentativeQueues.get(0))
          .verifyComplete();

      verify(tentativeDao, times(1))
          .findByTentativeId_ServerId(serverId);
    }

    @Test
    @DisplayName("retrieveTentativeQueues - Filter by Tournament name")
    void test_retrieveTentativeQueues_ifTournamentNameIsPassed_shouldInvokeFilterByTournamentName() {
      List<TentativeQueue> tentativeQueues = List.of(easyRandom.nextObject(TentativeQueue.class));

      TournamentId tournamentId = tentativeQueues
          .get(0)
          .getTentativeId()
          .getTournamentId();
      when(tentativeDao.findByTentativeId_TournamentId_TournamentName(
          tournamentId.getTournamentName()))
          .thenReturn(Mono
                          .just(tentativeQueues)
                          .flatMapMany(Flux::fromIterable));

      StepVerifier
          .create(tentativeService.retrieveTentativeQueues(null, null, tournamentId.getTournamentName(), null))
          .expectNext(tentativeQueues.get(0))
          .verifyComplete();

      verify(tentativeDao, times(1))
          .findByTentativeId_TournamentId_TournamentName(tournamentId.getTournamentName());
    }

    @Test
    @DisplayName("retrieveTentativeQueues - Filter by Tournament day")
    void test_retrieveTentativeQueues_ifTournamentDayIsPassed_shouldInvokeFilterByTournamentDay() {
      List<TentativeQueue> tentativeQueues = List.of(easyRandom.nextObject(TentativeQueue.class));

      TournamentId tournamentId = tentativeQueues
          .get(0)
          .getTentativeId()
          .getTournamentId();
      when(tentativeDao.findByTentativeId_TournamentId_TournamentDay(
          tournamentId.getTournamentDay()))
          .thenReturn(Mono
                          .just(tentativeQueues)
                          .flatMapMany(Flux::fromIterable));

      StepVerifier
          .create(tentativeService.retrieveTentativeQueues(null, null, null, tournamentId.getTournamentDay()))
          .expectNext(tentativeQueues.get(0))
          .verifyComplete();

      verify(tentativeDao, times(1))
          .findByTentativeId_TournamentId_TournamentDay(tournamentId.getTournamentDay());
    }

    @Test
    @DisplayName("retrieveTentativeQueues - Filter by All")
    void test_retrieveTentativeQueues_ifAllNullArePassed_shouldInvokeFilterByAll() {
      List<TentativeQueue> tentativeQueues = List.of(easyRandom.nextObject(TentativeQueue.class));

      when(tentativeDao.findAll())
          .thenReturn(Mono
                          .just(tentativeQueues)
                          .flatMapMany(Flux::fromIterable));

      StepVerifier
          .create(tentativeService.retrieveTentativeQueues(null, null, null, null))
          .expectNext(tentativeQueues.get(0))
          .verifyComplete();

      verify(tentativeDao, times(1)).findAll();
    }

    @Test
    @DisplayName("retrieveTentativeQueuesBasedOnTournaments - Should return all tenantive queues based on all Tournament Ids")
    void test_N() {
      ClashTournament tournamentOne = easyRandom.nextObject(ClashTournament.class);
      ClashTournament tournamentTwo = easyRandom.nextObject(ClashTournament.class);
      TentativeQueue tentativeQueueOne = easyRandom.nextObject(TentativeQueue.class);
      tentativeQueueOne
          .getTentativeId()
          .setTournamentId(tournamentOne.getTournamentId());
      TentativeQueue tentativeQueueTwo = easyRandom.nextObject(TentativeQueue.class);
      tentativeQueueTwo
          .getTentativeId()
          .setTournamentId(tournamentTwo.getTournamentId());

      List<TournamentId> tournamentIds = List.of(tournamentOne.getTournamentId(), tournamentTwo.getTournamentId());
      List<TentativeQueue> tentativeQueues = List.of(tentativeQueueOne, tentativeQueueTwo);
      PublisherProbe<TentativeQueue> findAllByTentativeIdProbe = PublisherProbe.of(
          Flux.fromIterable(tentativeQueues));
      when(tentativeDao.findAllByTentativeId_TournamentIdIsIn(tournamentIds))
          .thenReturn(findAllByTentativeIdProbe.flux());

      StepVerifier
          .create(tentativeService.retrieveTentativeQueuesBasedOnTournaments(tournamentIds))
          .recordWith(ArrayList::new)
          .expectNextCount(2)
          .consumeRecordedWith(response -> assertEquals(tentativeQueues, response))
          .verifyComplete();

      findAllByTentativeIdProbe.assertWasSubscribed();
    }

  }

  @Nested
  @DisplayName("Update")
  class Update {

    @Test
    @DisplayName("assignUserToTentativeQueue - If id does not belong on Tentative Queue then assign id to set of Discord Ids.")
    void test_assignUserToTentativeQueue_ifIdDoesNotExistOnTentativeQueueListThenInvokeUpdate() {
      String discordId = easyRandom.nextObject(String.class);
      TentativeQueue tentativeQueue = easyRandom.nextObject(TentativeQueue.class);
      tentativeQueue
          .getDiscordIds()
          .clear();
      TentativeQueue expectedTentativeQueue = TentativeQueue
          .builder()
          .tentativeId(tentativeQueue.getTentativeId())
          .discordIds(Set.of(discordId))
          .build();
      when(tentativeDao.updateByTentativeId_TentativeId(tentativeQueue
                                                            .getTentativeId()
                                                            .getTentativeId(), discordId))
          .thenReturn(Mono.just(1L));

      StepVerifier
          .create(tentativeService.assignUserToTentativeQueue(discordId, tentativeQueue))
          .expectNext(expectedTentativeQueue)
          .verifyComplete();
    }

    @Test
    @DisplayName("removeUserFromTentativeQueue - If id belongs on Tentative Queue then remove the id from the set of Discord Ids.")
    void test_removeUserFromTentativeQueue_ifIdDoesExistOnTentativeQueueListThenInvokeUpdate() {
      String discordId = easyRandom.nextObject(String.class);
      TentativeQueue tentativeQueue = easyRandom.nextObject(TentativeQueue.class);
      tentativeQueue
          .getDiscordIds()
          .clear();
      tentativeQueue
          .getDiscordIds()
          .add(discordId);
      when(tentativeDao.findByTentativeId_TentativeId(tentativeQueue
                                                          .getTentativeId()
                                                          .getTentativeId()))
          .thenReturn(Mono.just(tentativeQueue));
      when(tentativeDao.removeByTentativeId_TentativeId(tentativeQueue
                                                            .getTentativeId()
                                                            .getTentativeId(), discordId))
          .thenReturn(Mono.just(1L));
      PublisherProbe<Void> voidMono = PublisherProbe.empty();
      when(userAssociationService.delete(UserAssociationKey
                                             .builder()
                                             .tournamentId(tentativeQueue
                                                               .getTentativeId()
                                                               .getTournamentId())
                                             .discordId(discordId)
                                             .build()))
          .thenReturn(voidMono.mono());

      StepVerifier
          .create(tentativeService.removeUserFromTentativeQueue(discordId, tentativeQueue
              .getTentativeId()
              .getTentativeId()))
          .expectNext(tentativeQueue)
          .verifyComplete();
    }

  }

  @Nested
  @DisplayName("Save")
  class Save {

    @Test
    @DisplayName("Should be able to save tentative Queues and map them back to Tentative")
    void test_mapBackToTentative() {
      String mockTentativeId = "tq-123asdf";
      TentativeQueue tentativeQueue = easyRandom.nextObject(TentativeQueue.class);
      tentativeQueue
          .getTentativeId()
          .setTentativeId(null);

      ClashTeam clashTeam = easyRandom.nextObject(ClashTeam.class);
      clashTeam
          .getTeamId()
          .setTournamentId(tentativeQueue
                               .getTentativeId()
                               .getTournamentId());
      when(idUtils.retrieveNewTentativeQueueId()).thenReturn(mockTentativeId);
      when(tentativeDao.save(tentativeQueue))
          .thenReturn(Mono.just(tentativeQueue));

      StepVerifier
          .create(tentativeService.createTentativeQueue(tentativeQueue))
          .expectNext(tentativeQueue)
          .expectComplete()
          .verify();
    }

  }

}
