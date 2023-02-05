package com.poss.clash.bot.services;

import com.poss.clash.bot.ClashBotTestingConfig;
import com.poss.clash.bot.daos.TentativeDao;
import com.poss.clash.bot.daos.models.TentativeId;
import com.poss.clash.bot.daos.models.TentativeQueue;
import com.poss.clash.bot.daos.models.TournamentId;
import com.poss.clash.bot.openapi.model.BaseTournament;
import com.poss.clash.bot.openapi.model.Tentative;
import com.poss.clash.bot.openapi.model.TentativePlayer;
import com.poss.clash.bot.utils.TentativeMapper;
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
import org.springframework.data.domain.Example;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.stream.Collectors;

import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@Import(ClashBotTestingConfig.class)
public class TentativeServiceTest {

    @InjectMocks
    TentativeService tentativeService;

    @Mock
    TentativeDao tentativeDao;

    @Spy
    TentativeMapper tentativeMapper = Mappers.getMapper(TentativeMapper.class);

    @Autowired
    EasyRandom easyRandom;

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
                .create(tentativeService.retrieveTentativeQueuesByTournament(tournamentName, null))
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
                .create(tentativeService.retrieveTentativeQueuesByTournament(null, tournamentDay))
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
                .create(tentativeService.retrieveTentativeQueuesByTournament(tournamentName, tournamentDay))
                .expectNext(tentative)
                .expectNext(tentative1)
                .expectNext(tentative2)
                .expectNext(tentative3)
                .expectComplete()
                .verify();
    }

    @Nested
    @DisplayName("Save")
    class Save {

        @Test
        @DisplayName("Should be able to save tentative Queues and map them back to Tentative")
        void test_mapBackToTentative() {
            TentativeQueue tentativeQueue = easyRandom.nextObject(TentativeQueue.class);
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

            when(tentativeDao.save(tentativeQueue))
                    .thenReturn(Mono.just(tentativeQueue));

            StepVerifier
                    .create(tentativeService.saveTentativeQueue(tentativeQueue))
                    .expectNext(expectedMappedTentative)
                    .expectComplete()
                    .verify();
        }

    }

    private Tentative tentativeQueueToTentative(TentativeQueue tentativeQueue) {
        return Tentative.builder()
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
