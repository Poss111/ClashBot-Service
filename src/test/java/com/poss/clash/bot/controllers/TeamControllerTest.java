package com.poss.clash.bot.controllers;

import com.poss.clash.bot.ClashBotTestingConfig;
import com.poss.clash.bot.daos.models.ArchivedClashTeam;
import com.poss.clash.bot.daos.models.BasePlayerRecord;
import com.poss.clash.bot.daos.models.ClashTeam;
import com.poss.clash.bot.daos.models.TournamentId;
import com.poss.clash.bot.exceptions.ClashBotControllerException;
import com.poss.clash.bot.openapi.model.*;
import com.poss.clash.bot.services.ArchivedService;
import com.poss.clash.bot.services.TeamService;
import com.poss.clash.bot.services.UserAssignmentService;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.mockito.Mockito.*;

@ExtendWith({SpringExtension.class})
@Import(ClashBotTestingConfig.class)
public class TeamControllerTest {

    @InjectMocks
    TeamController teamController;

    @Mock
    TeamService teamService;

    @Mock
    UserAssignmentService userAssignmentService;

    @Mock
    ArchivedService archivedService;

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

            BaseTournament baseTournament = easyRandom.nextObject(BaseTournament.class);

            TeamPlayerDetails playerDetails = easyRandom.nextObject(TeamPlayerDetails.class);
            TeamRequired requestedTeamToCreate = TeamRequired.builder()
                    .serverId(serverId)
                    .name(name)
                    .playerDetails(playerDetails)
                    .tournament(baseTournament)
                    .build();

            Map<Role, Integer> roleToIdMap = Map.of(
                    Role.TOP, playerDetails.getTop().getDiscordId(),
                    Role.JG, playerDetails.getJg().getDiscordId(),
                    Role.MID, playerDetails.getMid().getDiscordId(),
                    Role.BOT, playerDetails.getBot().getDiscordId(),
                    Role.SUPP, playerDetails.getSupp().getDiscordId()
            );

            Map<Role, BasePlayerRecord> roleToBasePlayer = Map.of(
                    Role.TOP, BasePlayerRecord.builder().discordId(playerDetails.getTop().getDiscordId()).build(),
                    Role.JG, BasePlayerRecord.builder().discordId(playerDetails.getJg().getDiscordId()).build(),
                    Role.MID, BasePlayerRecord.builder().discordId(playerDetails.getMid().getDiscordId()).build(),
                    Role.BOT, BasePlayerRecord.builder().discordId(playerDetails.getBot().getDiscordId()).build(),
                    Role.SUPP, BasePlayerRecord.builder().discordId(playerDetails.getSupp().getDiscordId()).build()
            );

            ClashTeam clashTeam = teamMapper.teamRequiredToClashTeam(requestedTeamToCreate);
            clashTeam.setPositions(roleToBasePlayer);
            when(userAssignmentService.createTeamAndAssignUser(roleToIdMap,
                name,
                serverId,
                baseTournament.getTournamentName(),
                baseTournament.getTournamentDay())
            ).thenReturn(Mono.just(clashTeam));

            StepVerifier
                    .create(teamController.createTeam(Mono.just(requestedTeamToCreate), null))
                    .expectNext(ResponseEntity.ok(teamMapper.clashTeamToTeam(clashTeam)))
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

    @Nested
    @DisplayName("PATH - assignUserToTeam")
    class AssignUser {

        @Test
        @DisplayName("Should invoke assign user to team and map to a Team object")
        void test1() {
            String teamId = "ct-1234";
            long discordId = 1L;
            Mono<PositionDetails> positionDetailsMono = Mono.just(PositionDetails.builder().role(Role.TOP).build());

            ClashTeam clashTeamToBeReturned = easyRandom.nextObject(ClashTeam.class);
            when(userAssignmentService.assignUserToTeam(1, Role.TOP, teamId))
                    .thenReturn(Mono.just(clashTeamToBeReturned));

            StepVerifier
                    .create(teamController.assignUserToTeam(teamId, discordId, positionDetailsMono, null))
                    .expectNext(ResponseEntity.ok(teamMapper.clashTeamToTeam(clashTeamToBeReturned)))
                    .verifyComplete();
        }

    }

    @Nested
    @DisplayName("DELETE - removeUserFromTeam")
    class RemoveUser {

        @Test
        @DisplayName("Should invoke remove user from Team")
        void test() {
            String teamId = "ct-1234";
            long discordId = 1L;

            ClashTeam clashTeamToBeReturned = easyRandom.nextObject(ClashTeam.class);
            when(userAssignmentService.findAndRemoveUserFromTeam(1, teamId))
                    .thenReturn(Mono.just(clashTeamToBeReturned));

            StepVerifier
                    .create(teamController.removeUserFromTeam(teamId, discordId, null))
                    .expectNext(ResponseEntity.ok(teamMapper.clashTeamToTeam(clashTeamToBeReturned)))
                    .verifyComplete();
        }

    }

    @Nested
    @DisplayName("GET - retrieveTeamBasedOnId")
    class RetrieveTeamById {

        @Test
        @DisplayName("Should invoke retrieval of Team by its id")
        void test() {
            ClashTeam clashTeamToBeReturned = easyRandom.nextObject(ClashTeam.class);
            when(teamService.findTeamById(clashTeamToBeReturned.getTeamId().getId()))
                    .thenReturn(Mono.just(clashTeamToBeReturned));

            StepVerifier
                    .create(teamController.retrieveTeamBasedOnId(clashTeamToBeReturned.getTeamId().getId(), null))
                    .expectNext(ResponseEntity.ok(teamMapper.clashTeamToTeam(clashTeamToBeReturned)))
                    .verifyComplete();
        }

    }

