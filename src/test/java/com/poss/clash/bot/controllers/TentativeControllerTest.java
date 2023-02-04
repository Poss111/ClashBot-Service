package com.poss.clash.bot.controllers;

import com.poss.clash.bot.openapi.model.*;
import com.poss.clash.bot.services.TentativeService;
import com.poss.clash.bot.services.UserService;
import com.poss.clash.bot.utils.TentativeMapper;
import com.poss.clash.bot.utils.TentativeMapperImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;

@ExtendWith({ SpringExtension.class, MockitoExtension.class })
public class TentativeControllerTest {

    @InjectMocks
    TentativeController tentativeController;

    @Mock
    TentativeService tentativeService;

    @Mock
    UserService userService;

    @Spy
    TentativeMapper tentativeMapper;

    @Test
    @DisplayName("retrieveTentativeQueues - return all Tentative Queues")
    void test_retrieveTentativeQueues_mapTentativeQueuesAndPlayerDetails() {
        Integer serverId = 1;
        String tournamentName = "awesome_sauce";
        String tournamentDay = "1";
        Integer playerOneId = 1;
        Integer playerTwoId = 2;
        Integer playerThreeId = 3;
        List<TentativePlayer> tentativePlayers = new ArrayList<>();
        tentativePlayers.add(TentativePlayer.builder()
                .discordId(playerOneId)
                .build());
        tentativePlayers.add(TentativePlayer.builder()
                .discordId(playerTwoId)
                .build());
        tentativePlayers.add(TentativePlayer.builder()
                .discordId(playerThreeId)
                .build());

        Player playerOne = Player.builder()
                .discordId(playerOneId)
                .name("one")
                .champions(new ArrayList<>())
                .build();
        Player playerTwo = Player.builder()
                .discordId(playerTwoId)
                .name("two")
                .champions(new ArrayList<>())
                .build();
        Player playerThree = Player.builder()
                .discordId(playerThreeId)
                .name("three")
                .champions(new ArrayList<>())
                .build();
        BaseTournament baseTournament = BaseTournament.builder()
                .tournamentName(tournamentName)
                .tournamentDay(tournamentDay)
                .build();
        ArrayList<Tentative> tentativeQueues = new ArrayList<>();
        tentativeQueues.add(Tentative.builder()
                        .serverId(serverId)
                        .tournamentDetails(baseTournament)
                        .tentativePlayers(tentativePlayers)
                .build());
        when(tentativeService.retrieveTentativeQueues())
                .thenReturn(Mono.just(tentativeQueues)
                        .flatMapIterable(tentatives -> tentatives));
        when(userService.retrieveUser(playerOneId))
                .thenReturn(Mono.just(playerOne));
        when(userService.retrieveUser(playerTwoId))
                .thenReturn(Mono.just(playerTwo));
        when(userService.retrieveUser(playerThreeId))
                .thenReturn(Mono.just(playerThree));
        StepVerifier.create(tentativeController.retrieveTentativeQueues(false, null))
                .expectComplete()
                .log()
                .verify();
    }

}
