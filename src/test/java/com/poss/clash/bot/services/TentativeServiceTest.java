package com.poss.clash.bot.services;

import com.poss.clash.bot.ClashBotTestingConfig;
import com.poss.clash.bot.daos.TentativeDao;
import com.poss.clash.bot.daos.models.*;
import com.poss.clash.bot.exceptions.ClashBotDbException;
import com.poss.clash.bot.openapi.model.BaseTournament;
import com.poss.clash.bot.openapi.model.Tentative;
import com.poss.clash.bot.openapi.model.TentativePlayer;
import com.poss.clash.bot.utils.IdUtils;
import com.poss.clash.bot.utils.TentativeMapper;
import com.poss.clash.bot.utils.UserAssociationMapper;
import org.apache.commons.lang3.StringUtils;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.*;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Example;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@Import(ClashBotTestingConfig.class)
class TentativeServiceTest {

    @InjectMocks
    TentativeService tentativeService;

    @Mock
    TentativeDao tentativeDao;

    @Mock
    TeamService teamService;

    @Mock
    UserAssociationService userAssociationService;

    @Mock
    IdUtils idUtils;

    @Spy
    TentativeMapper tentativeMapper = Mappers.getMapper(TentativeMapper.class);

    @Spy
    UserAssociationMapper userAssociationMapper = Mappers.getMapper(UserAssociationMapper.class);

    @Autowired
    EasyRandom easyRandom;

    @Captor
    private ArgumentCaptor<Function<UserAssociation, Publisher<UserAssociation>>> functionArgumentCaptor;

    @Captor
    private ArgumentCaptor<Collection<Integer>> discordIdsCaptor;

    @Captor
    private ArgumentCaptor<TournamentId> tournamentIdCaptor;

    @Captor
    private ArgumentCaptor<UserAssociation> userAssociationCaptor;

