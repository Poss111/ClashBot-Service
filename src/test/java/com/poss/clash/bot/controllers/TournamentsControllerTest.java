package com.poss.clash.bot.controllers;

import com.poss.clash.bot.ClashBotTestingConfig;
import com.poss.clash.bot.daos.models.ClashTournament;
import com.poss.clash.bot.openapi.model.DetailedTournament;
import com.poss.clash.bot.openapi.model.Tournaments;
import com.poss.clash.bot.services.TournamentService;
import com.poss.clash.bot.utils.TournamentMapper;
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
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.Mockito.when;

@ExtendWith({SpringExtension.class})
@Import(ClashBotTestingConfig.class)
public class TournamentsControllerTest {

    @InjectMocks
    TournamentsController tournamentsController;

    @Mock
    TournamentService tournamentService;

    @Spy
    TournamentMapper tournamentMapper = Mappers.getMapper(TournamentMapper.class);

    @Autowired
    EasyRandom easyRandom;

    @Nested
    @DisplayName("POST - createTournament")
    class CreateTournament {

        @Test
        @DisplayName("200 - should create and save a new tournament.")
        void test() {
            DetailedTournament detailedTournament = easyRandom.nextObject(DetailedTournament.class);

            ClashTournament clashTournament = tournamentMapper.detailedTournamentToClashTournament(detailedTournament);
            when(tournamentService.saveTournament(clashTournament))
                    .thenReturn(Mono.just(clashTournament));

            StepVerifier
                    .create(tournamentsController.createTournament(Mono.just(detailedTournament), null))
                    .expectNext(ResponseEntity.ok(tournamentMapper.clashTournamentToDetailedTournament(clashTournament)))
                    .verifyComplete();
        }

    }

    @Nested
    @DisplayName("GET - getTournaments")
    class GetTournaments {

        @Test
        @DisplayName("200 - should retrieve tournaments based on filter")
        void test() {
            ClashTournament clashTournament = easyRandom.nextObject(ClashTournament.class);

            when(tournamentService.retrieveTournamentsByTournamentOrDay(
                    clashTournament.getTournamentId().getTournamentName(),
                    clashTournament.getTournamentId().getTournamentDay()))
                    .thenReturn(Mono.just(List.of(clashTournament)).flatMapMany(Flux::fromIterable));

            Tournaments expectedResponse = Tournaments.builder()
                    .tournaments(List.of(tournamentMapper.clashTournamentToDetailedTournament(clashTournament)))
                    .count(1)
                    .build();

            StepVerifier
                    .create(tournamentsController.getTournaments(
                            clashTournament.getTournamentId().getTournamentName(),
                            clashTournament.getTournamentId().getTournamentDay(),
                            false,
                            null))
                    .expectNext(ResponseEntity.ok(expectedResponse))
                    .verifyComplete();
        }

        @Test
        @DisplayName("200 - if no filter criteria is passed, all tournaments should be returned")
        void test2() {
            ClashTournament clashTournament = easyRandom.nextObject(ClashTournament.class);

            when(tournamentService.retrieveAllTournaments(false))
                    .thenReturn(Mono.just(List.of(clashTournament)).flatMapMany(Flux::fromIterable));

            Tournaments expectedResponse = Tournaments.builder()
                    .tournaments(List.of(tournamentMapper.clashTournamentToDetailedTournament(clashTournament)))
                    .count(1)
                    .build();

            StepVerifier
                    .create(tournamentsController.getTournaments(
                            null,
                            null,
                            false,
                            null))
                    .expectNext(ResponseEntity.ok(expectedResponse))
                    .verifyComplete();
        }

    }

}
