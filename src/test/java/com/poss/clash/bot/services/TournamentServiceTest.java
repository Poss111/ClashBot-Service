package com.poss.clash.bot.services;

import com.poss.clash.bot.ClashBotTestingConfig;
import com.poss.clash.bot.daos.TournamentDao;
import com.poss.clash.bot.daos.models.ClashTournament;
import com.poss.clash.bot.daos.models.TournamentId;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Example;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@Import(ClashBotTestingConfig.class)
class TournamentServiceTest {

    @InjectMocks
    TournamentService tournamentService;

    @Mock
    TournamentDao tournamentDao;

    @Autowired
    EasyRandom easyRandom;

    @Nested
    @DisplayName("Query")
    class Query {

        @Test
        @DisplayName("retrieveTournamentsByTournamentOrDay - Retrieve all tournaments by name or day")
        void test() {
            TournamentId tournamentId = easyRandom.nextObject(TournamentId.class);

            ClashTournament clashTournament = easyRandom.nextObject(ClashTournament.class);

            when(tournamentDao.findAll(Example.of(ClashTournament.builder()
                    .tournamentId(tournamentId)
                    .build()))
            ).thenReturn(Mono.just(List.of(clashTournament))
                    .flatMapMany(Flux::fromIterable));

            StepVerifier
                    .create(tournamentService.retrieveTournamentsByTournamentOrDay(
                            tournamentId.getTournamentName(),
                            tournamentId.getTournamentDay())
                    )
                    .expectNext(clashTournament)
                    .verifyComplete();
        }

        @Test
        @DisplayName("retrieveAllTournaments - If upcoming only is false then retrieve all tournaments")
        void test2() {
            ClashTournament clashTournament = easyRandom.nextObject(ClashTournament.class);

            when(tournamentDao.findAll())
                    .thenReturn(Mono.just(List.of(clashTournament))
                            .flatMapMany(Flux::fromIterable));

            StepVerifier
                    .create(tournamentService.retrieveAllTournaments(false))
                    .expectNext(clashTournament)
                    .verifyComplete();
        }

        @Test
        @DisplayName("retrieveAllTournaments - If upcoming only is true then retrieve all tournaments that are after the current time")
        void test3() {
            ClashTournament clashTournament = easyRandom.nextObject(ClashTournament.class);

            when(tournamentDao.findClashTournamentsByStartTimeAfter(any(Instant.class)))
                    .thenReturn(Mono.just(List.of(clashTournament))
                            .flatMapMany(Flux::fromIterable));

            StepVerifier
                    .create(tournamentService.retrieveAllTournaments(true))
                    .expectNext(clashTournament)
                    .verifyComplete();
        }

        @Test
        @DisplayName("isTournamentActive - Should take a tournament name and day and return true of false if it is active")
        void test4() {
            TournamentId tournamentId = easyRandom.nextObject(TournamentId.class);

            when(tournamentDao.existsByTournamentIdTournamentName_AndTournamentIdTournamentDay_AndStartTimeAfter(
                    anyString(),
                    anyString(),
                    any(Instant.class))
            ).thenReturn(Mono.just(true));

            StepVerifier
                    .create(tournamentService.isTournamentActive(tournamentId.getTournamentName(), tournamentId.getTournamentDay()))
                    .expectNext(true)
                    .verifyComplete();
        }

    }

    @Nested
    @DisplayName("Create")
    class Create {

        @Test
        @DisplayName("saveTournament - should take a tournament in and invoke save")
        void test() {
            ClashTournament clashTournament = easyRandom.nextObject(ClashTournament.class);

            when(tournamentDao.save(clashTournament))
                    .thenReturn(Mono.just(clashTournament));

            StepVerifier
                    .create(tournamentService.saveTournament(clashTournament))
                    .expectNext(clashTournament)
                    .verifyComplete();
        }

    }

}
