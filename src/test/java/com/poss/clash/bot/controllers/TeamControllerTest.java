package com.poss.clash.bot.controllers;

import com.poss.clash.bot.ClashBotTestingConfig;
import com.poss.clash.bot.daos.models.ClashTeam;
import com.poss.clash.bot.exceptions.ClashBotControllerException;
import com.poss.clash.bot.openapi.model.*;
import com.poss.clash.bot.services.TeamService;
import com.poss.clash.bot.utils.TeamMapper;
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
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith({SpringExtension.class})
@Import(ClashBotTestingConfig.class)
public class TeamControllerTest {

    @InjectMocks
    TeamController teamController;

    @Mock
    TeamService teamService;

    @Spy
    TeamMapper teamMapper = Mappers.getMapper(TeamMapper.class);

    @Autowired
    EasyRandom easyRandom;

    @Nested
    @DisplayName("POST - createTeam")
    class Create {

        @Test
        @DisplayName("200 - Should successfully create a Team if there is a valid User position detail ")
        void test_createTeam_shouldCreateATeam() {
            Integer serverId = 1234;
            String name = "Charizard";
            Integer discordId = 1;

            BaseTournament baseTournament = easyRandom.nextObject(BaseTournament.class);

            TeamRequired requestedTeamToCreate = TeamRequired.builder()
                    .serverId(serverId)
                    .name(name)
                    .playerDetails(TeamPlayerDetails.builder()
                            .top(TeamPlayer.builder()
                                    .discordId(discordId)
                                    .build())
                            .build())
                    .tournament(baseTournament)
                    .build();

            ClashTeam clashTeam = teamMapper.teamRequiredToClashTeam(requestedTeamToCreate);
            Team team = teamMapper.clashTeamToTeam(clashTeam);

            StepVerifier
                    .create(teamController.createTeam(Mono.just(requestedTeamToCreate), null))
                    .expectNext(ResponseEntity.ok(team))
                    .verifyComplete();
        }

        @Test
        @DisplayName("400 - If the payload does not have any position details then it should return with a bad request.")
        void test_createTeam_shouldValidateThatThereIsAtLeastOnePosition() {
            Integer serverId = 1234;
            String name = "Charizard";

            BaseTournament baseTournament = easyRandom.nextObject(BaseTournament.class);

            TeamRequired requestedTeamToCreate = TeamRequired.builder()
                    .serverId(serverId)
                    .name(name)
                    .tournament(baseTournament)
                    .build();

            ClashTeam clashTeam = teamMapper.teamRequiredToClashTeam(requestedTeamToCreate);

            StepVerifier
                    .create(teamController.createTeam(Mono.just(requestedTeamToCreate), null))
                    .expectError(ClashBotControllerException.class)
                    .verify();

            verify(teamService, times(0))
                    .createClashTeam(clashTeam);
        }

    }

}