    @Test
    void test_retrieveTentativeQueues_retrieveAllQueues() {
        TentativeQueue tentativeQueue = easyRandom.nextObject(TentativeQueue.class);
        TentativeQueue tentativeQueue2 = easyRandom.nextObject(TentativeQueue.class);
        TentativeQueue tentativeQueue3 = easyRandom.nextObject(TentativeQueue.class);
        TentativeQueue tentativeQueue4 = easyRandom.nextObject(TentativeQueue.class);

        Tentative tentative = tentativeQueueToTentative(tentativeQueue);
        Tentative tentative1 = tentativeQueueToTentative(tentativeQueue2);
        Tentative tentative2 = tentativeQueueToTentative(tentativeQueue3);
        Tentative tentative3 = tentativeQueueToTentative(tentativeQueue4);


        Flux<TentativeQueue> dbResponse = Flux.just(tentativeQueue, tentativeQueue2, tentativeQueue3, tentativeQueue4);

        when(tentativeDao.findAll())
                .thenReturn(dbResponse);

        StepVerifier
                .create(tentativeService.retrieveTentativeQueues())
                .expectNext(tentative)
                .expectNext(tentative1)
                .expectNext(tentative2)
                .expectNext(tentative3)
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("retrieveTentativeQueuesByExample - should retrieve Tentative Queues based on the tournament name passed.")
    void test_retrieveTentativeQueuesByTournament_byTournamentName() {
        String tournamentName = "tournament1";
        TentativeQueue tentativeQueue = easyRandom.nextObject(TentativeQueue.class);
        TentativeQueue tentativeQueue2 = easyRandom.nextObject(TentativeQueue.class);
        TentativeQueue tentativeQueue3 = easyRandom.nextObject(TentativeQueue.class);
        TentativeQueue tentativeQueue4 = easyRandom.nextObject(TentativeQueue.class);

        Tentative tentative = tentativeQueueToTentative(tentativeQueue);
        Tentative tentative1 = tentativeQueueToTentative(tentativeQueue2);
        Tentative tentative2 = tentativeQueueToTentative(tentativeQueue3);
        Tentative tentative3 = tentativeQueueToTentative(tentativeQueue4);


        Flux<TentativeQueue> dbResponse = Flux.just(tentativeQueue, tentativeQueue2, tentativeQueue3, tentativeQueue4);

        TentativeQueue example = TentativeQueue.builder()
                .tentativeId(TentativeId.builder()
                                     .tournamentId(
                                             TournamentId.builder()
                                                     .tournamentName(tournamentName)
                                                     .build())
                                     .build())
                .build();

        when(tentativeDao.findAll(Example.of(example)))
                .thenReturn(dbResponse);

        StepVerifier
                .create(tentativeService.retrieveTentativeQueues(null, tournamentName, null))
                .expectNext(tentative)
                .expectNext(tentative1)
                .expectNext(tentative2)
                .expectNext(tentative3)
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("retrieveTentativeQueuesByExample - should retrieve Tentative Queues based on the tournament day passed.")
    void test_retrieveTentativeQueuesByTournament_byTournamentDay() {
        String tournamentDay = "1";
        TentativeQueue tentativeQueue = easyRandom.nextObject(TentativeQueue.class);
        TentativeQueue tentativeQueue2 = easyRandom.nextObject(TentativeQueue.class);
        TentativeQueue tentativeQueue3 = easyRandom.nextObject(TentativeQueue.class);
        TentativeQueue tentativeQueue4 = easyRandom.nextObject(TentativeQueue.class);

        Tentative tentative = tentativeQueueToTentative(tentativeQueue);
        Tentative tentative1 = tentativeQueueToTentative(tentativeQueue2);
        Tentative tentative2 = tentativeQueueToTentative(tentativeQueue3);
        Tentative tentative3 = tentativeQueueToTentative(tentativeQueue4);


        Flux<TentativeQueue> dbResponse = Flux.just(tentativeQueue, tentativeQueue2, tentativeQueue3, tentativeQueue4);

        TentativeQueue example = TentativeQueue.builder()
                .tentativeId(TentativeId.builder()
                                     .tournamentId(
                                             TournamentId.builder()
                                                     .tournamentDay(tournamentDay)
                                                     .build())
                                     .build())
                .build();

        when(tentativeDao.findAll(Example.of(example)))
                .thenReturn(dbResponse);

        StepVerifier
                .create(tentativeService.retrieveTentativeQueues(null, null, tournamentDay))
                .expectNext(tentative)
                .expectNext(tentative1)
                .expectNext(tentative2)
                .expectNext(tentative3)
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("retrieveTentativeQueuesByExample - should retrieve Tentative Queues based on the tournament details passed.")
    void test_retrieveTentativeQueuesByTournament_byTournamentDetails() {
        String tournamentName = "tournament1";
        String tournamentDay = "1";
        TentativeQueue tentativeQueue = easyRandom.nextObject(TentativeQueue.class);
        TentativeQueue tentativeQueue2 = easyRandom.nextObject(TentativeQueue.class);
        TentativeQueue tentativeQueue3 = easyRandom.nextObject(TentativeQueue.class);
        TentativeQueue tentativeQueue4 = easyRandom.nextObject(TentativeQueue.class);

        Tentative tentative = tentativeQueueToTentative(tentativeQueue);
        Tentative tentative1 = tentativeQueueToTentative(tentativeQueue2);
        Tentative tentative2 = tentativeQueueToTentative(tentativeQueue3);
        Tentative tentative3 = tentativeQueueToTentative(tentativeQueue4);


        Flux<TentativeQueue> dbResponse = Flux.just(tentativeQueue, tentativeQueue2, tentativeQueue3, tentativeQueue4);

        TentativeQueue example = TentativeQueue.builder()
                .tentativeId(TentativeId.builder()
                                     .tournamentId(
                                             TournamentId.builder()
                                                     .tournamentName(tournamentName)
                                                     .tournamentDay(tournamentDay)
                                                     .build())
                                     .build())
                .build();

        when(tentativeDao.findAll(Example.of(example)))
                .thenReturn(dbResponse);

        StepVerifier
                .create(tentativeService.retrieveTentativeQueues(null, tournamentName, tournamentDay))
                .expectNext(tentative)
                .expectNext(tentative1)
                .expectNext(tentative2)
                .expectNext(tentative3)
                .expectComplete()
                .verify();
    }

    @Nested
    @DisplayName("Query")
    class Query {

        @Test
        @DisplayName("findById - should be able to filter by Tentative Id")
        void test_findById_shouldAcceptATentativeQueueIdToFilterBy() {
            TentativeQueue tentativeQueue = easyRandom.nextObject(TentativeQueue.class);

            when(tentativeDao.findByTentativeId_TentativeId(tentativeQueue.getTentativeId().getTentativeId()))
                    .thenReturn(Mono.just(tentativeQueue));

            StepVerifier
                    .create(tentativeService.findById(tentativeQueue.getTentativeId().getTentativeId()))
                    .expectNext(tentativeMapper.tentativeQueueToTentative(tentativeQueue))
                    .verifyComplete();
        }

        @Test
        @DisplayName("retrieveTentativeQueues - Filter by Discord Id")
        void test_retrieveTentativeQueues_ifDiscordIdIsPassed_shouldInvokeFilterByDiscordId() {
            Long discordId = 1L;

            List<TentativeQueue> tentativeQueues = List.of(easyRandom.nextObject(TentativeQueue.class));

            when(tentativeDao.findByDiscordIdsContaining(discordId.intValue()))
                    .thenReturn(Mono.just(tentativeQueues).flatMapMany(Flux::fromIterable));

            List<Tentative> mappedTentatives = tentativeQueues.stream()
                    .map(tentativeMapper::tentativeQueueToTentative)
                    .collect(Collectors.toList());

            StepVerifier
                    .create(tentativeService.retrieveTentativeQueues(discordId, null, null, null))
                    .expectNext(mappedTentatives.get(0))
                    .verifyComplete();

            verify(tentativeDao, times(1))
                    .findByDiscordIdsContaining(discordId.intValue());
        }

        @Test
        @DisplayName("retrieveTentativeQueues - Filter by Discord Id, Server Id, Tournament name and Tournament day")
        void test_retrieveTentativeQueues_ifDiscordIdServerIdTournamentNameAndTournamentDayArePassed_shouldInvokeFilterByDiscordIdServerIdTournamentNameAndTournamentDay() {
            Long discordId = 1L;
            Long serverId = 1234L;

            List<TentativeQueue> tentativeQueues = List.of(easyRandom.nextObject(TentativeQueue.class));

            TournamentId tournamentId = tentativeQueues.get(0).getTentativeId().getTournamentId();

            when(tentativeDao.findByTentativeId_ServerId_AndTentativeId_TournamentId_TournamentName_AndTentativeId_TournamentId_TournamentDay_AndDiscordIdsContaining(
                    serverId.intValue(),
                    tournamentId.getTournamentName(),
                    tournamentId.getTournamentDay(),
                    discordId.intValue()))
                    .thenReturn(Mono.just(tentativeQueues).flatMapMany(Flux::fromIterable));

            List<Tentative> mappedTentatives = tentativeQueues.stream()
                    .map(tentativeMapper::tentativeQueueToTentative)
                    .collect(Collectors.toList());

            StepVerifier
                    .create(tentativeService.retrieveTentativeQueues(discordId, serverId, tournamentId.getTournamentName(), tournamentId.getTournamentDay()))
                    .expectNext(mappedTentatives.get(0))
                    .verifyComplete();

            verify(tentativeDao, times(1))
                    .findByTentativeId_ServerId_AndTentativeId_TournamentId_TournamentName_AndTentativeId_TournamentId_TournamentDay_AndDiscordIdsContaining(
                            serverId.intValue(),
                            tournamentId.getTournamentName(),
                            tournamentId.getTournamentDay(),
                            discordId.intValue()
                    );
        }

        @Test
        @DisplayName("retrieveTentativeQueues - Filter by Discord Id, Tournament name and Tournament day")
        void test_retrieveTentativeQueues_ifDiscordIdTournamentNameAndTournamentDayArePassed_shouldInvokeFilterByDiscordIdTournamentNameAndTournamentDay() {
            Long discordId = 1L;
            Long serverId = 1234L;

            List<TentativeQueue> tentativeQueues = List.of(easyRandom.nextObject(TentativeQueue.class));

            TournamentId tournamentId = tentativeQueues.get(0).getTentativeId().getTournamentId();

            when(tentativeDao.findByTentativeId_TournamentId_TournamentName_AndTentativeId_TournamentId_TournamentDay_AndDiscordIdsContaining(
                    tournamentId.getTournamentName(),
                    tournamentId.getTournamentDay(),
                    discordId.intValue()))
                    .thenReturn(Mono.just(tentativeQueues).flatMapMany(Flux::fromIterable));

            List<Tentative> mappedTentatives = tentativeQueues.stream()
                    .map(tentativeMapper::tentativeQueueToTentative)
                    .collect(Collectors.toList());

            StepVerifier
                    .create(tentativeService.retrieveTentativeQueues(discordId, null, tournamentId.getTournamentName(), tournamentId.getTournamentDay()))
                    .expectNext(mappedTentatives.get(0))
                    .verifyComplete();

            verify(tentativeDao, times(1))
                    .findByTentativeId_TournamentId_TournamentName_AndTentativeId_TournamentId_TournamentDay_AndDiscordIdsContaining(
                            tournamentId.getTournamentName(),
                            tournamentId.getTournamentDay(),
                            discordId.intValue()
                    );
        }

        @Test
        @DisplayName("retrieveTentativeQueues - Filter by Discord Id, Server Id and Tournament day")
        void test_retrieveTentativeQueues_ifDiscordIdServerIdAndTournamentDayArePassed_shouldInvokeFilterByDiscordIdServerIdAndTournamentDay() {
            Long discordId = 1L;
            Long serverId = 1234L;

            List<TentativeQueue> tentativeQueues = List.of(easyRandom.nextObject(TentativeQueue.class));

            TournamentId tournamentId = tentativeQueues.get(0).getTentativeId().getTournamentId();

            when(tentativeDao.findByTentativeId_ServerId_AndTentativeId_TournamentId_TournamentDay_AndDiscordIdsContaining(
                    serverId.intValue(),
                    tournamentId.getTournamentDay(),
                    discordId.intValue()))
                    .thenReturn(Mono.just(tentativeQueues).flatMapMany(Flux::fromIterable));

            List<Tentative> mappedTentatives = tentativeQueues.stream()
                    .map(tentativeMapper::tentativeQueueToTentative)
                    .collect(Collectors.toList());

            StepVerifier
                    .create(tentativeService.retrieveTentativeQueues(discordId, serverId, null, tournamentId.getTournamentDay()))
                    .expectNext(mappedTentatives.get(0))
                    .verifyComplete();

            verify(tentativeDao, times(1))
                    .findByTentativeId_ServerId_AndTentativeId_TournamentId_TournamentDay_AndDiscordIdsContaining(
                            serverId.intValue(),
                            tournamentId.getTournamentDay(),
                            discordId.intValue()
                    );
        }

        @Test
        @DisplayName("retrieveTentativeQueues - Filter by Discord Id, Server Id and Tournament name")
        void test_retrieveTentativeQueues_ifDiscordIdServerIdAndTournamentNameArePassed_shouldInvokeFilterByDiscordIdServerIdAndTournamentName() {
            Long discordId = 1L;
            Long serverId = 1234L;

            List<TentativeQueue> tentativeQueues = List.of(easyRandom.nextObject(TentativeQueue.class));

            TournamentId tournamentId = tentativeQueues.get(0).getTentativeId().getTournamentId();

            when(tentativeDao.findByTentativeId_ServerId_AndTentativeId_TournamentId_TournamentName_AndDiscordIdsContaining(
                    serverId.intValue(),
                    tournamentId.getTournamentName(),
                    discordId.intValue()))
                    .thenReturn(Mono.just(tentativeQueues).flatMapMany(Flux::fromIterable));

            List<Tentative> mappedTentatives = tentativeQueues.stream()
                    .map(tentativeMapper::tentativeQueueToTentative)
                    .collect(Collectors.toList());

            StepVerifier
                    .create(tentativeService.retrieveTentativeQueues(discordId, serverId, tournamentId.getTournamentName(), null))
                    .expectNext(mappedTentatives.get(0))
                    .verifyComplete();

            verify(tentativeDao, times(1))
                    .findByTentativeId_ServerId_AndTentativeId_TournamentId_TournamentName_AndDiscordIdsContaining(
                            serverId.intValue(),
                            tournamentId.getTournamentName(),
                            discordId.intValue()
                    );
        }

        @Test
        @DisplayName("retrieveTentativeQueues - Filter by Discord Id and Server Id")
        void test_retrieveTentativeQueues_ifDiscordIdAndServerIdArePassed_shouldInvokeFilterByDiscordIdAndServerId() {
            Long discordId = 1L;
            Long serverId = 1234L;

            List<TentativeQueue> tentativeQueues = List.of(easyRandom.nextObject(TentativeQueue.class));

            TournamentId tournamentId = tentativeQueues.get(0).getTentativeId().getTournamentId();

            when(tentativeDao.findByTentativeId_ServerId_AndDiscordIdsContaining(
                    serverId.intValue(),
                    discordId.intValue()))
                    .thenReturn(Mono.just(tentativeQueues).flatMapMany(Flux::fromIterable));

            List<Tentative> mappedTentatives = tentativeQueues.stream()
                    .map(tentativeMapper::tentativeQueueToTentative)
                    .collect(Collectors.toList());

            StepVerifier
                    .create(tentativeService.retrieveTentativeQueues(discordId, serverId, null, null))
                    .expectNext(mappedTentatives.get(0))
                    .verifyComplete();

            verify(tentativeDao, times(1))
                    .findByTentativeId_ServerId_AndDiscordIdsContaining(
                            serverId.intValue(),
                            discordId.intValue()
                    );
        }

        @Test
        @DisplayName("retrieveTentativeQueues - Filter by Discord Id and Tournament name")
        void test_retrieveTentativeQueues_ifDiscordIdAndTournamentNameArePassed_shouldInvokeFilterByDiscordIdAndTournamentName() {
            Long discordId = 1L;
            Long serverId = 1234L;

            List<TentativeQueue> tentativeQueues = List.of(easyRandom.nextObject(TentativeQueue.class));

            TournamentId tournamentId = tentativeQueues.get(0).getTentativeId().getTournamentId();

            when(tentativeDao.findByTentativeId_TournamentId_TournamentName_AndDiscordIdsContaining(
                    tournamentId.getTournamentName(),
                    discordId.intValue()))
                    .thenReturn(Mono.just(tentativeQueues).flatMapMany(Flux::fromIterable));

            List<Tentative> mappedTentatives = tentativeQueues.stream()
                    .map(tentativeMapper::tentativeQueueToTentative)
                    .collect(Collectors.toList());

            StepVerifier
                    .create(tentativeService.retrieveTentativeQueues(discordId, null, tournamentId.getTournamentName(), null))
                    .expectNext(mappedTentatives.get(0))
                    .verifyComplete();

            verify(tentativeDao, times(1))
                    .findByTentativeId_TournamentId_TournamentName_AndDiscordIdsContaining(
                            tournamentId.getTournamentName(),
                            discordId.intValue()
                    );
        }

        @Test
        @DisplayName("retrieveTentativeQueues - Filter by Discord Id and Tournament day")
        void test_retrieveTentativeQueues_ifDiscordIdAndTournamentDayArePassed_shouldInvokeFilterByDiscordIdAndTournamentDay() {
            Long discordId = 1L;
            Long serverId = 1234L;

            List<TentativeQueue> tentativeQueues = List.of(easyRandom.nextObject(TentativeQueue.class));

            TournamentId tournamentId = tentativeQueues.get(0).getTentativeId().getTournamentId();

            when(tentativeDao.findByTentativeId_TournamentId_TournamentDay_AndDiscordIdsContaining(
                    tournamentId.getTournamentDay(),
                    discordId.intValue()))
                    .thenReturn(Mono.just(tentativeQueues).flatMapMany(Flux::fromIterable));

            List<Tentative> mappedTentatives = tentativeQueues.stream()
                    .map(tentativeMapper::tentativeQueueToTentative)
                    .collect(Collectors.toList());

            StepVerifier
                    .create(tentativeService.retrieveTentativeQueues(discordId, null, null, tournamentId.getTournamentDay()))
                    .expectNext(mappedTentatives.get(0))
                    .verifyComplete();

            verify(tentativeDao, times(1))
                    .findByTentativeId_TournamentId_TournamentDay_AndDiscordIdsContaining(
                            tournamentId.getTournamentDay(),
                            discordId.intValue()
                    );
        }

        @Test
        @DisplayName("retrieveTentativeQueues - Filter by Server Id, Tournament name and Tournament day")
        void test_retrieveTentativeQueues_ifServerIdTournamentNameAndTournamentDayArePassed_shouldInvokeFilterByServerIdTournamentNameAndTournamentDay() {
            Long serverId = 1L;

            List<TentativeQueue> tentativeQueues = List.of(easyRandom.nextObject(TentativeQueue.class));

            TournamentId tournamentId = tentativeQueues.get(0).getTentativeId().getTournamentId();
            when(tentativeDao.findByTentativeId_ServerId_AndTentativeId_TournamentId_TournamentName_AndTentativeId_TournamentId_TournamentDay(
                    serverId.intValue(),
                    tournamentId.getTournamentName(),
                    tournamentId.getTournamentDay()))
                    .thenReturn(Mono.just(tentativeQueues).flatMapMany(Flux::fromIterable));

            List<Tentative> mappedTentatives = tentativeQueues.stream()
                    .map(tentativeMapper::tentativeQueueToTentative)
                    .collect(Collectors.toList());

            StepVerifier
                    .create(tentativeService.retrieveTentativeQueues(null, serverId, tournamentId.getTournamentName(), tournamentId.getTournamentDay()))
                    .expectNext(mappedTentatives.get(0))
                    .verifyComplete();

            verify(tentativeDao, times(1))
                    .findByTentativeId_ServerId_AndTentativeId_TournamentId_TournamentName_AndTentativeId_TournamentId_TournamentDay(
                            serverId.intValue(),
                            tournamentId.getTournamentName(),
                            tournamentId.getTournamentDay()
                    );
        }

        @Test
        @DisplayName("retrieveTentativeQueues - Filter by Server Id and Tournament name")
        void test_retrieveTentativeQueues_ifServerIdAndTournamentNameArePassed_shouldInvokeFilterByServerIdAndTournamentName() {
            Long serverId = 1L;

            List<TentativeQueue> tentativeQueues = List.of(easyRandom.nextObject(TentativeQueue.class));

            TournamentId tournamentId = tentativeQueues.get(0).getTentativeId().getTournamentId();
            when(tentativeDao.findByTentativeId_ServerId_AndTentativeId_TournamentId_TournamentName(
                    serverId.intValue(),
                    tournamentId.getTournamentName()))
                    .thenReturn(Mono.just(tentativeQueues).flatMapMany(Flux::fromIterable));

            List<Tentative> mappedTentatives = tentativeQueues.stream()
                    .map(tentativeMapper::tentativeQueueToTentative)
                    .collect(Collectors.toList());

            StepVerifier
                    .create(tentativeService.retrieveTentativeQueues(null, serverId, tournamentId.getTournamentName(), null))
                    .expectNext(mappedTentatives.get(0))
                    .verifyComplete();

            verify(tentativeDao, times(1))
                    .findByTentativeId_ServerId_AndTentativeId_TournamentId_TournamentName(
                            serverId.intValue(),
                            tournamentId.getTournamentName()
                    );
        }

        @Test
        @DisplayName("retrieveTentativeQueues - Filter by Server Id and Tournament day")
        void test_retrieveTentativeQueues_ifServerIdAndTournamentDayArePassed_shouldInvokeFilterByServerIdAndTournamentDay() {
            Long serverId = 1L;

            List<TentativeQueue> tentativeQueues = List.of(easyRandom.nextObject(TentativeQueue.class));

            TournamentId tournamentId = tentativeQueues.get(0).getTentativeId().getTournamentId();
            when(tentativeDao.findByTentativeId_ServerId_AndTentativeId_TournamentId_TournamentDay(
                    serverId.intValue(),
                    tournamentId.getTournamentDay()))
                    .thenReturn(Mono.just(tentativeQueues).flatMapMany(Flux::fromIterable));

            List<Tentative> mappedTentatives = tentativeQueues.stream()
                    .map(tentativeMapper::tentativeQueueToTentative)
                    .collect(Collectors.toList());

            StepVerifier
                    .create(tentativeService.retrieveTentativeQueues(null, serverId, null, tournamentId.getTournamentDay()))
                    .expectNext(mappedTentatives.get(0))
                    .verifyComplete();

            verify(tentativeDao, times(1))
                    .findByTentativeId_ServerId_AndTentativeId_TournamentId_TournamentDay(
                            serverId.intValue(),
                            tournamentId.getTournamentDay()
                    );
        }

        @Test
        @DisplayName("retrieveTentativeQueues - Filter by Tournament name and Tournament day")
        void test_retrieveTentativeQueues_ifTournamentNameAndTournamentDayArePassed_shouldInvokeFilterByTournamentNameAndTournamentDay() {
            List<TentativeQueue> tentativeQueues = List.of(easyRandom.nextObject(TentativeQueue.class));

            TournamentId tournamentId = tentativeQueues.get(0).getTentativeId().getTournamentId();
            when(tentativeDao.findByTentativeId_TournamentId_TournamentName_AndTentativeId_TournamentId_TournamentDay(
                    tournamentId.getTournamentName(),
                    tournamentId.getTournamentDay()))
                    .thenReturn(Mono.just(tentativeQueues).flatMapMany(Flux::fromIterable));

            List<Tentative> mappedTentatives = tentativeQueues.stream()
                    .map(tentativeMapper::tentativeQueueToTentative)
                    .collect(Collectors.toList());

            StepVerifier
                    .create(tentativeService.retrieveTentativeQueues(null, null, tournamentId.getTournamentName(), tournamentId.getTournamentDay()))
                    .expectNext(mappedTentatives.get(0))
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
            Long serverId = 1L;

            List<TentativeQueue> tentativeQueues = List.of(easyRandom.nextObject(TentativeQueue.class));

            TournamentId tournamentId = tentativeQueues.get(0).getTentativeId().getTournamentId();
            when(tentativeDao.findByTentativeId_ServerId(
                    serverId.intValue()))
                    .thenReturn(Mono.just(tentativeQueues).flatMapMany(Flux::fromIterable));

            List<Tentative> mappedTentatives = tentativeQueues.stream()
                    .map(tentativeMapper::tentativeQueueToTentative)
                    .collect(Collectors.toList());

            StepVerifier
                    .create(tentativeService.retrieveTentativeQueues(null, serverId, null, null))
                    .expectNext(mappedTentatives.get(0))
                    .verifyComplete();

            verify(tentativeDao, times(1))
                    .findByTentativeId_ServerId(serverId.intValue());
        }

        @Test
        @DisplayName("retrieveTentativeQueues - Filter by Tournament name")
        void test_retrieveTentativeQueues_ifTournamentNameIsPassed_shouldInvokeFilterByTournamentName() {
            List<TentativeQueue> tentativeQueues = List.of(easyRandom.nextObject(TentativeQueue.class));

            TournamentId tournamentId = tentativeQueues.get(0).getTentativeId().getTournamentId();
            when(tentativeDao.findByTentativeId_TournamentId_TournamentName(
                    tournamentId.getTournamentName()))
                    .thenReturn(Mono.just(tentativeQueues).flatMapMany(Flux::fromIterable));

            List<Tentative> mappedTentatives = tentativeQueues.stream()
                    .map(tentativeMapper::tentativeQueueToTentative)
                    .collect(Collectors.toList());

            StepVerifier
                    .create(tentativeService.retrieveTentativeQueues(null, null, tournamentId.getTournamentName(), null))
                    .expectNext(mappedTentatives.get(0))
                    .verifyComplete();

            verify(tentativeDao, times(1))
                    .findByTentativeId_TournamentId_TournamentName(tournamentId.getTournamentName());
        }

        @Test
        @DisplayName("retrieveTentativeQueues - Filter by Tournament day")
        void test_retrieveTentativeQueues_ifTournamentDayIsPassed_shouldInvokeFilterByTournamentDay() {
            List<TentativeQueue> tentativeQueues = List.of(easyRandom.nextObject(TentativeQueue.class));

            TournamentId tournamentId = tentativeQueues.get(0).getTentativeId().getTournamentId();
            when(tentativeDao.findByTentativeId_TournamentId_TournamentDay(
                    tournamentId.getTournamentDay()))
                    .thenReturn(Mono.just(tentativeQueues).flatMapMany(Flux::fromIterable));

            List<Tentative> mappedTentatives = tentativeQueues.stream()
                    .map(tentativeMapper::tentativeQueueToTentative)
                    .collect(Collectors.toList());

            StepVerifier
                    .create(tentativeService.retrieveTentativeQueues(null, null, null, tournamentId.getTournamentDay()))
                    .expectNext(mappedTentatives.get(0))
                    .verifyComplete();

            verify(tentativeDao, times(1))
                    .findByTentativeId_TournamentId_TournamentDay(tournamentId.getTournamentDay());
        }

        @Test
        @DisplayName("retrieveTentativeQueues - Filter by All")
        void test_retrieveTentativeQueues_ifAllNullArePassed_shouldInvokeFilterByAll() {
            List<TentativeQueue> tentativeQueues = List.of(easyRandom.nextObject(TentativeQueue.class));

            when(tentativeDao.findAll())
                    .thenReturn(Mono.just(tentativeQueues).flatMapMany(Flux::fromIterable));

            List<Tentative> mappedTentatives = tentativeQueues.stream()
                    .map(tentativeMapper::tentativeQueueToTentative)
                    .collect(Collectors.toList());

            StepVerifier
                    .create(tentativeService.retrieveTentativeQueues(null, null, null, null))
                    .expectNext(mappedTentatives.get(0))
                    .verifyComplete();

            verify(tentativeDao, times(1)).findAll();
        }
    }

    @Nested
    @DisplayName("Update")
    class Update {

        @Test
        @DisplayName("assignUserToTentativeQueue - If id does not belong on Tentative Queue then assign id to set of Discord Ids.")
        void test_assignUserToTentativeQueue_ifIdDoesNotExistOnTentativeQueueListThenInvokeUpdate() {
            Integer discordId = 1;
            TentativeQueue tentativeQueue = easyRandom.nextObject(TentativeQueue.class);
            tentativeQueue.getDiscordIds()
                            .clear();
            tentativeQueue.getDiscordIds().add(2);
            when(tentativeDao.findByTentativeId_TentativeId(tentativeQueue.getTentativeId().getTentativeId()))
                    .thenReturn(Mono.just(tentativeQueue));
            when(tentativeDao.updateByTentativeId_TentativeId(tentativeQueue.getTentativeId().getTentativeId(), discordId))
                    .thenReturn(Mono.just(1L));

            TentativeQueue expectedMappedTentativeQueue = TentativeQueue.builder()
                    .tentativeId(tentativeQueue.getTentativeId())
                    .discordIds(new HashSet<>(tentativeQueue.getDiscordIds()))
                    .build();

            expectedMappedTentativeQueue.getDiscordIds().add(discordId);

            StepVerifier
                    .create(tentativeService.assignUserToTentativeQueue(discordId, tentativeQueue.getTentativeId().getTentativeId()))
                    .expectNext(tentativeMapper.tentativeQueueToTentative(expectedMappedTentativeQueue))
                    .verifyComplete();
        }

        @Test
        @DisplayName("assignUserToTentativeQueue - If id does belong on Tentative Queue then throw an ClashBotDbException.")
        void test_assignUserToTentativeQueue_ifIdDoesExistOnTentativeQueueThenThrowClashBotDbException() {
            Integer discordId = 1;
            TentativeQueue tentativeQueue = easyRandom.nextObject(TentativeQueue.class);
            tentativeQueue.getDiscordIds()
                    .clear();
            tentativeQueue.getDiscordIds().add(discordId);
            when(tentativeDao.findByTentativeId_TentativeId(tentativeQueue.getTentativeId().getTentativeId()))
                    .thenReturn(Mono.just(tentativeQueue));
            StepVerifier
                    .create(tentativeService.assignUserToTentativeQueue(discordId, tentativeQueue.getTentativeId().getTentativeId()))
                    .expectError(ClashBotDbException.class)
                    .verify();
        }

        @Test
        @DisplayName("removeUserFromTentativeQueue - If id belongs on Tentative Queue then remove the id from the set of Discord Ids.")
        void test_removeUserFromTentativeQueue_ifIdDoesExistOnTentativeQueueListThenInvokeUpdate() {
            Integer discordId = 1;
            TentativeQueue tentativeQueue = easyRandom.nextObject(TentativeQueue.class);
            tentativeQueue.getDiscordIds()
                    .clear();
            tentativeQueue.getDiscordIds().add(discordId);
            when(tentativeDao.findByTentativeId_TentativeId(tentativeQueue.getTentativeId().getTentativeId()))
                    .thenReturn(Mono.just(tentativeQueue));
            when(tentativeDao.removeByTentativeId_TentativeId(tentativeQueue.getTentativeId().getTentativeId(), discordId))
                    .thenReturn(Mono.just(1L));

            TentativeQueue expectedMappedTentativeQueue = TentativeQueue.builder()
                    .tentativeId(tentativeQueue.getTentativeId())
                    .discordIds(new HashSet<>())
                    .build();

            StepVerifier
                    .create(tentativeService.removeUserFromTentativeQueue(discordId, tentativeQueue.getTentativeId().getTentativeId()))
                    .expectNext(tentativeMapper.tentativeQueueToTentative(expectedMappedTentativeQueue))
                    .verifyComplete();
        }

        @Test
        @DisplayName("removeUserFromTentativeQueue - If id does not belong to Tentative Queue then throw an ClashBotDbException.")
        void test_removeUserFromTentativeQueue_ifIdDoesNotExistOnTentativeQueueThenThrowClashBotDbException() {
            Integer discordId = 1;
            TentativeQueue tentativeQueue = easyRandom.nextObject(TentativeQueue.class);
            tentativeQueue.getDiscordIds()
                    .clear();
            when(tentativeDao.findByTentativeId_TentativeId(tentativeQueue.getTentativeId().getTentativeId()))
                    .thenReturn(Mono.just(tentativeQueue));
            StepVerifier
                    .create(tentativeService.removeUserFromTentativeQueue(discordId, tentativeQueue.getTentativeId().getTentativeId()))
                    .expectError(ClashBotDbException.class)
                    .verify();
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
            tentativeQueue.getTentativeId().setTentativeId(null);
            Tentative expectedMappedTentative = Tentative.builder()
                    .tentativePlayers(tentativeQueue.getDiscordIds().stream()
                                              .map(id -> TentativePlayer.builder().discordId(id).build()).collect(
                                    Collectors.toList()))
                    .serverId(tentativeQueue.getTentativeId().getServerId())
                    .tournamentDetails(
                            BaseTournament.builder()
                                    .tournamentName(
                                            tentativeQueue.getTentativeId().getTournamentId().getTournamentName())
                                    .tournamentDay(tentativeQueue.getTentativeId().getTournamentId().getTournamentDay())
                                    .build())
                    .build();

            ClashTeam clashTeam = easyRandom.nextObject(ClashTeam.class);
            clashTeam.getTeamId().setTournamentId(tentativeQueue.getTentativeId().getTournamentId());
            when(idUtils.retrieveNewTentativeQueueId()).thenReturn(mockTentativeId);
            List<UserAssociation> listOfUserAssociations = new ArrayList<>();
            tentativeQueue.getDiscordIds()
                            .forEach((id) -> {
                                UserAssociation userAssociation = UserAssociation.builder()
                                        .userAssociationKey(UserAssociationKey.builder()
                                                .tournamentId(tentativeQueue.getTentativeId().getTournamentId())
                                                .discordId(id)
                                                .build())
                                        .teamId(clashTeam.getTeamId().getId())
                                        .serverId(tentativeQueue.getTentativeId().getServerId())
                                        .build();
                                when(userAssociationService.retrieveUsersTeamOrTentativeQueueForTournament(id,
                                        tentativeQueue.getTentativeId().getTournamentId().getTournamentName(),
                                        tentativeQueue.getTentativeId().getTournamentId().getTournamentDay()))
                                        .thenReturn(Mono.just(userAssociation));
                                listOfUserAssociations.add(userAssociation);
                            }
                        );
            UserAssociation defaultUa = UserAssociation.builder()
                    .tentativeId(mockTentativeId)
                    .userAssociationKey(UserAssociationKey.builder()
                            .tournamentId(tentativeQueue.getTentativeId().getTournamentId())
                            .build())
                    .serverId(tentativeQueue.getTentativeId().getServerId())
                    .build();
            when(userAssociationService.swapUserAssociationBetweenTeamAndTentative(
                    discordIdsCaptor.capture(),
                    tournamentIdCaptor.capture(),
                    userAssociationCaptor.capture(),
                    functionArgumentCaptor.capture()
                    )).thenReturn(Mono.just(List.of(UserAssociation.builder().build())));
            when(tentativeDao.save(tentativeQueue))
                    .thenReturn(Mono.just(tentativeQueue));


            StepVerifier
                    .create(tentativeService.createTentativeQueue(tentativeQueue))
                    .expectNextMatches(tentative ->
                            StringUtils.isNotBlank(tentative.getId())
                            && expectedMappedTentative.getTentativePlayers().equals(tentative.getTentativePlayers())
                            && expectedMappedTentative.getServerId().equals(tentative.getServerId())
                            && expectedMappedTentative.getTournamentDetails().equals(tentative.getTournamentDetails())
                    )
                    .expectComplete()
                    .verify();
            assertEquals(tentativeQueue.getDiscordIds(), discordIdsCaptor.getValue());
            assertEquals(tentativeQueue.getTentativeId().getTournamentId(), tournamentIdCaptor.getValue());
            assertEquals(defaultUa, userAssociationCaptor.getValue());
        }

        @Test
        @DisplayName("If User does not belong to anything for a Tournament, they should have a user association created for them and they should be able to save tentative Queues and map them back to Tentative")
        void test_mapBackToTentative_ifUserDoesNotHaveAnAssociation_createAnAssociation() {
            String mockTentativeId = "tq-123asdf";
            TentativeQueue tentativeQueue = easyRandom.nextObject(TentativeQueue.class);
            tentativeQueue.getTentativeId().setTentativeId(null);
            Tentative expectedMappedTentative = Tentative.builder()
                    .tentativePlayers(tentativeQueue.getDiscordIds().stream()
                            .map(id -> TentativePlayer.builder().discordId(id).build()).collect(Collectors.toList()))
                    .serverId(tentativeQueue.getTentativeId().getServerId())
                    .tournamentDetails(
                            BaseTournament.builder()
                                    .tournamentName(
                                            tentativeQueue.getTentativeId().getTournamentId().getTournamentName())
                                    .tournamentDay(tentativeQueue.getTentativeId().getTournamentId().getTournamentDay())
                                    .build())
                    .build();

            ClashTeam clashTeam = easyRandom.nextObject(ClashTeam.class);
            clashTeam.getTeamId().setTournamentId(tentativeQueue.getTentativeId().getTournamentId());
            when(idUtils.retrieveNewTentativeQueueId()).thenReturn(mockTentativeId);
            tentativeQueue.getDiscordIds()
                            .forEach((id) -> {
                                when(userAssociationService.retrieveUsersTeamOrTentativeQueueForTournament(id,
                                        tentativeQueue.getTentativeId().getTournamentId().getTournamentName(),
                                        tentativeQueue.getTentativeId().getTournamentId().getTournamentDay()))
                                        .thenReturn(Mono.empty());
                            }
                        );

            List<UserAssociation> listOfUserAssociations = tentativeQueue.getDiscordIds()
                            .stream()
                                    .map(id -> UserAssociation.builder().userAssociationKey(UserAssociationKey.builder()
                                                    .discordId(id)
                                                    .tournamentId(tentativeQueue.getTentativeId().getTournamentId())
                                                    .build())
                                            .tentativeId(mockTentativeId)
                                            .teamId(null)
                                            .serverId(tentativeQueue.getTentativeId().getServerId())
                                            .build())
                                            .collect(Collectors.toList());

            UserAssociation defaultUa = UserAssociation.builder()
                    .tentativeId(mockTentativeId)
                    .userAssociationKey(UserAssociationKey.builder()
                            .tournamentId(tentativeQueue.getTentativeId().getTournamentId())
                            .build())
                    .serverId(tentativeQueue.getTentativeId().getServerId())
                    .build();
            when(userAssociationService.swapUserAssociationBetweenTeamAndTentative(
                    discordIdsCaptor.capture(),
                    tournamentIdCaptor.capture(),
                    userAssociationCaptor.capture(),
                    functionArgumentCaptor.capture()
            )).thenReturn(Mono.just(List.of(UserAssociation.builder().build())));
            when(tentativeDao.save(tentativeQueue))
                    .thenReturn(Mono.just(tentativeQueue));

            StepVerifier
                    .create(tentativeService.createTentativeQueue(tentativeQueue))
                    .expectNextMatches(tentative ->
                            StringUtils.isNotBlank(tentative.getId())
                            && expectedMappedTentative.getTentativePlayers().equals(tentative.getTentativePlayers())
                            && expectedMappedTentative.getServerId().equals(tentative.getServerId())
                            && expectedMappedTentative.getTournamentDetails().equals(tentative.getTournamentDetails())
                    )
                    .expectComplete()
                    .verify();
            assertEquals(tentativeQueue.getDiscordIds(), discordIdsCaptor.getValue());
            assertEquals(tentativeQueue.getTentativeId().getTournamentId(), tournamentIdCaptor.getValue());
            assertEquals(defaultUa, userAssociationCaptor.getValue());
        }

        @Test
        @DisplayName("Swap from Team to Tentative - If the user belongs to a Team, then they should be removed from it.")
        void test_swapFromTeamToTentative_ifTeamIdExistsOnTheAssociationThenInvokeRemoveFromTeam() {
            String tentativeQueueId = "tq-1234";
            String teamId = "ct-1234";
            UserAssociation userAssociation = UserAssociation.builder()
                    .userAssociationKey(easyRandom.nextObject(UserAssociationKey.class))
                    .serverId(1234)
                    .teamId(teamId)
                    .build();
            ArgumentCaptor<TeamId> teamIdArgumentCaptor = ArgumentCaptor.forClass(TeamId.class);
            ArgumentCaptor<Integer> discordIdCaptor = ArgumentCaptor.forClass(Integer.class);

            when(teamService.removeUserFromTeam(teamIdArgumentCaptor.capture(), discordIdCaptor.capture()))
                    .thenReturn(Mono.just(easyRandom.nextObject(ClashTeam.class)));
            tentativeService.swapFromTeamToTentative(tentativeQueueId)
                    .apply(userAssociation);

            assertEquals(teamId, teamIdArgumentCaptor.getValue().getId());
            assertEquals(tentativeQueueId, userAssociation.getTentativeId());
            assertNull(userAssociation.getTeamId());
        }

        @Test
        @DisplayName("Swap from Team to Tentative - If the user does not belong to a Team, then the association should be passed through.")
        void test_swapFromTeamToTentative_ifTeamIdDoesNotExistOnTheAssociationThenItShouldBePassedThrough() {
            String tentativeQueueId = "tq-1234";
            String teamId = "ct-1234";
            UserAssociation userAssociation = UserAssociation.builder()
                    .userAssociationKey(easyRandom.nextObject(UserAssociationKey.class))
                    .serverId(1234)
                    .tentativeId(tentativeQueueId)
                    .build();
            tentativeService.swapFromTeamToTentative(tentativeQueueId)
                    .apply(userAssociation);

            assertEquals(tentativeQueueId, userAssociation.getTentativeId());
            assertNull(userAssociation.getTeamId());
            verify(teamService, times(0))
                    .removeUserFromTeam(TeamId.builder()
                            .id(teamId)
                            .tournamentId(userAssociation.getUserAssociationKey().getTournamentId())
                            .build(), userAssociation.getUserAssociationKey().getDiscordId());
        }

    }

    @Nested
    @DisplayName("Swap from Team to Tentative")
    class SwapFromTeamToTentative {

        @Test
        @DisplayName("Swapping from Team to Tentative - association has a Team Id so it should be nulled and tentativeId should be populated")
        void test_swapFromTeamToTentative_associationHasTeamIdSoItShouldBeSwapped() {
            String tqId = "tq-123qwer";
            Integer discordId = 1;
            Integer serverId = 1;
            String teamId = "ct-123asdf";
            UserAssociation userAssociationWithTeam = UserAssociation.builder()
                    .userAssociationKey(UserAssociationKey
                            .builder()
                            .tournamentId(easyRandom.nextObject(TournamentId.class))
                            .discordId(discordId)
                            .build())
                    .teamId(teamId)
                    .serverId(serverId)
                    .build();
            UserAssociation expectedUserAssociation = userAssociationMapper.clone(userAssociationWithTeam);
            expectedUserAssociation.setTentativeId(tqId);
            expectedUserAssociation.setTeamId(null);

            when(teamService.removeUserFromTeam(TeamId.builder()
                            .id(teamId)
                            .tournamentId(userAssociationWithTeam.getUserAssociationKey()
                                    .getTournamentId())
                    .build(), discordId))
                    .thenReturn(Mono.just(ClashTeam.builder().build()));
            StepVerifier
                    .create(tentativeService.swapFromTeamToTentative(tqId)
                            .apply(userAssociationWithTeam))
                    .expectNext(expectedUserAssociation)
                    .verifyComplete();
        }

        @Test
        @DisplayName("Swapping from Team to Tentative - association does not have a Team Id so it should pass through")
        void test_swapFromTeamToTentative_theAssociationShouldPassThrough() {
            String tqId = "tq-123qwer";
            Integer discordId = 1;
            Integer serverId = 1;
            String teamId = "ct-123asdf";
            UserAssociation userAssociationWithTeam = UserAssociation.builder()
                    .userAssociationKey(UserAssociationKey
                            .builder()
                            .tournamentId(easyRandom.nextObject(TournamentId.class))
                            .discordId(discordId)
                            .build())
                    .tentativeId(tqId)
                    .serverId(serverId)
                    .build();
            UserAssociation expectedUserAssociation = userAssociationMapper.clone(userAssociationWithTeam);
            expectedUserAssociation.setTentativeId(tqId);
            expectedUserAssociation.setTeamId(null);

            StepVerifier
                    .create(tentativeService.swapFromTeamToTentative(tqId)
                            .apply(userAssociationWithTeam))
                    .expectNext(expectedUserAssociation)
                    .verifyComplete();
            verify(teamService, times(0))
                    .removeUserFromTeam(TeamId.builder()
                            .tournamentId(userAssociationWithTeam.getUserAssociationKey().getTournamentId())
                            .id(teamId)
                            .build(), discordId);
        }

    }

    private Tentative tentativeQueueToTentative(TentativeQueue tentativeQueue) {
        return Tentative.builder()
                .id(tentativeQueue.getTentativeId().getTentativeId())
                .tentativePlayers(tentativeQueue.getDiscordIds().stream()
                                          .map(id -> TentativePlayer.builder().discordId(id).build()).collect(
                                Collectors.toList()))
                .serverId(tentativeQueue.getTentativeId().getServerId())
                .tournamentDetails(
                        BaseTournament.builder()
                                .tournamentName(tentativeQueue.getTentativeId().getTournamentId().getTournamentName())
                                .tournamentDay(tentativeQueue.getTentativeId().getTournamentId().getTournamentDay())
                                .build())
                .build();
    }

}