    @Nested
    @DisplayName("GET - retrieveTeams")
    class RetrieveTeams {

        @Test
        @DisplayName("If looking to retrieve active Teams, then it should query active teams filter passed on passed details")
        void test() {
            int discordId = 1;
            int serverId = 1234;
            TournamentId tournamentId = easyRandom.nextObject(TournamentId.class);
            ClashTeam clashTeam = easyRandom.nextObject(ClashTeam.class);
            ClashTeam clashTeam2 = easyRandom.nextObject(ClashTeam.class);
            ClashTeam clashTeam3 = easyRandom.nextObject(ClashTeam.class);
            clashTeam.getTeamId().setTournamentId(tournamentId);
            clashTeam2.getTeamId().setTournamentId(tournamentId);
            clashTeam3.getTeamId().setTournamentId(tournamentId);
            List<ClashTeam> filteredClashTeams = List.of(
                    clashTeam,
                    clashTeam2,
                    clashTeam3);
            when(teamService.retrieveTeamBasedOnCriteria(
                    discordId,
                    serverId,
                    tournamentId.getTournamentName(),
                    tournamentId.getTournamentDay())
            ).thenReturn(Mono.just(filteredClashTeams).flatMapMany(Flux::fromIterable));

            Teams expectedResponse = Teams.builder()
                    .teams(filteredClashTeams.stream().map(teamMapper::clashTeamToTeam).collect(Collectors.toList()))
                    .count(3)
                    .build();

            StepVerifier
                    .create(teamController.retrieveTeams(
                            false,
                                    Integer.toUnsignedLong(discordId),
                            Integer.toUnsignedLong(serverId),
                            tournamentId.getTournamentName(),
                            tournamentId.getTournamentDay(),
                            null)
                    ).expectNext(ResponseEntity.ok(expectedResponse))
                    .verifyComplete();
        }

        @Test
        @DisplayName("If looking to retrieve inactive Teams, then it should query inactive teams filter passed on passed details")
        void test2() {
            long discordId = 1;
            long serverId = 1234;
            TournamentId tournamentId = easyRandom.nextObject(TournamentId.class);
            ArchivedClashTeam clashTeam = easyRandom.nextObject(ArchivedClashTeam.class);
            ArchivedClashTeam clashTeam2 = easyRandom.nextObject(ArchivedClashTeam.class);
            ArchivedClashTeam clashTeam3 = easyRandom.nextObject(ArchivedClashTeam.class);
            clashTeam.getTeamId().setTournamentId(tournamentId);
            clashTeam2.getTeamId().setTournamentId(tournamentId);
            clashTeam3.getTeamId().setTournamentId(tournamentId);
            List<ArchivedClashTeam> filteredClashTeams = List.of(
                    clashTeam,
                    clashTeam2,
                    clashTeam3);
            when(archivedService.retrieveTeamBasedOnCriteria(
                    discordId,
                    serverId,
                    tournamentId.getTournamentName(),
                    tournamentId.getTournamentDay())
            ).thenReturn(Mono.just(filteredClashTeams).flatMapMany(Flux::fromIterable));

            Teams expectedResponse = Teams.builder()
                    .teams(filteredClashTeams.stream().map(teamMapper::archivedClashTeamToTeam)
                            .collect(Collectors.toList()))
                    .count(3)
                    .build();

            StepVerifier
                    .create(teamController.retrieveTeams(
                            true,
                            discordId,
                            serverId,
                            tournamentId.getTournamentName(),
                            tournamentId.getTournamentDay(),
                            null)
                    ).expectNext(ResponseEntity.ok(expectedResponse))
                    .verifyComplete();
        }

    }

    @Nested
    @DisplayName("PATCH - updateTeam")
    class UpdateTeam {

        @Test
        @DisplayName("200 - When a request to update a Team comes in, it should invoke to update a team")
        void test() {
            String teamId = "ct-1234";
            String newName = "new name";
            TeamUpdate teamNameUpdate = TeamUpdate.builder().teamName(newName).build();
            Mono<TeamUpdate> teamUpdateMono = Mono.just(teamNameUpdate);

            ClashTeam clashTeamUpdate = easyRandom.nextObject(ClashTeam.class);
            when(teamService.updateTeamName(teamId, newName))
                    .thenReturn(Mono.just(clashTeamUpdate));

            StepVerifier
                    .create(teamController.updateTeam(teamId, teamUpdateMono, null))
                    .expectNext(ResponseEntity.ok(teamMapper.clashTeamToTeam(clashTeamUpdate)))
                    .verifyComplete();
        }

        @Test
        @DisplayName("404 - When no team was found, it should return with 404")
        void test2() {
            String teamId = "ct-1234";
            String newName = "new name";
            TeamUpdate teamNameUpdate = TeamUpdate.builder().teamName(newName).build();
            Mono<TeamUpdate> teamUpdateMono = Mono.just(teamNameUpdate);
            when(teamService.updateTeamName(teamId, newName))
                    .thenReturn(Mono.empty());

            StepVerifier
                    .create(teamController.updateTeam(teamId, teamUpdateMono, null))
                    .expectNext(ResponseEntity.notFound().build())
                    .verifyComplete();
        }

    }

}
