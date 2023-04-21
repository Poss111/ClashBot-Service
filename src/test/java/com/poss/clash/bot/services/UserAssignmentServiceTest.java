package com.poss.clash.bot.services;


import com.poss.clash.bot.ClashBotTestingConfig;
import com.poss.clash.bot.daos.models.*;
import com.poss.clash.bot.exceptions.ClashBotDbException;
import com.poss.clash.bot.openapi.model.Event;
import com.poss.clash.bot.openapi.model.Role;
import com.poss.clash.bot.openapi.model.Team;
import com.poss.clash.bot.openapi.model.Tentative;
import com.poss.clash.bot.source.TeamSource;
import com.poss.clash.bot.utils.TeamMapper;
import com.poss.clash.bot.utils.TentativeMapper;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.publisher.PublisherProbe;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@Import(ClashBotTestingConfig.class)
public class UserAssignmentServiceTest {

    @InjectMocks
    UserAssignmentService userAssignmentService;

    @Mock
    TeamService teamService;

    @Mock
    UserAssociationService userAssociationService;

    @Mock
    TentativeService tentativeService;

    @Mock
    TournamentService tournamentService;

    @Mock
    TeamSource teamSource;

    @Spy
    TeamMapper teamMapper = Mappers.getMapper(TeamMapper.class);

    @Spy
    TentativeMapper tentativeMapper = Mappers.getMapper(TentativeMapper.class);

    @Captor
    ArgumentCaptor<Team> teamSourceArgumentCaptor;

    @Captor
    ArgumentCaptor<Tentative> tentativeArgumentCaptor;

    @Captor
    ArgumentCaptor<String> causedByCaptor;

    @Autowired
    EasyRandom easyRandom;

    @Nested
    @DisplayName("Team Interactions")
    class TeamInteractions {

        @Nested
        @DisplayName("Assign to Team")
        class Assign {

            @Test
            @DisplayName("Nothing -> Team - Assign User to Team when they do not belong to any Team or Tentative Queue for a Tournament")
            void test_assignUserToTeam_userDoesNotExistOnOtherTeamOrTentativeQueues() {
                String discordId = "1";
                String serverId = "1234";
                String clashTeamId = "ct-1234";
                String teamName = "Some Random Name";

                TournamentId tournamentId = easyRandom.nextObject(TournamentId.class);
                TeamId teamId = TeamId.builder()
                        .id(clashTeamId)
                        .tournamentId(tournamentId)
                        .build();

                ClashTeam retrievedTeam = ClashTeam.builder()
                        .teamId(teamId)
                        .teamName(teamName)
                        .serverId(serverId)
                        .positions(new HashMap<>())
                        .build();


                ClashTeam returnedClashTeam = ClashTeam.builder()
                        .teamId(teamId)
                        .teamName(teamName)
                        .serverId(serverId)
                        .positions(Map.of(Role.TOP, BasePlayerRecord.builder()
                                .discordId(discordId)
                                .build()))
                        .build();

                UserAssociation expectedUserAssociation = UserAssociation.builder()
                        .teamId(clashTeamId)
                        .serverId(serverId)
                        .userAssociationKey(UserAssociationKey.builder()
                                .discordId(discordId)
                                .tournamentId(tournamentId)
                                .build())
                        .build();

                when(teamService.findTeamById(clashTeamId))
                        .thenReturn(Mono.just(retrievedTeam));
                when(teamService.addUserToTeam(discordId, Role.TOP, retrievedTeam))
                        .thenReturn(returnedClashTeam);
                when(teamService.upsertClashTeam(returnedClashTeam))
                        .thenReturn(Mono.just(returnedClashTeam));
                when(userAssociationService.retrieveUsersTeamOrTentativeQueueForTournament(discordId, tournamentId.getTournamentName(), tournamentId.getTournamentDay()))
                        .thenReturn(Mono.empty());
                when(userAssociationService.save(expectedUserAssociation))
                        .thenReturn(Mono.just(expectedUserAssociation));
                when(teamSource.sendTeamUpdateEvent(teamSourceArgumentCaptor.capture(), causedByCaptor.capture()))
                        .thenReturn(Mono.just(Event.builder().build()));

                StepVerifier
                        .create(userAssignmentService.assignUserToTeam(discordId, Role.TOP, clashTeamId))
                        .expectNext(returnedClashTeam)
                        .verifyComplete();

                verify(teamService, times(1))
                        .addUserToTeam(discordId, Role.TOP, retrievedTeam);
                verify(teamService, times(1))
                        .upsertClashTeam(returnedClashTeam);
                verify(userAssociationService, times(1))
                        .retrieveUsersTeamOrTentativeQueueForTournament(discordId, tournamentId.getTournamentName(), tournamentId.getTournamentDay());
                verify(userAssociationService, times(1))
                        .save(expectedUserAssociation);

                assertAll(() -> {
                    assertEquals(1, teamSourceArgumentCaptor.getAllValues().size(), "More events executed than expected");
                    assertEquals(1, causedByCaptor.getAllValues().size(), "More events executed than expected");
                    verifyTeamEvent(discordId, serverId, returnedClashTeam, teamSourceArgumentCaptor.getAllValues().get(0), causedByCaptor.getAllValues().get(0));
                });
            }

            @Test
            @DisplayName("Another Team -> Team - Assign User to Team when they belong to another Team for a Tournament")
            void test_assignUserToTeam_userExistsOnAnotherTeam() {
                String discordId = "1";
                String serverId = "1234";
                String clashTeamId = "ct-1234";
                String currentlyAssignedClashTeamId = "ct-4321";
                String teamName = "Some Random Name";

                TournamentId tournamentId = easyRandom.nextObject(TournamentId.class);
                TeamId teamId = TeamId.builder()
                        .id(clashTeamId)
                        .tournamentId(tournamentId)
                        .build();

                ClashTeam retrievedTeam = ClashTeam.builder()
                        .teamId(teamId)
                        .teamName(teamName)
                        .serverId(serverId)
                        .positions(new HashMap<>())
                        .build();

                ClashTeam returnedClashTeam = ClashTeam.builder()
                        .teamId(teamId)
                        .teamName(teamName)
                        .serverId(serverId)
                        .positions(Map.of(Role.TOP, BasePlayerRecord.builder()
                                .discordId(discordId)
                                .build()))
                        .build();

                UserAssociation otherTeamUserAssociation = UserAssociation.builder()
                        .teamId(currentlyAssignedClashTeamId)
                        .serverId(serverId)
                        .userAssociationKey(UserAssociationKey.builder()
                                .discordId(discordId)
                                .tournamentId(tournamentId)
                                .build())
                        .build();

                UserAssociation expectedUserAssociation = UserAssociation.builder()
                        .teamId(clashTeamId)
                        .serverId(serverId)
                        .userAssociationKey(UserAssociationKey.builder()
                                .discordId(discordId)
                                .tournamentId(tournamentId)
                                .build())
                        .build();

                ClashTeam teamAfterRemoval = ClashTeam.builder()
                        .serverId(serverId)
                        .teamId(TeamId.builder()
                                .id(currentlyAssignedClashTeamId)
                                .tournamentId(tournamentId)
                                .build())
                        .positions(new HashMap<>())
                        .build();

                when(teamService.findTeamById(clashTeamId))
                        .thenReturn(Mono.just(retrievedTeam));
                when(teamService.addUserToTeam(discordId, Role.TOP, retrievedTeam))
                        .thenReturn(returnedClashTeam);
                when(teamService.upsertClashTeam(returnedClashTeam))
                        .thenReturn(Mono.just(returnedClashTeam));
                when(userAssociationService.retrieveUsersTeamOrTentativeQueueForTournament(discordId, tournamentId.getTournamentName(), tournamentId.getTournamentDay()))
                        .thenReturn(Mono.just(otherTeamUserAssociation));
                when(teamService.removeUserFromTeam(currentlyAssignedClashTeamId, discordId))
                        .thenReturn(Mono.just(teamAfterRemoval));
                when(userAssociationService.save(expectedUserAssociation))
                        .thenReturn(Mono.just(expectedUserAssociation));
                when(teamSource.sendTeamUpdateEvent(teamSourceArgumentCaptor.capture(), causedByCaptor.capture()))
                        .thenReturn(Mono.just(Event.builder().build()));

                StepVerifier
                        .create(userAssignmentService.assignUserToTeam(discordId, Role.TOP, clashTeamId))
                        .expectNext(returnedClashTeam)
                        .verifyComplete();

                verify(teamService, times(1))
                        .addUserToTeam(discordId, Role.TOP, retrievedTeam);
                verify(teamService, times(1))
                        .upsertClashTeam(returnedClashTeam);
                verify(userAssociationService, times(1))
                        .retrieveUsersTeamOrTentativeQueueForTournament(discordId, tournamentId.getTournamentName(), tournamentId.getTournamentDay());
                verify(teamService, times(1))
                        .removeUserFromTeam(currentlyAssignedClashTeamId, discordId);
                verify(userAssociationService, times(1))
                        .save(expectedUserAssociation);
                verify(teamSource, times(2))
                        .sendTeamUpdateEvent(any(Team.class), anyString());

                assertAll(() -> {
                    assertEquals(2, teamSourceArgumentCaptor.getAllValues().size(), "More events executed than expected");
                    assertEquals(2, causedByCaptor.getAllValues().size(), "More events executed than expected");
                    verifyTeamEvent(discordId, serverId, teamAfterRemoval, teamSourceArgumentCaptor.getAllValues().get(0), causedByCaptor.getAllValues().get(0));
                    verifyTeamEvent(discordId, serverId, returnedClashTeam, teamSourceArgumentCaptor.getAllValues().get(1), causedByCaptor.getAllValues().get(1));
                });
            }

            @Test
            @DisplayName("Same Team -> Same Team - Assign User to Team when they belong to the same Team for a Tournament")
            void test_assignUserToTeam_swapRoleOnTeam() {
                String discordId = "1";
                String serverId = "1234";
                String currentlyAssignedClashTeamId = "ct-4321";
                String teamName = "Some Random Name";

                TournamentId tournamentId = easyRandom.nextObject(TournamentId.class);
                TeamId teamId = TeamId.builder()
                        .id(currentlyAssignedClashTeamId)
                        .tournamentId(tournamentId)
                        .build();

                ClashTeam retrievedTeam = ClashTeam.builder()
                        .teamId(teamId)
                        .teamName(teamName)
                        .serverId(serverId)
                        .positions(new HashMap<>())
                        .build();

                ClashTeam returnedClashTeam = ClashTeam.builder()
                        .teamId(teamId)
                        .teamName(teamName)
                        .serverId(serverId)
                        .positions(Map.of(Role.TOP, BasePlayerRecord.builder()
                                .discordId(discordId)
                                .build()))
                        .build();

                UserAssociation otherTeamUserAssociation = UserAssociation.builder()
                        .teamId(currentlyAssignedClashTeamId)
                        .serverId(serverId)
                        .userAssociationKey(UserAssociationKey.builder()
                                .discordId(discordId)
                                .tournamentId(tournamentId)
                                .build())
                        .build();

                UserAssociation expectedUserAssociation = UserAssociation.builder()
                        .teamId(currentlyAssignedClashTeamId)
                        .serverId(serverId)
                        .userAssociationKey(UserAssociationKey.builder()
                                .discordId(discordId)
                                .tournamentId(tournamentId)
                                .build())
                        .build();

                ClashTeam teamAfterRemoval = ClashTeam.builder()
                        .serverId(serverId)
                        .teamId(TeamId.builder()
                                .id(currentlyAssignedClashTeamId)
                                .tournamentId(tournamentId)
                                .build())
                        .positions(new HashMap<>())
                        .build();

                when(teamService.findTeamById(currentlyAssignedClashTeamId))
                        .thenReturn(Mono.just(retrievedTeam));
                when(teamService.addUserToTeam(discordId, Role.TOP, retrievedTeam))
                        .thenReturn(returnedClashTeam);
                when(teamService.upsertClashTeam(returnedClashTeam))
                        .thenReturn(Mono.just(returnedClashTeam));
                when(userAssociationService.retrieveUsersTeamOrTentativeQueueForTournament(discordId, tournamentId.getTournamentName(), tournamentId.getTournamentDay()))
                        .thenReturn(Mono.just(otherTeamUserAssociation));
                when(teamService.removeUserFromTeam(currentlyAssignedClashTeamId, discordId))
                        .thenReturn(Mono.just(teamAfterRemoval));
                when(userAssociationService.save(expectedUserAssociation))
                        .thenReturn(Mono.just(expectedUserAssociation));
                when(teamSource.sendTeamUpdateEvent(teamSourceArgumentCaptor.capture(), causedByCaptor.capture()))
                        .thenReturn(Mono.just(Event.builder().build()));

                StepVerifier
                        .create(userAssignmentService.assignUserToTeam(discordId, Role.TOP, currentlyAssignedClashTeamId))
                        .expectNext(returnedClashTeam)
                        .verifyComplete();

                verify(teamService, times(0))
                        .addUserToTeam(discordId, Role.TOP, retrievedTeam);
                verify(teamService, times(1))
                        .upsertClashTeam(returnedClashTeam);
                verify(userAssociationService, times(1))
                        .retrieveUsersTeamOrTentativeQueueForTournament(discordId, tournamentId.getTournamentName(), tournamentId.getTournamentDay());
                verify(teamService, times(0))
                        .removeUserFromTeam(currentlyAssignedClashTeamId, discordId);
                verify(userAssociationService, times(0))
                        .save(expectedUserAssociation);

                assertAll(() -> {
                    assertEquals(1, teamSourceArgumentCaptor.getAllValues().size(), "More events executed than expected");
                    verifyTeamEvent(discordId, serverId, returnedClashTeam, teamSourceArgumentCaptor.getAllValues().get(0), discordId);
                });
            }

            @Test
            @DisplayName("Tentative Queue -> Team - Assign User to Team when they belong to Tentative Queue for a Tournament")
            void test_assignUserToTeam_userExistsOnATentativeQueue() {
                String discordId = "1";
                String serverId = "1234";
                String clashTeamId = "ct-1234";
                String tentativeQueueId = "tq-1234";
                String teamName = "Some Random Name";

                TournamentId tournamentId = easyRandom.nextObject(TournamentId.class);
                TeamId teamId = TeamId.builder()
                        .id(clashTeamId)
                        .tournamentId(tournamentId)
                        .build();

                ClashTeam retrievedTeam = ClashTeam.builder()
                        .teamId(teamId)
                        .teamName(teamName)
                        .serverId(serverId)
                        .positions(new HashMap<>())
                        .build();


                ClashTeam returnedClashTeam = ClashTeam.builder()
                        .teamId(teamId)
                        .teamName(teamName)
                        .serverId(serverId)
                        .positions(Map.of(Role.TOP, BasePlayerRecord.builder()
                                .discordId(discordId)
                                .build()))
                        .build();

                UserAssociation otherTentativeQueueUserAssociation = UserAssociation.builder()
                        .tentativeId(tentativeQueueId)
                        .serverId(serverId)
                        .userAssociationKey(UserAssociationKey.builder()
                                .discordId(discordId)
                                .tournamentId(tournamentId)
                                .build())
                        .build();

                UserAssociation expectedUserAssociation = UserAssociation.builder()
                        .teamId(clashTeamId)
                        .serverId(serverId)
                        .userAssociationKey(UserAssociationKey.builder()
                                .discordId(discordId)
                                .tournamentId(tournamentId)
                                .build())
                        .build();

                TentativeQueue updatedTentativeQueue = easyRandom.nextObject(TentativeQueue.class);
                updatedTentativeQueue.getTentativeId().setServerId(serverId);

                when(teamService.findTeamById(clashTeamId))
                        .thenReturn(Mono.just(retrievedTeam));
                when(teamService.addUserToTeam(discordId, Role.TOP, retrievedTeam))
                        .thenReturn(returnedClashTeam);
                when(teamService.upsertClashTeam(returnedClashTeam))
                        .thenReturn(Mono.just(returnedClashTeam));
                when(userAssociationService.retrieveUsersTeamOrTentativeQueueForTournament(discordId, tournamentId.getTournamentName(), tournamentId.getTournamentDay()))
                        .thenReturn(Mono.just(otherTentativeQueueUserAssociation));
                when(tentativeService.removeUserFromTentativeQueue(discordId, tentativeQueueId))
                        .thenReturn(Mono.just(updatedTentativeQueue));
                when(userAssociationService.save(expectedUserAssociation))
                        .thenReturn(Mono.just(expectedUserAssociation));
                when(teamSource.sendTentativeQueueUpdateEvent(tentativeArgumentCaptor.capture(), causedByCaptor.capture()))
                        .thenReturn(Mono.just(Event.builder().build()));
                when(teamSource.sendTeamUpdateEvent(teamSourceArgumentCaptor.capture(), causedByCaptor.capture()))
                        .thenReturn(Mono.just(Event.builder().build()));

                StepVerifier
                        .create(userAssignmentService.assignUserToTeam(discordId, Role.TOP, clashTeamId))
                        .expectNext(returnedClashTeam)
                        .verifyComplete();

                verify(teamService, times(1))
                        .addUserToTeam(discordId, Role.TOP, retrievedTeam);
                verify(teamService, times(1))
                        .upsertClashTeam(returnedClashTeam);
                verify(userAssociationService, times(1))
                        .retrieveUsersTeamOrTentativeQueueForTournament(discordId, tournamentId.getTournamentName(), tournamentId.getTournamentDay());
                verify(tentativeService, times(1))
                        .removeUserFromTentativeQueue(discordId, tentativeQueueId);
                verify(teamService, times(0))
                        .removeUserFromTeam(anyString(), anyString());
                verify(userAssociationService, times(1))
                        .save(expectedUserAssociation);
                verify(teamSource, times(1))
                        .sendTentativeQueueUpdateEvent(any(Tentative.class), any());
                verify(teamSource, times(1))
                        .sendTeamUpdateEvent(any(Team.class), any());

                assertAll(() -> {
                    assertEquals(1, tentativeArgumentCaptor.getAllValues().size(), "More Tentative events executed than expected");
                    assertEquals(1, teamSourceArgumentCaptor.getAllValues().size(), "More Team events executed than expected");
                    verifyTentativeEvent(discordId, serverId, updatedTentativeQueue, tentativeArgumentCaptor.getAllValues().get(0), discordId);
                    verifyTeamEvent(discordId, serverId, returnedClashTeam, teamSourceArgumentCaptor.getAllValues().get(0), discordId);
                });
            }

            @Test
            @DisplayName("(Error) assignUserToTeam - Role already taken")
            void test_assignUserToTeam_error_roleIsAlreadyTakenForATeam() {
                String discordId = "1";
                String serverId = "1234";
                String clashTeamId = "ct-1234";
                String teamName = "Some Random Name";

                TournamentId tournamentId = easyRandom.nextObject(TournamentId.class);
                TeamId teamId = TeamId.builder()
                        .id(clashTeamId)
                        .tournamentId(tournamentId)
                        .build();

                ClashTeam retrievedTeam = ClashTeam.builder()
                        .teamId(teamId)
                        .teamName(teamName)
                        .serverId(serverId)
                        .positions(Map.of(Role.TOP, BasePlayerRecord.builder()
                                .discordId("2")
                                .build()))
                        .build();

                when(teamService.findTeamById(clashTeamId))
                        .thenReturn(Mono.just(retrievedTeam));

                StepVerifier
                        .create(userAssignmentService.assignUserToTeam(discordId, Role.TOP, clashTeamId))
                        .expectError(ClashBotDbException.class)
                        .verify();

                verify(teamService, times(0))
                        .addUserToTeam(discordId, Role.TOP, retrievedTeam);
                verify(teamService, times(0))
                        .upsertClashTeam(any(ClashTeam.class));
            }

            @Test
            @DisplayName("(Error) validateTeamAvailability - Role already taken")
            void test_validateTeamAvailability_error_roleIsAlreadyTakenForATeam() {
                String serverId = "1234";
                String clashTeamId = "ct-1234";
                String teamName = "Some Random Name";

                TournamentId tournamentId = easyRandom.nextObject(TournamentId.class);
                TeamId teamId = TeamId.builder()
                        .id(clashTeamId)
                        .tournamentId(tournamentId)
                        .build();

                ClashTeam returnedClashTeam = ClashTeam.builder()
                        .teamId(teamId)
                        .teamName(teamName)
                        .serverId(serverId)
                        .positions(Map.of(Role.TOP, BasePlayerRecord.builder()
                                .discordId("2")
                                .build()))
                        .build();

                ClashBotDbException clashBotDbException = assertThrows(ClashBotDbException.class,
                        () -> userAssignmentService.validateTeamAvailability(Role.TOP, returnedClashTeam));
                assertEquals(MessageFormat.format("Role {0} already taken on Team {1}", Role.TOP, returnedClashTeam.getTeamId()), clashBotDbException.getMessage());
            }

            @Test
            @DisplayName("(Error) Target Team does not exist")
            void test_assignUserToTeam_error_teamDoesNotExist() {
                String discordId = "1";
                String serverId = "1234";
                String clashTeamId = "ct-1234";
                String teamName = "Some Random Name";

                TournamentId tournamentId = easyRandom.nextObject(TournamentId.class);
                TeamId teamId = TeamId.builder()
                        .id(clashTeamId)
                        .tournamentId(tournamentId)
                        .build();

                ClashTeam retrievedTeam = ClashTeam.builder()
                        .teamId(teamId)
                        .teamName(teamName)
                        .serverId(serverId)
                        .positions(new HashMap<>())
                        .build();

                when(teamService.findTeamById(clashTeamId))
                        .thenReturn(Mono.empty());

                StepVerifier
                        .create(userAssignmentService.assignUserToTeam(discordId, Role.TOP, clashTeamId))
                        .expectErrorMessage(MessageFormat.format("No Team found with id {0}", clashTeamId))
                        .verify();

                verify(teamService, times(0))
                        .addUserToTeam(discordId, Role.TOP, retrievedTeam);
                verify(teamService, times(0))
                        .upsertClashTeam(any(ClashTeam.class));
            }

        }

        @Nested
        @DisplayName("Create a Team")
        class Create {

            @Test
            @DisplayName("Nothing -> Team - User does not belong on anything before creating a Team")
            void test_createTeamAndAssignUser_userDoesNotExistOnAnything() {
                String discordId = "1";
                String discordServerId = "123";
                String teamName = "team name";
                String createdTeamId = "ct-1234";

                TournamentId tournamentId = easyRandom.nextObject(TournamentId.class);
                HashMap<Role, BasePlayerRecord> positions = new HashMap<>();
                positions.put(Role.TOP, BasePlayerRecord.builder()
                        .discordId(discordId)
                        .build());
                HashMap<Role, String> roleToIdMap = new HashMap<>();
                roleToIdMap.put(Role.TOP, discordId);
                ClashTeam expectedTeamToCreate = ClashTeam.builder()
                        .teamId(TeamId.builder()
                                .tournamentId(tournamentId)
                                .build())
                        .teamName(teamName)
                        .serverId(discordServerId)
                        .positions(positions)
                        .build();

                ClashTeam createdTeam = ClashTeam.builder()
                        .teamId(TeamId.builder()
                                .tournamentId(tournamentId)
                                .id(createdTeamId)
                                .build())
                        .teamName(teamName)
                        .serverId(discordServerId)
                        .positions(positions)
                        .build();

                UserAssociation createTeamUA = UserAssociation.builder()
                        .userAssociationKey(UserAssociationKey.builder()
                                .tournamentId(tournamentId)
                                .discordId(discordId)
                                .build())
                        .teamId(createdTeamId)
                        .serverId(discordServerId)
                        .build();

                when(tournamentService.isTournamentActive(tournamentId.getTournamentName(), tournamentId.getTournamentDay()))
                        .thenReturn(Mono.just(true));
                when(userAssociationService.retrieveUsersTeamOrTentativeQueueForTournament(discordId,
                        tournamentId.getTournamentName(),
                        tournamentId.getTournamentDay()))
                        .thenReturn(Mono.empty());
                when(teamService.createClashTeam(expectedTeamToCreate))
                        .thenReturn(Mono.just(createdTeam));
                when(userAssociationService.save(createTeamUA))
                        .thenReturn(Mono.just(createTeamUA));
                when(teamSource.sendTeamCreateEvent(teamSourceArgumentCaptor.capture(), causedByCaptor.capture()))
                        .thenReturn(Mono.just(Event.builder().build()));

                StepVerifier
                        .create(userAssignmentService.createTeamAndAssignUser(roleToIdMap,
                                teamName,
                                discordServerId,
                                tournamentId.getTournamentName(),
                                tournamentId.getTournamentDay()))
                        .expectNext(createdTeam)
                        .verifyComplete();

                verify(teamService, times(1))
                        .createClashTeam(expectedTeamToCreate);
                verify(userAssociationService, times(1))
                        .retrieveUsersTeamOrTentativeQueueForTournament(discordId,
                                tournamentId.getTournamentName(),
                                tournamentId.getTournamentDay());
                verify(userAssociationService, times(1))
                        .save(createTeamUA);
                verify(teamSource, times(1))
                        .sendTeamCreateEvent(any(Team.class), any());

                assertAll(() -> {
                    assertEquals(1, teamSourceArgumentCaptor.getAllValues().size(), "More events executed than expected");
                    verifyTeamEvent(discordId, discordServerId, createdTeam, teamSourceArgumentCaptor.getAllValues().get(0), discordId);
                });
            }

            @Test
            @DisplayName("Another Team -> Team - User belongs on another Team, they should be removed before creating a Team")
            void test_createTeamAndAssignUser_userDoesExistsOnAnotherTeam() {
                String discordId = "1";
                String discordServerId = "123";
                String teamName = "team name";
                String createdTeamId = "ct-1234";
                String otherTeamId = "ct-54321";

                TournamentId tournamentId = easyRandom.nextObject(TournamentId.class);
                HashMap<Role, BasePlayerRecord> positions = new HashMap<>();
                positions.put(Role.TOP, BasePlayerRecord.builder()
                        .discordId(discordId)
                        .build());
                HashMap<Role, String> roleToIdMap = new HashMap<>();
                roleToIdMap.put(Role.TOP, discordId);
                ClashTeam expectedTeamToCreate = ClashTeam.builder()
                        .teamId(TeamId.builder()
                                .tournamentId(tournamentId)
                                .build())
                        .teamName(teamName)
                        .serverId(discordServerId)
                        .positions(positions)
                        .build();

                ClashTeam createdTeam = ClashTeam.builder()
                        .teamId(TeamId.builder()
                                .tournamentId(tournamentId)
                                .id(createdTeamId)
                                .build())
                        .teamName(teamName)
                        .serverId(discordServerId)
                        .positions(positions)
                        .build();

                UserAssociation createTeamUA = UserAssociation.builder()
                        .userAssociationKey(UserAssociationKey.builder()
                                .tournamentId(tournamentId)
                                .discordId(discordId)
                                .build())
                        .teamId(createdTeamId)
                        .serverId(discordServerId)
                        .build();

                when(tournamentService.isTournamentActive(tournamentId.getTournamentName(), tournamentId.getTournamentDay()))
                        .thenReturn(Mono.just(true));
                when(userAssociationService.retrieveUsersTeamOrTentativeQueueForTournament(discordId,
                        tournamentId.getTournamentName(),
                        tournamentId.getTournamentDay()))
                        .thenReturn(Mono.just(UserAssociation.builder()
                                .teamId(otherTeamId)
                                .userAssociationKey(UserAssociationKey.builder()
                                        .discordId(discordId)
                                        .tournamentId(tournamentId)
                                        .build())
                                .serverId(discordServerId)
                                .build()));
                ClashTeam teamToBeRemovedFrom = ClashTeam.builder().teamId(TeamId.builder()
                                .id(otherTeamId)
                                .tournamentId(tournamentId)
                                .build())
                        .serverId(discordServerId)
                        .build();
                when(teamService.removeUserFromTeam(otherTeamId, discordId))
                        .thenReturn(Mono.just(teamToBeRemovedFrom));
                when(teamService.createClashTeam(expectedTeamToCreate))
                        .thenReturn(Mono.just(createdTeam));
                when(userAssociationService.save(createTeamUA))
                        .thenReturn(Mono.just(createTeamUA));
                when(teamSource.sendTeamUpdateEvent(teamSourceArgumentCaptor.capture(), causedByCaptor.capture()))
                        .thenReturn(Mono.just(Event.builder().build()));
                when(teamSource.sendTeamCreateEvent(teamSourceArgumentCaptor.capture(), causedByCaptor.capture()))
                        .thenReturn(Mono.just(Event.builder().build()));

                StepVerifier
                        .create(userAssignmentService.createTeamAndAssignUser(roleToIdMap,
                                teamName,
                                discordServerId,
                                tournamentId.getTournamentName(),
                                tournamentId.getTournamentDay()))
                        .expectNext(createdTeam)
                        .verifyComplete();

                verify(teamService, times(1))
                        .createClashTeam(expectedTeamToCreate);
                verify(userAssociationService, times(1))
                        .retrieveUsersTeamOrTentativeQueueForTournament(discordId,
                                tournamentId.getTournamentName(),
                                tournamentId.getTournamentDay());
                verify(teamService, times(1))
                        .removeUserFromTeam(otherTeamId, discordId);
                verify(userAssociationService, times(1))
                        .save(createTeamUA);
                verify(teamSource, times(1))
                        .sendTeamUpdateEvent(any(Team.class), anyString());
                verify(teamSource, times(1))
                        .sendTeamCreateEvent(any(Team.class), anyString());

                assertEquals(2, teamSourceArgumentCaptor.getAllValues().size(), "More events executed than expected");
                verifyTeamEvent(discordId, discordServerId, teamToBeRemovedFrom, teamSourceArgumentCaptor.getAllValues().get(0), causedByCaptor.getAllValues().get(0));
                verifyTeamEvent(discordId, discordServerId, createdTeam, teamSourceArgumentCaptor.getAllValues().get(1), causedByCaptor.getAllValues().get(1));
            }

            @Test
            @DisplayName("Nothing -> Team - All 5 positions, Users do not belong on anything before creating a Team")
            void test_createTeamAndAssignUser_allPositions_usersDoNotExistOnAnything() {
                String discordId = "1";
                String discordIdTwo = "2";
                String discordIdThree = "3";
                String discordIdFour = "4";
                String discordIdFive = "5";
                List<String> discordIds = List.of(discordId, discordIdTwo, discordIdThree, discordIdFour, discordIdFive);
                String teamName = "team name";
                String discordServerId = "123";
                String createdTeamId = "ct-1234";

                TournamentId tournamentId = easyRandom.nextObject(TournamentId.class);
                HashMap<Role, BasePlayerRecord> positions = new HashMap<>();
                positions.put(Role.TOP, BasePlayerRecord.builder()
                        .discordId(discordId)
                        .build());
                positions.put(Role.JG, BasePlayerRecord.builder()
                        .discordId(discordIdTwo)
                        .build());
                positions.put(Role.MID, BasePlayerRecord.builder()
                        .discordId(discordIdThree)
                        .build());
                positions.put(Role.BOT, BasePlayerRecord.builder()
                        .discordId(discordIdFour)
                        .build());
                positions.put(Role.SUPP, BasePlayerRecord.builder()
                        .discordId(discordIdFive)
                        .build());
                HashMap<Role, String> roleToIdMap = new HashMap<>();
                roleToIdMap.put(Role.TOP, discordId);
                roleToIdMap.put(Role.JG, discordIdTwo);
                roleToIdMap.put(Role.MID, discordIdThree);
                roleToIdMap.put(Role.BOT, discordIdFour);
                roleToIdMap.put(Role.SUPP, discordIdFive);
                ClashTeam expectedTeamToCreate = ClashTeam.builder()
                        .teamId(TeamId.builder()
                                .tournamentId(tournamentId)
                                .build())
                        .teamName(teamName)
                        .serverId(discordServerId)
                        .positions(positions)
                        .build();

                ClashTeam createdTeam = ClashTeam.builder()
                        .teamId(TeamId.builder()
                                .tournamentId(tournamentId)
                                .id(createdTeamId)
                                .build())
                        .teamName(teamName)
                        .serverId(discordServerId)
                        .positions(positions)
                        .build();

                List<UserAssociation> expectedUserAssociations = discordIds.stream()
                        .map(id -> UserAssociation.builder()
                                .userAssociationKey(UserAssociationKey.builder()
                                        .tournamentId(tournamentId)
                                        .discordId(id)
                                        .build())
                                .teamId(createdTeamId)
                                .serverId(discordServerId)
                                .build())
                        .collect(Collectors.toList());

                when(tournamentService.isTournamentActive(tournamentId.getTournamentName(), tournamentId.getTournamentDay()))
                        .thenReturn(Mono.just(true));
                when(teamService.createClashTeam(expectedTeamToCreate))
                        .thenReturn(Mono.just(createdTeam));


                when(userAssociationService.retrieveUsersTeamOrTentativeQueueForTournament(anyString(),
                        anyString(),
                        anyString()))
                        .thenReturn(Mono.empty());

                for (UserAssociation expectedUserAssociation : expectedUserAssociations) {
                    when(userAssociationService.save(expectedUserAssociation))
                            .thenReturn(Mono.just(expectedUserAssociation));
                }
                when(teamSource.sendTeamCreateEvent(teamSourceArgumentCaptor.capture(), causedByCaptor.capture()))
                        .thenReturn(Mono.just(Event.builder().build()));

                StepVerifier
                        .create(userAssignmentService.createTeamAndAssignUser(roleToIdMap,
                                teamName,
                                discordServerId,
                                tournamentId.getTournamentName(),
                                tournamentId.getTournamentDay()))
                        .expectNext(createdTeam)
                        .verifyComplete();

                verify(teamService, times(1))
                        .createClashTeam(expectedTeamToCreate);
                for (String id : discordIds) {
                    verify(userAssociationService, times(1))
                            .retrieveUsersTeamOrTentativeQueueForTournament(id,
                                    tournamentId.getTournamentName(),
                                    tournamentId.getTournamentDay());
                }
                for (UserAssociation expectedUserAssociation : expectedUserAssociations) {
                    verify(userAssociationService, times(1))
                            .save(expectedUserAssociation);
                }

                assertEquals(1, teamSourceArgumentCaptor.getAllValues().size(), "More events executed than expected");
                verifyTeamEvent(discordIdFive, discordServerId, createdTeam, teamSourceArgumentCaptor.getAllValues().get(0), discordIdFive);
            }

            @Test
            @DisplayName("Other Teams -> Team - All 5 positions, Users belong on other Teams before creating a Team")
            void test_createTeamAndAssignUser_allPositions_usersDoExistOnOtherTeams() {
                String discordId = "1";
                String discordIdTwo = "2";
                String discordIdThree = "3";
                String discordIdFour = "4";
                String discordIdFive = "5";
                List<String> discordIds = List.of(discordId, discordIdTwo, discordIdThree, discordIdFour, discordIdFive);
                String teamName = "team name";
                String discordServerId = "123";
                String createdTeamId = "ct-1234";
                String otherTeamId = "ct-54321";

                TournamentId tournamentId = easyRandom.nextObject(TournamentId.class);
                HashMap<Role, BasePlayerRecord> positions = new HashMap<>();
                positions.put(Role.TOP, BasePlayerRecord.builder()
                        .discordId(discordId)
                        .build());
                positions.put(Role.JG, BasePlayerRecord.builder()
                        .discordId(discordIdTwo)
                        .build());
                positions.put(Role.MID, BasePlayerRecord.builder()
                        .discordId(discordIdThree)
                        .build());
                positions.put(Role.BOT, BasePlayerRecord.builder()
                        .discordId(discordIdFour)
                        .build());
                positions.put(Role.SUPP, BasePlayerRecord.builder()
                        .discordId(discordIdFive)
                        .build());
                HashMap<Role, String> roleToIdMap = new HashMap<>();
                roleToIdMap.put(Role.TOP, discordId);
                roleToIdMap.put(Role.JG, discordIdTwo);
                roleToIdMap.put(Role.MID, discordIdThree);
                roleToIdMap.put(Role.BOT, discordIdFour);
                roleToIdMap.put(Role.SUPP, discordIdFive);
                ClashTeam expectedTeamToCreate = ClashTeam.builder()
                        .teamId(TeamId.builder()
                                .tournamentId(tournamentId)
                                .build())
                        .teamName(teamName)
                        .serverId(discordServerId)
                        .positions(positions)
                        .build();

                ClashTeam createdTeam = ClashTeam.builder()
                        .teamId(TeamId.builder()
                                .tournamentId(tournamentId)
                                .id(createdTeamId)
                                .build())
                        .teamName(teamName)
                        .serverId(discordServerId)
                        .positions(positions)
                        .build();

                List<UserAssociation> expectedUserAssociations = discordIds.stream()
                        .map(id -> UserAssociation.builder()
                                .userAssociationKey(UserAssociationKey.builder()
                                        .tournamentId(tournamentId)
                                        .discordId(id)
                                        .build())
                                .teamId(createdTeamId)
                                .serverId(discordServerId)
                                .build())
                        .collect(Collectors.toList());

                when(tournamentService.isTournamentActive(tournamentId.getTournamentName(), tournamentId.getTournamentDay()))
                        .thenReturn(Mono.just(true));
                when(teamService.createClashTeam(expectedTeamToCreate))
                        .thenReturn(Mono.just(createdTeam));

                ClashTeam teamToBeRemovedFrom = ClashTeam.builder()
                        .teamId(TeamId.builder()
                                .id(otherTeamId)
                                .tournamentId(tournamentId)
                                .build())
                        .serverId(discordServerId)
                        .build();

                for (String id : discordIds) {
                    when(userAssociationService.retrieveUsersTeamOrTentativeQueueForTournament(id,
                            tournamentId.getTournamentName(),
                            tournamentId.getTournamentDay()))
                            .thenReturn(Mono.just(UserAssociation.builder()
                                    .teamId(otherTeamId)
                                    .serverId(discordServerId)
                                    .userAssociationKey(UserAssociationKey.builder()
                                            .discordId(id)
                                            .tournamentId(tournamentId)
                                            .build())
                                    .build()));
                    when(teamService.removeUserFromTeam(otherTeamId, id))
                            .thenReturn(Mono.just(teamToBeRemovedFrom));
                }

                for (UserAssociation expectedUserAssociation : expectedUserAssociations) {
                    when(userAssociationService.save(expectedUserAssociation))
                            .thenReturn(Mono.just(expectedUserAssociation));
                }
                when(teamSource.sendTeamUpdateEvent(teamSourceArgumentCaptor.capture(), causedByCaptor.capture()))
                        .thenReturn(Mono.just(Event.builder().build()));
                when(teamSource.sendTeamCreateEvent(teamSourceArgumentCaptor.capture(), causedByCaptor.capture()))
                        .thenReturn(Mono.just(Event.builder().build()));

                StepVerifier
                        .create(userAssignmentService.createTeamAndAssignUser(roleToIdMap,
                                teamName,
                                discordServerId,
                                tournamentId.getTournamentName(),
                                tournamentId.getTournamentDay()))
                        .expectNext(createdTeam)
                        .verifyComplete();

                verify(teamService, times(1))
                        .createClashTeam(expectedTeamToCreate);
                for (String id : discordIds) {
                    verify(userAssociationService, times(1))
                            .retrieveUsersTeamOrTentativeQueueForTournament(id,
                                    tournamentId.getTournamentName(),
                                    tournamentId.getTournamentDay());
                    verify(teamService, times(1))
                            .removeUserFromTeam(otherTeamId, id);
                }
                for (UserAssociation expectedUserAssociation : expectedUserAssociations) {
                    verify(userAssociationService, times(1))
                            .save(expectedUserAssociation);
                }
                verify(teamSource, times(5))
                        .sendTeamUpdateEvent(any(Team.class), anyString());
                verify(teamSource, times(1))
                        .sendTeamCreateEvent(any(Team.class), anyString());

                assertEquals(6, teamSourceArgumentCaptor.getAllValues().size(), "More events executed than expected");

                List<String> idsToCheck = new ArrayList<>(discordIds);
                for (int i = 0; i < 5; i++) {
                    var team = teamSourceArgumentCaptor.getAllValues().get(i);
                    idsToCheck.remove(causedByCaptor.getAllValues().remove(i));
                    assertNotNull(team);
                    assertEquals(discordServerId, team.getServerId());
                    assertEquals(teamMapper.clashTeamToTeam(teamToBeRemovedFrom), team);
                }

                assertEquals(0, idsToCheck.size(), "Not all caused by discord ids where used.");
                assertNotNull(teamSourceArgumentCaptor.getAllValues().get(5).getId());
                assertEquals(discordServerId, teamSourceArgumentCaptor.getAllValues().get(5).getServerId());
                assertEquals(teamMapper.clashTeamToTeam(createdTeam), teamSourceArgumentCaptor.getAllValues().get(5));
            }

            @Test
            @DisplayName("Another Tentative Queue -> Team - User belongs on another Tentative Queue, they should be removed before creating a Team")
            void test_createTeamAndAssignUser_userDoesExistsOnAnotherTentativeQueue() {
                String discordId = "1";
                String teamName = "team name";
                String discordServerId = "123";
                String createdTeamId = "ct-1234";
                String otherTentativeQueue = "tq-1234";

                TournamentId tournamentId = easyRandom.nextObject(TournamentId.class);
                HashMap<Role, BasePlayerRecord> positions = new HashMap<>();
                positions.put(Role.TOP, BasePlayerRecord.builder()
                        .discordId(discordId)
                        .build());
                HashMap<Role, String> roleToIdMap = new HashMap<>();
                roleToIdMap.put(Role.TOP, discordId);
                ClashTeam expectedTeamToCreate = ClashTeam.builder()
                        .teamId(TeamId.builder()
                                .tournamentId(tournamentId)
                                .build())
                        .teamName(teamName)
                        .serverId(discordServerId)
                        .positions(positions)
                        .build();

                ClashTeam createdTeam = ClashTeam.builder()
                        .teamId(TeamId.builder()
                                .tournamentId(tournamentId)
                                .id(createdTeamId)
                                .build())
                        .teamName(teamName)
                        .serverId(discordServerId)
                        .positions(positions)
                        .build();

                UserAssociation createTeamUA = UserAssociation.builder()
                        .userAssociationKey(UserAssociationKey.builder()
                                .tournamentId(tournamentId)
                                .discordId(discordId)
                                .build())
                        .teamId(createdTeamId)
                        .serverId(discordServerId)
                        .build();
                TentativeQueue tentativeQueueToBeRemovedFrom = easyRandom.nextObject(TentativeQueue.class);

                when(tournamentService.isTournamentActive(tournamentId.getTournamentName(), tournamentId.getTournamentDay()))
                        .thenReturn(Mono.just(true));
                when(userAssociationService.retrieveUsersTeamOrTentativeQueueForTournament(discordId,
                        tournamentId.getTournamentName(),
                        tournamentId.getTournamentDay()))
                        .thenReturn(Mono.just(UserAssociation.builder()
                                .tentativeId(otherTentativeQueue)
                                .userAssociationKey(UserAssociationKey.builder()
                                        .discordId(discordId)
                                        .tournamentId(tournamentId)
                                        .build())
                                .serverId(discordServerId)
                                .build()));
                when(tentativeService.removeUserFromTentativeQueue(discordId, otherTentativeQueue))
                        .thenReturn(Mono.just(tentativeQueueToBeRemovedFrom));
                when(teamService.createClashTeam(expectedTeamToCreate))
                        .thenReturn(Mono.just(createdTeam));
                when(userAssociationService.save(createTeamUA))
                        .thenReturn(Mono.just(createTeamUA));
                when(teamSource.sendTentativeQueueUpdateEvent(tentativeArgumentCaptor.capture(), causedByCaptor.capture()))
                        .thenReturn(Mono.just(Event.builder().build()));
                when(teamSource.sendTeamCreateEvent(teamSourceArgumentCaptor.capture(), causedByCaptor.capture()))
                        .thenReturn(Mono.just(Event.builder().build()));

                StepVerifier
                        .create(userAssignmentService.createTeamAndAssignUser(roleToIdMap,
                                teamName,
                                discordServerId,
                                tournamentId.getTournamentName(),
                                tournamentId.getTournamentDay()))
                        .expectNext(createdTeam)
                        .verifyComplete();

                verify(teamService, times(1))
                        .createClashTeam(expectedTeamToCreate);
                verify(userAssociationService, times(1))
                        .retrieveUsersTeamOrTentativeQueueForTournament(discordId,
                                tournamentId.getTournamentName(),
                                tournamentId.getTournamentDay());
                verify(tentativeService, times(1))
                        .removeUserFromTentativeQueue(discordId, otherTentativeQueue);
                verify(userAssociationService, times(1))
                        .save(createTeamUA);
                verify(teamSource, times(1))
                        .sendTentativeQueueUpdateEvent(any(Tentative.class), anyString());
                verify(teamSource, times(1))
                        .sendTeamCreateEvent(any(Team.class), anyString());

                assertEquals(1, tentativeArgumentCaptor.getAllValues().size(), "More Tentative events executed than expected");
                assertEquals(1, teamSourceArgumentCaptor.getAllValues().size(), "More Team events executed than expected");
                verifyTentativeEvent(discordId, tentativeQueueToBeRemovedFrom.getTentativeId().getServerId(), tentativeQueueToBeRemovedFrom, tentativeArgumentCaptor.getAllValues().get(0), discordId);
                verifyTeamEvent(discordId, discordServerId, createdTeam, teamSourceArgumentCaptor.getAllValues().get(0), discordId);
            }

            @Test
            @DisplayName("(Error) The tournament passed does not exist")
            void test6() {
                String discordId = "1";
                String teamName = "team name";
                String discordServerId = "123";
                String createdTeamId = "ct-1234";
                String otherTentativeQueue = "tq-1234";

                TournamentId tournamentId = easyRandom.nextObject(TournamentId.class);
                HashMap<Role, BasePlayerRecord> positions = new HashMap<>();
                positions.put(Role.TOP, BasePlayerRecord.builder()
                        .discordId(discordId)
                        .build());
                HashMap<Role, String> roleToIdMap = new HashMap<>();
                roleToIdMap.put(Role.TOP, discordId);
                ClashTeam expectedTeamToCreate = ClashTeam.builder()
                        .teamId(TeamId.builder()
                                .tournamentId(tournamentId)
                                .build())
                        .teamName(teamName)
                        .serverId(discordServerId)
                        .positions(positions)
                        .build();

                ClashTeam createdTeam = ClashTeam.builder()
                        .teamId(TeamId.builder()
                                .tournamentId(tournamentId)
                                .id(createdTeamId)
                                .build())
                        .teamName(teamName)
                        .serverId(discordServerId)
                        .positions(positions)
                        .build();

                UserAssociation createTeamUA = UserAssociation.builder()
                        .userAssociationKey(UserAssociationKey.builder()
                                .tournamentId(tournamentId)
                                .discordId(discordId)
                                .build())
                        .teamId(createdTeamId)
                        .serverId(discordServerId)
                        .build();
                TentativeQueue tentativeQueueToBeRemovedFrom = easyRandom.nextObject(TentativeQueue.class);

                when(tournamentService.isTournamentActive(tournamentId.getTournamentName(), tournamentId.getTournamentDay()))
                        .thenReturn(Mono.just(false));
                when(userAssociationService.retrieveUsersTeamOrTentativeQueueForTournament(discordId,
                        tournamentId.getTournamentName(),
                        tournamentId.getTournamentDay()))
                        .thenReturn(Mono.just(UserAssociation.builder()
                                .tentativeId(otherTentativeQueue)
                                .userAssociationKey(UserAssociationKey.builder()
                                        .discordId(discordId)
                                        .tournamentId(tournamentId)
                                        .build())
                                .serverId(discordServerId)
                                .build()));
                when(tentativeService.removeUserFromTentativeQueue(discordId, otherTentativeQueue))
                        .thenReturn(Mono.just(tentativeQueueToBeRemovedFrom));
                when(teamService.createClashTeam(expectedTeamToCreate))
                        .thenReturn(Mono.just(createdTeam));
                when(userAssociationService.save(createTeamUA))
                        .thenReturn(Mono.just(createTeamUA));

                StepVerifier
                        .create(userAssignmentService.createTeamAndAssignUser(roleToIdMap,
                                teamName,
                                discordServerId,
                                tournamentId.getTournamentName(),
                                tournamentId.getTournamentDay()))
                        .expectErrorMessage("Tournament not found.")
                        .verify();

                verify(teamService, times(0))
                        .createClashTeam(expectedTeamToCreate);
                verify(userAssociationService, times(0))
                        .retrieveUsersTeamOrTentativeQueueForTournament(discordId,
                                tournamentId.getTournamentName(),
                                tournamentId.getTournamentDay());
                verify(tentativeService, times(0))
                        .removeUserFromTentativeQueue(discordId, otherTentativeQueue);
                verify(userAssociationService, times(0))
                        .save(createTeamUA);
            }

        }

        @Nested
        @DisplayName("Remove from Team")
        class Remove {

            @Test
            @DisplayName("Team -> Nothing - If a user belongs to a Team, they should be removed and their association should be removed")
            void test_removeUserFromTeam() {
                String discordId = "1";
                String clashTeamId = "ct-1234";
                String serverId = "1234";

                HashMap<Role, BasePlayerRecord> positions = new HashMap<>();
                positions.put(Role.TOP, BasePlayerRecord.builder()
                        .discordId(discordId)
                        .build());
                TournamentId tournamentId = easyRandom.nextObject(TournamentId.class);
                ClashTeam teamToBeRemovedFrom = ClashTeam.builder()
                        .teamId(TeamId.builder()
                                .id(clashTeamId)
                                .tournamentId(tournamentId)
                                .build())
                        .teamName("The team")
                        .serverId(serverId)
                        .positions(positions)
                        .build();
                ClashTeam teamAfterUpdate = ClashTeam.builder()
                        .teamId(TeamId.builder()
                                .id(clashTeamId)
                                .tournamentId(tournamentId)
                                .build())
                        .teamName("The team")
                        .serverId(serverId)
                        .positions(new HashMap<>())
                        .build();
                when(teamService.findTeamById(clashTeamId))
                        .thenReturn(Mono.just(teamToBeRemovedFrom));
                when(teamService.upsertClashTeam(teamAfterUpdate))
                        .thenReturn(Mono.just(teamAfterUpdate));
                PublisherProbe<Void> userAsscProbe = PublisherProbe.empty();
                UserAssociationKey associationKey = UserAssociationKey.builder()
                        .discordId(discordId)
                        .tournamentId(tournamentId)
                        .build();
                when(userAssociationService.delete(associationKey))
                        .thenReturn(userAsscProbe.mono());

                StepVerifier
                        .create(userAssignmentService.findAndRemoveUserFromTeam(discordId, clashTeamId))
                        .expectNext(teamAfterUpdate)
                        .verifyComplete();

                verify(teamService, times(1))
                        .findTeamById(clashTeamId);
                verify(teamService, times(1))
                        .upsertClashTeam(teamAfterUpdate);
                verify(userAssociationService, times(1))
                        .delete(associationKey);
            }

            @Test
            @DisplayName("(Error) User is not on the Team passed")
            void test_removeUserFromTeam_userDoesNotBelongOnTheTeam() {
                String discordId = "1";
                String clashTeamId = "ct-1234";
                String serverId = "1234";

                HashMap<Role, BasePlayerRecord> positions = new HashMap<>();
                TournamentId tournamentId = easyRandom.nextObject(TournamentId.class);
                ClashTeam teamToBeRemovedFrom = ClashTeam.builder()
                        .teamId(TeamId.builder()
                                .id(clashTeamId)
                                .tournamentId(tournamentId)
                                .build())
                        .teamName("The team")
                        .serverId(serverId)
                        .positions(positions)
                        .build();
                ClashTeam teamAfterUpdate = ClashTeam.builder()
                        .teamId(TeamId.builder()
                                .id(clashTeamId)
                                .tournamentId(tournamentId)
                                .build())
                        .teamName("The team")
                        .serverId(serverId)
                        .positions(new HashMap<>())
                        .build();
                when(teamService.findTeamById(clashTeamId))
                        .thenReturn(Mono.just(teamToBeRemovedFrom));
                when(teamService.upsertClashTeam(teamAfterUpdate))
                        .thenReturn(Mono.just(teamAfterUpdate));
                PublisherProbe<Void> userAsscProbe = PublisherProbe.empty();
                UserAssociationKey associationKey = UserAssociationKey.builder()
                        .discordId(discordId)
                        .tournamentId(tournamentId)
                        .build();
                when(userAssociationService.delete(associationKey))
                        .thenReturn(userAsscProbe.mono());

                StepVerifier
                        .create(userAssignmentService.findAndRemoveUserFromTeam(discordId, clashTeamId))
                        .expectErrorMessage(MessageFormat.format("User {0} does not belong on Team {1}.", discordId, clashTeamId))
                        .verify();

                verify(teamService, times(1))
                        .findTeamById(clashTeamId);
                verify(teamService, times(0))
                        .upsertClashTeam(teamAfterUpdate);
                verify(userAssociationService, times(0))
                        .delete(associationKey);
            }

            @Test
            @DisplayName("(Error) Team does not exist")
            void test_removeUserFromTeam_theTeamDoesNotExist() {
                String discordId = "1";
                String clashTeamId = "ct-1234";
                String serverId = "1234";

                TournamentId tournamentId = easyRandom.nextObject(TournamentId.class);
                ClashTeam teamAfterUpdate = ClashTeam.builder()
                        .teamId(TeamId.builder()
                                .id(clashTeamId)
                                .tournamentId(tournamentId)
                                .build())
                        .teamName("The team")
                        .serverId(serverId)
                        .positions(new HashMap<>())
                        .build();
                when(teamService.findTeamById(clashTeamId))
                        .thenReturn(Mono.empty());
                when(teamService.upsertClashTeam(teamAfterUpdate))
                        .thenReturn(Mono.just(teamAfterUpdate));
                PublisherProbe<Void> userAsscProbe = PublisherProbe.empty();
                UserAssociationKey associationKey = UserAssociationKey.builder()
                        .discordId(discordId)
                        .tournamentId(tournamentId)
                        .build();
                when(userAssociationService.delete(associationKey))
                        .thenReturn(userAsscProbe.mono());

                StepVerifier
                        .create(userAssignmentService.findAndRemoveUserFromTeam(discordId, clashTeamId))
                        .expectErrorMessage(MessageFormat.format("Team {0} does not exist.", clashTeamId))
                        .verify();

                verify(teamService, times(1))
                        .findTeamById(clashTeamId);
                verify(teamService, times(0))
                        .upsertClashTeam(teamAfterUpdate);
                verify(userAssociationService, times(0))
                        .delete(associationKey);
            }
        }
    }

    private void verifyTentativeEvent(String discordId, String serverId, TentativeQueue updatedTentativeQueue, Tentative updatedTentative, String actualCausedBy) {
        assertNotNull(updatedTentative.getId());
        assertEquals(discordId, actualCausedBy);
        assertEquals(serverId, updatedTentative.getServerId());
        assertEquals(tentativeMapper.tentativeQueueToTentative(updatedTentativeQueue), updatedTentative);
    }

    private void verifyTeamEvent(String discordId, String discordServerId, ClashTeam createdTeam, Team updateEventSubmitted, String actualCausedBy) {
        assertNotNull(updateEventSubmitted.getId());
        assertEquals(discordId, actualCausedBy);
        assertEquals(discordServerId, updateEventSubmitted.getServerId());
        assertEquals(teamMapper.clashTeamToTeam(createdTeam), updateEventSubmitted);
    }

    @Nested
    @DisplayName("Tentative Queue Interaction")
    class TentativeQueues {

        @Nested
        @DisplayName("Assign to Tentative Queue")
        class Assign {

            @Test
            @DisplayName("Nothing -> Tentative Queue - If a User does not belong to anything and is requesting to be assigned to a Tentative Queue")
            void test() {
                String discordId = "1";
                String tentativeQueueId = "tq-1234";
                String serverId = "1234";

                TournamentId tournamentId = easyRandom.nextObject(TournamentId.class);
                TentativeQueue tentativeQueueToBeAssignedTo = TentativeQueue.builder()
                        .discordIds(new HashSet<>())
                        .tentativeId(TentativeId.builder()
                                .tentativeId(tentativeQueueId)
                                .serverId(serverId)
                                .tournamentId(tournamentId)
                                .build())
                        .build();
                TentativeQueue tentativeQueueAfterAssignment = TentativeQueue.builder()
                        .discordIds(Set.of(discordId))
                        .tentativeId(TentativeId.builder()
                                .tentativeId(tentativeQueueId)
                                .serverId(serverId)
                                .tournamentId(tournamentId)
                                .build())
                        .build();

                UserAssociation userAssociationToSave = UserAssociation.builder()
                        .userAssociationKey(UserAssociationKey.builder()
                                .discordId(discordId)
                                .tournamentId(tournamentId)
                                .build())
                        .tentativeId(tentativeQueueId)
                        .serverId(serverId)
                        .build();


                when(tentativeService.findById(tentativeQueueId))
                        .thenReturn(Mono.just(tentativeQueueToBeAssignedTo));
                when(userAssociationService.retrieveUsersTeamOrTentativeQueueForTournament(
                        discordId,
                        tournamentId.getTournamentName(),
                        tournamentId.getTournamentDay()
                )).thenReturn(Mono.empty());
                when(tentativeService.assignUserToTentativeQueue(discordId, tentativeQueueToBeAssignedTo))
                        .thenReturn(Mono.just(tentativeQueueAfterAssignment));
                when(userAssociationService.save(userAssociationToSave))
                        .thenReturn(Mono.just(userAssociationToSave));
                when(teamSource.sendTentativeQueueUpdateEvent(tentativeArgumentCaptor.capture(), causedByCaptor.capture()))
                        .thenReturn(Mono.just(Event.builder().build()));

                StepVerifier
                        .create(userAssignmentService.assignUserToTentativeQueue(discordId, tentativeQueueId))
                        .expectNext(tentativeQueueAfterAssignment)
                        .verifyComplete();

                verify(tentativeService, times(1))
                        .findById(tentativeQueueId);
                verify(userAssociationService, times(1))
                        .retrieveUsersTeamOrTentativeQueueForTournament(
                                discordId,
                                tournamentId.getTournamentName(),
                                tournamentId.getTournamentDay()
                        );
                verify(tentativeService, times(1))
                        .assignUserToTentativeQueue(discordId, tentativeQueueToBeAssignedTo);
                verify(userAssociationService, times(1))
                        .save(userAssociationToSave);
                verify(teamSource, times(1))
                        .sendTentativeQueueUpdateEvent(any(Tentative.class), anyString());

                assertEquals(1, tentativeArgumentCaptor.getAllValues().size());
                verifyTentativeEvent(discordId, serverId, tentativeQueueAfterAssignment, tentativeArgumentCaptor.getAllValues().get(0), discordId);
            }

            @Test
            @DisplayName("Another Team -> Tentative Queue - If a User belongs to a Team and is requesting to be assigned to a Tentative Queue, they should be removed and assigned to the Queue")
            void test2() {
                String discordId = "1";
                String tentativeQueueId = "tq-1234";
                String serverId = "1234";
                String clashTeamId = "ct-12345";
                TournamentId tournamentId = easyRandom.nextObject(TournamentId.class);

                TentativeQueue tentativeQueueToBeAssignedTo = TentativeQueue.builder()
                        .discordIds(new HashSet<>())
                        .tentativeId(TentativeId.builder()
                                .tentativeId(tentativeQueueId)
                                .serverId(serverId)
                                .tournamentId(tournamentId)
                                .build())
                        .build();
                TentativeQueue tentativeQueueAfterAssignment = TentativeQueue.builder()
                        .discordIds(Set.of(discordId))
                        .tentativeId(TentativeId.builder()
                                .tentativeId(tentativeQueueId)
                                .serverId(serverId)
                                .tournamentId(tournamentId)
                                .build())
                        .build();
                UserAssociation teamUserAssociation = UserAssociation.builder()
                        .userAssociationKey(UserAssociationKey.builder()
                                .discordId(discordId)
                                .tournamentId(tournamentId)
                                .build())
                        .teamId(clashTeamId)
                        .serverId(serverId)
                        .build();

                UserAssociation userAssociationToSave = UserAssociation.builder()
                        .userAssociationKey(UserAssociationKey.builder()
                                .discordId(discordId)
                                .tournamentId(tournamentId)
                                .build())
                        .tentativeId(tentativeQueueId)
                        .serverId(serverId)
                        .build();


                when(tentativeService.findById(tentativeQueueId))
                        .thenReturn(Mono.just(tentativeQueueToBeAssignedTo));
                when(userAssociationService.retrieveUsersTeamOrTentativeQueueForTournament(
                        discordId,
                        tournamentId.getTournamentName(),
                        tournamentId.getTournamentDay()
                )).thenReturn(Mono.just(teamUserAssociation));
                when(teamService.removeUserFromTeam(clashTeamId, discordId))
                        .thenReturn(Mono.just(ClashTeam.builder()
                                .teamId(TeamId.builder()
                                        .id(clashTeamId)
                                        .tournamentId(tournamentId)
                                        .build())
                                .build()));
                when(tentativeService.assignUserToTentativeQueue(discordId, tentativeQueueToBeAssignedTo))
                        .thenReturn(Mono.just(tentativeQueueAfterAssignment));
                when(userAssociationService.save(userAssociationToSave))
                        .thenReturn(Mono.just(userAssociationToSave));
                when(teamSource.sendTeamUpdateEvent(teamSourceArgumentCaptor.capture(), causedByCaptor.capture()))
                        .thenReturn(Mono.just(Event.builder().build()));
                when(teamSource.sendTentativeQueueUpdateEvent(tentativeArgumentCaptor.capture(), causedByCaptor.capture()))
                        .thenReturn(Mono.just(Event.builder().build()));

                StepVerifier
                        .create(userAssignmentService.assignUserToTentativeQueue(discordId, tentativeQueueId))
                        .expectNext(tentativeQueueAfterAssignment)
                        .verifyComplete();

                verify(tentativeService, times(1))
                        .findById(tentativeQueueId);
                verify(userAssociationService, times(1))
                        .retrieveUsersTeamOrTentativeQueueForTournament(
                                discordId,
                                tournamentId.getTournamentName(),
                                tournamentId.getTournamentDay()
                        );
                verify(teamService, times(1))
                        .removeUserFromTeam(clashTeamId, discordId);
                verify(tentativeService, times(1))
                        .assignUserToTentativeQueue(discordId, tentativeQueueToBeAssignedTo);
                verify(userAssociationService, times(1))
                        .save(userAssociationToSave);
                verify(teamSource, times(1))
                        .sendTeamUpdateEvent(any(Team.class), anyString());
                verify(teamSource, times(1))
                        .sendTentativeQueueUpdateEvent(any(Tentative.class), anyString());
            }

            @Test
            @DisplayName("(Error) Tentative Queue does not exist")
            void test3() {
                String discordId = "1";
                String tentativeQueueId = "tq-1234";
                String serverId = "1234";
                String clashTeamId = "ct-12345";
                TournamentId tournamentId = easyRandom.nextObject(TournamentId.class);

                TentativeQueue tentativeQueueToBeAssignedTo = TentativeQueue.builder()
                        .discordIds(new HashSet<>())
                        .tentativeId(TentativeId.builder()
                                .tentativeId(tentativeQueueId)
                                .serverId(serverId)
                                .tournamentId(tournamentId)
                                .build())
                        .build();
                TentativeQueue tentativeQueueAfterAssignment = TentativeQueue.builder()
                        .discordIds(Set.of(discordId))
                        .tentativeId(TentativeId.builder()
                                .tentativeId(tentativeQueueId)
                                .serverId(serverId)
                                .tournamentId(tournamentId)
                                .build())
                        .build();
                UserAssociation teamUserAssociation = UserAssociation.builder()
                        .userAssociationKey(UserAssociationKey.builder()
                                .discordId(discordId)
                                .tournamentId(tournamentId)
                                .build())
                        .teamId(clashTeamId)
                        .serverId(serverId)
                        .build();

                UserAssociation userAssociationToSave = UserAssociation.builder()
                        .userAssociationKey(UserAssociationKey.builder()
                                .discordId(discordId)
                                .tournamentId(tournamentId)
                                .build())
                        .tentativeId(tentativeQueueId)
                        .serverId(serverId)
                        .build();


                when(tentativeService.findById(tentativeQueueId))
                        .thenReturn(Mono.empty());
                when(userAssociationService.retrieveUsersTeamOrTentativeQueueForTournament(
                        discordId,
                        tournamentId.getTournamentName(),
                        tournamentId.getTournamentDay()
                )).thenReturn(Mono.just(teamUserAssociation));
                when(teamService.removeUserFromTeam(clashTeamId, discordId))
                        .thenReturn(Mono.just(ClashTeam.builder()
                                .teamId(TeamId.builder()
                                        .id(clashTeamId)
                                        .tournamentId(tournamentId)
                                        .build())
                                .build()));
                when(tentativeService.assignUserToTentativeQueue(discordId, tentativeQueueToBeAssignedTo))
                        .thenReturn(Mono.just(tentativeQueueAfterAssignment));
                when(userAssociationService.save(userAssociationToSave))
                        .thenReturn(Mono.just(userAssociationToSave));

                StepVerifier
                        .create(userAssignmentService.assignUserToTentativeQueue(discordId, tentativeQueueId))
                        .expectErrorMessage("Tentative Queue does not exist.")
                        .verify();

                verify(tentativeService, times(1))
                        .findById(tentativeQueueId);
                verify(userAssociationService, times(0))
                        .retrieveUsersTeamOrTentativeQueueForTournament(
                                discordId,
                                tournamentId.getTournamentName(),
                                tournamentId.getTournamentDay()
                        );
                verify(teamService, times(0))
                        .removeUserFromTeam(clashTeamId, discordId);
                verify(tentativeService, times(0))
                        .assignUserToTentativeQueue(discordId, tentativeQueueToBeAssignedTo);
                verify(userAssociationService, times(0))
                        .save(userAssociationToSave);
            }

        }

        @Nested
        @DisplayName("Create a Tentative Queue")
        class Create {

            @Test
            @DisplayName("Nothing -> Tentative Queue - A User is not assigned to anything before and wants to create a Tentative Queue")
            void test() {
                String discordId = "1";
                String serverId = "1234";
                String tentativeId = "tq-12345";
                TournamentId tournamentId = easyRandom.nextObject(TournamentId.class);
                TentativeQueue tentativeQueueToCreate = TentativeQueue.builder()
                        .tentativeId(TentativeId.builder()
                                .serverId(serverId)
                                .tournamentId(tournamentId)
                                .build())
                        .discordIds(Set.of(discordId))
                        .build();
                TentativeQueue createdTentativeQueue = TentativeQueue.builder()
                        .tentativeId(TentativeId.builder()
                                .tentativeId(tentativeId)
                                .serverId(serverId)
                                .tournamentId(tournamentId)
                                .build())
                        .discordIds(Set.of(discordId))
                        .build();
                UserAssociation createdUserAssociation = UserAssociation.builder()
                        .tentativeId(tentativeId)
                        .userAssociationKey(UserAssociationKey.builder()
                                .discordId(discordId)
                                .tournamentId(tournamentId)
                                .build())
                        .serverId(serverId)
                        .build();

                when(tournamentService.isTournamentActive(tournamentId.getTournamentName(), tournamentId.getTournamentDay()))
                        .thenReturn(Mono.just(true));
                when(tentativeService.doesServerAlreadyHaveATentativeQueueForTournament(
                        serverId,
                        tournamentId.getTournamentName(),
                        tournamentId.getTournamentDay()))
                        .thenReturn(Mono.just(false));
                when(userAssociationService.retrieveUsersTeamOrTentativeQueueForTournament(discordId,
                        tournamentId.getTournamentName(),
                        tournamentId.getTournamentDay()))
                        .thenReturn(Mono.empty());
                when(tentativeService.createTentativeQueue(tentativeQueueToCreate))
                        .thenReturn(Mono.just(createdTentativeQueue));
                when(userAssociationService.save(createdUserAssociation))
                        .thenReturn(Mono.just(createdUserAssociation));
                when(teamSource.sendTentativeQueueCreateEvent(tentativeArgumentCaptor.capture(), causedByCaptor.capture()))
                        .thenReturn(Mono.just(Event.builder().build()));

                StepVerifier
                        .create(userAssignmentService.createTentativeQueueAndAssignUser(
                                Set.of(discordId),
                                serverId,
                                tournamentId.getTournamentName(),
                                tournamentId.getTournamentDay()))
                        .expectNext(createdTentativeQueue)
                        .verifyComplete();

                verify(tournamentService, times(1))
                        .isTournamentActive(
                                tournamentId.getTournamentName(),
                                tournamentId.getTournamentDay());
                verify(tentativeService, times(1))
                        .doesServerAlreadyHaveATentativeQueueForTournament(
                                serverId,
                                tournamentId.getTournamentName(),
                                tournamentId.getTournamentDay());
                verify(userAssociationService, times(1))
                        .retrieveUsersTeamOrTentativeQueueForTournament(
                                discordId,
                                tournamentId.getTournamentName(),
                                tournamentId.getTournamentDay());
                verify(tentativeService, times(1))
                        .createTentativeQueue(tentativeQueueToCreate);
                verify(userAssociationService, times(1))
                        .save(createdUserAssociation);
                verify(teamSource, times(1))
                        .sendTentativeQueueCreateEvent(any(Tentative.class), anyString());

                assertEquals(1, tentativeArgumentCaptor.getAllValues().size());
                verifyTentativeEvent(discordId, serverId, createdTentativeQueue, tentativeArgumentCaptor.getAllValues().get(0), discordId);
            }

            @Test
            @DisplayName("Team -> Tentative Queue - A User is assigned to a Team and wants to create a Tentative Queue")
            void test2() {
                String discordId = "1";
                String serverId = "1234";
                String tentativeId = "tq-12345";
                String clashTeamId = "ct-12345";
                TournamentId tournamentId = easyRandom.nextObject(TournamentId.class);
                TentativeQueue tentativeQueueToCreate = TentativeQueue.builder()
                        .tentativeId(TentativeId.builder()
                                .serverId(serverId)
                                .tournamentId(tournamentId)
                                .build())
                        .discordIds(Set.of(discordId))
                        .build();
                TentativeQueue createdTentativeQueue = TentativeQueue.builder()
                        .tentativeId(TentativeId.builder()
                                .tentativeId(tentativeId)
                                .serverId(serverId)
                                .tournamentId(tournamentId)
                                .build())
                        .discordIds(Set.of(discordId))
                        .build();
                UserAssociation currentAssociation = UserAssociation.builder()
                        .teamId(clashTeamId)
                        .userAssociationKey(UserAssociationKey.builder()
                                .discordId(discordId)
                                .tournamentId(tournamentId)
                                .build())
                        .serverId(serverId)
                        .build();
                UserAssociation createdUserAssociation = UserAssociation.builder()
                        .tentativeId(tentativeId)
                        .userAssociationKey(UserAssociationKey.builder()
                                .discordId(discordId)
                                .tournamentId(tournamentId)
                                .build())
                        .serverId(serverId)
                        .build();
                ClashTeam returnedClashTeam = ClashTeam.builder()
                        .teamId(TeamId.builder()
                                .id(clashTeamId)
                                .tournamentId(tournamentId)
                                .build())
                        .serverId("1234")
                        .build();

                when(tournamentService.isTournamentActive(tournamentId.getTournamentName(), tournamentId.getTournamentDay()))
                        .thenReturn(Mono.just(true));
                when(tentativeService.doesServerAlreadyHaveATentativeQueueForTournament(
                        serverId,
                        tournamentId.getTournamentName(),
                        tournamentId.getTournamentDay()))
                        .thenReturn(Mono.just(false));
                when(userAssociationService.retrieveUsersTeamOrTentativeQueueForTournament(discordId,
                        tournamentId.getTournamentName(),
                        tournamentId.getTournamentDay()))
                        .thenReturn(Mono.just(currentAssociation));
                when(teamService.removeUserFromTeam(clashTeamId, discordId))
                        .thenReturn(Mono.just(returnedClashTeam));
                when(tentativeService.createTentativeQueue(tentativeQueueToCreate))
                        .thenReturn(Mono.just(createdTentativeQueue));
                when(userAssociationService.save(createdUserAssociation))
                        .thenReturn(Mono.just(createdUserAssociation));
                when(teamSource.sendTeamUpdateEvent(teamSourceArgumentCaptor.capture(), causedByCaptor.capture()))
                        .thenReturn(Mono.just(Event.builder().build()));
                when(teamSource.sendTentativeQueueCreateEvent(tentativeArgumentCaptor.capture(), causedByCaptor.capture()))
                        .thenReturn(Mono.just(Event.builder().build()));

                StepVerifier
                        .create(userAssignmentService.createTentativeQueueAndAssignUser(
                                Set.of(discordId),
                                serverId,
                                tournamentId.getTournamentName(),
                                tournamentId.getTournamentDay()))
                        .expectNext(createdTentativeQueue)
                        .verifyComplete();

                verify(tournamentService, times(1))
                        .isTournamentActive(
                                tournamentId.getTournamentName(),
                                tournamentId.getTournamentDay());
                verify(tentativeService, times(1))
                        .doesServerAlreadyHaveATentativeQueueForTournament(
                                serverId,
                                tournamentId.getTournamentName(),
                                tournamentId.getTournamentDay());
                verify(userAssociationService, times(1))
                        .retrieveUsersTeamOrTentativeQueueForTournament(
                                discordId,
                                tournamentId.getTournamentName(),
                                tournamentId.getTournamentDay());
                verify(teamService, times(1))
                        .removeUserFromTeam(clashTeamId, discordId);
                verify(tentativeService, times(1))
                        .createTentativeQueue(tentativeQueueToCreate);
                verify(userAssociationService, times(1))
                        .save(createdUserAssociation);

                assertAll(() -> {
                    assertEquals(1, teamSourceArgumentCaptor.getAllValues().size());
                    Team updateEventSubmitted = teamSourceArgumentCaptor.getAllValues().get(0);
                    assertNotNull(updateEventSubmitted.getId());
                    assertEquals(discordId, causedByCaptor.getAllValues().get(0));
                    assertEquals(serverId, updateEventSubmitted.getServerId());
                    assertEquals(teamMapper.clashTeamToTeam(returnedClashTeam), updateEventSubmitted);

                    assertEquals(1, tentativeArgumentCaptor.getAllValues().size());
                    Tentative createEventSubmitted = tentativeArgumentCaptor.getAllValues().get(0);
                    assertNotNull(createEventSubmitted.getId());
                    assertEquals(discordId, causedByCaptor.getAllValues().get(1));
                    assertEquals(serverId, createEventSubmitted.getServerId());
                    assertEquals(tentativeMapper.tentativeQueueToTentative(createdTentativeQueue), createEventSubmitted);
                });
            }

            @Test
            @DisplayName("(Error) Tournament not found")
            void test3() {
                String discordId = "1";
                String serverId = "1234";
                String tentativeId = "tq-12345";
                TournamentId tournamentId = easyRandom.nextObject(TournamentId.class);
                TentativeQueue tentativeQueueToCreate = TentativeQueue.builder()
                        .tentativeId(TentativeId.builder()
                                .serverId(serverId)
                                .tournamentId(tournamentId)
                                .build())
                        .discordIds(Set.of(discordId))
                        .build();
                TentativeQueue createdTentativeQueue = TentativeQueue.builder()
                        .tentativeId(TentativeId.builder()
                                .tentativeId(tentativeId)
                                .serverId(serverId)
                                .tournamentId(tournamentId)
                                .build())
                        .discordIds(Set.of(discordId))
                        .build();
                UserAssociation createdUserAssociation = UserAssociation.builder()
                        .tentativeId(tentativeId)
                        .userAssociationKey(UserAssociationKey.builder()
                                .discordId(discordId)
                                .tournamentId(tournamentId)
                                .build())
                        .serverId(serverId)
                        .build();

                when(tournamentService.isTournamentActive(tournamentId.getTournamentName(), tournamentId.getTournamentDay()))
                        .thenReturn(Mono.just(false));
                when(tentativeService.doesServerAlreadyHaveATentativeQueueForTournament(
                        serverId,
                        tournamentId.getTournamentName(),
                        tournamentId.getTournamentDay()))
                        .thenReturn(Mono.just(false));
                when(userAssociationService.retrieveUsersTeamOrTentativeQueueForTournament(discordId,
                        tournamentId.getTournamentName(),
                        tournamentId.getTournamentDay()))
                        .thenReturn(Mono.empty());
                when(tentativeService.createTentativeQueue(tentativeQueueToCreate))
                        .thenReturn(Mono.just(createdTentativeQueue));
                when(userAssociationService.save(createdUserAssociation))
                        .thenReturn(Mono.just(createdUserAssociation));

                StepVerifier
                        .create(userAssignmentService.createTentativeQueueAndAssignUser(
                                Set.of(discordId),
                                serverId,
                                tournamentId.getTournamentName(),
                                tournamentId.getTournamentDay()))
                        .expectErrorMessage("Tournament not found.")
                        .verify();

                verify(tournamentService, times(1))
                        .isTournamentActive(
                                tournamentId.getTournamentName(),
                                tournamentId.getTournamentDay());
                verify(tentativeService, times(0))
                        .doesServerAlreadyHaveATentativeQueueForTournament(
                                serverId,
                                tournamentId.getTournamentName(),
                                tournamentId.getTournamentDay());
                verify(userAssociationService, times(0))
                        .retrieveUsersTeamOrTentativeQueueForTournament(
                                discordId,
                                tournamentId.getTournamentName(),
                                tournamentId.getTournamentDay());
                verify(tentativeService, times(0))
                        .createTentativeQueue(tentativeQueueToCreate);
                verify(userAssociationService, times(0))
                        .save(createdUserAssociation);
            }


            @Test
            @DisplayName("(Error) Tentative Queue already exists")
            void test4() {
                String discordId = "1";
                String serverId = "1234";
                String tentativeId = "tq-12345";
                TournamentId tournamentId = easyRandom.nextObject(TournamentId.class);
                TentativeQueue tentativeQueueToCreate = TentativeQueue.builder()
                        .tentativeId(TentativeId.builder()
                                .serverId(serverId)
                                .tournamentId(tournamentId)
                                .build())
                        .discordIds(Set.of(discordId))
                        .build();
                TentativeQueue createdTentativeQueue = TentativeQueue.builder()
                        .tentativeId(TentativeId.builder()
                                .tentativeId(tentativeId)
                                .serverId(serverId)
                                .tournamentId(tournamentId)
                                .build())
                        .discordIds(Set.of(discordId))
                        .build();
                UserAssociation createdUserAssociation = UserAssociation.builder()
                        .tentativeId(tentativeId)
                        .userAssociationKey(UserAssociationKey.builder()
                                .discordId(discordId)
                                .tournamentId(tournamentId)
                                .build())
                        .serverId(serverId)
                        .build();

                when(tournamentService.isTournamentActive(tournamentId.getTournamentName(), tournamentId.getTournamentDay()))
                        .thenReturn(Mono.just(true));
                when(tentativeService.doesServerAlreadyHaveATentativeQueueForTournament(
                        serverId,
                        tournamentId.getTournamentName(),
                        tournamentId.getTournamentDay()))
                        .thenReturn(Mono.just(true));
                when(userAssociationService.retrieveUsersTeamOrTentativeQueueForTournament(discordId,
                        tournamentId.getTournamentName(),
                        tournamentId.getTournamentDay()))
                        .thenReturn(Mono.empty());
                when(tentativeService.createTentativeQueue(tentativeQueueToCreate))
                        .thenReturn(Mono.just(createdTentativeQueue));
                when(userAssociationService.save(createdUserAssociation))
                        .thenReturn(Mono.just(createdUserAssociation));

                StepVerifier
                        .create(userAssignmentService.createTentativeQueueAndAssignUser(
                                Set.of(discordId),
                                serverId,
                                tournamentId.getTournamentName(),
                                tournamentId.getTournamentDay()))
                        .expectErrorMessage("Tentative Queue already exists.")
                        .verify();

                verify(tournamentService, times(1))
                        .isTournamentActive(
                                tournamentId.getTournamentName(),
                                tournamentId.getTournamentDay());
                verify(tentativeService, times(1))
                        .doesServerAlreadyHaveATentativeQueueForTournament(
                                serverId,
                                tournamentId.getTournamentName(),
                                tournamentId.getTournamentDay());
                verify(userAssociationService, times(0))
                        .retrieveUsersTeamOrTentativeQueueForTournament(
                                discordId,
                                tournamentId.getTournamentName(),
                                tournamentId.getTournamentDay());
                verify(tentativeService, times(0))
                        .createTentativeQueue(tentativeQueueToCreate);
                verify(userAssociationService, times(0))
                        .save(createdUserAssociation);
            }

        }

        @Nested
        @DisplayName("Remove from Tentative Queue")
        class Remove {

            @Test
            @DisplayName("Tentative Queue -> Nothing - A User wants to remove themselves from a Tentative Queue")
            void test() {
                String discordId = "1";
                String tentativeQueueId = "tq-1234";
                String serverId = "1234";

                TournamentId tournamentId = easyRandom.nextObject(TournamentId.class);
                HashSet<String> discordIds = new HashSet<>();
                discordIds.add("1");
                TentativeQueue tentativeQueueToBeRemovedFrom = TentativeQueue.builder()
                        .discordIds(discordIds)
                        .tentativeId(TentativeId.builder()
                                .tentativeId(tentativeQueueId)
                                .serverId(serverId)
                                .tournamentId(tournamentId)
                                .build())
                        .build();
                TentativeQueue tentativeQueueAfterRemoval = TentativeQueue.builder()
                        .discordIds(new HashSet<>())
                        .tentativeId(TentativeId.builder()
                                .tentativeId(tentativeQueueId)
                                .serverId(serverId)
                                .tournamentId(tournamentId)
                                .build())
                        .build();

                UserAssociation userAssociationToSave = UserAssociation.builder()
                        .userAssociationKey(UserAssociationKey.builder()
                                .discordId(discordId)
                                .tournamentId(tournamentId)
                                .build())
                        .tentativeId(tentativeQueueId)
                        .serverId(serverId)
                        .build();

                when(tentativeService.findById(tentativeQueueId))
                        .thenReturn(Mono.just(tentativeQueueToBeRemovedFrom));
                when(tentativeService.removeUserFromTentativeQueue(discordId, tentativeQueueId))
                        .thenReturn(Mono.just(tentativeQueueToBeRemovedFrom));
                PublisherProbe<Void> publisherProbe = PublisherProbe.empty();
                when(userAssociationService.delete(userAssociationToSave.getUserAssociationKey()))
                        .thenReturn(publisherProbe.mono());
                when(teamSource.sendTentativeQueueUpdateEvent(tentativeArgumentCaptor.capture(), causedByCaptor.capture()))
                        .thenReturn(Mono.just(Event.builder().build()));

                StepVerifier
                        .create(userAssignmentService.findAndRemoveUserFromTentativeQueue(discordId, tentativeQueueId))
                        .expectNext(tentativeQueueAfterRemoval)
                        .verifyComplete();

                verify(tentativeService, times(1))
                        .findById(tentativeQueueId);
                verify(tentativeService, times(1))
                        .removeUserFromTentativeQueue(discordId, tentativeQueueId);
                verify(userAssociationService, times(1))
                        .delete(userAssociationToSave.getUserAssociationKey());

                assertAll(() -> {
                    Tentative removeEventSubmitted = tentativeArgumentCaptor.getAllValues().get(0);
                    verifyTentativeEvent(discordId, serverId, tentativeQueueToBeRemovedFrom, removeEventSubmitted, discordId);
                });
            }

            @Test
            @DisplayName("(Error) Tentative Queue does not exist")
            void test2() {
                String discordId = "1";
                String tentativeQueueId = "tq-1234";
                String serverId = "1234";

                TournamentId tournamentId = easyRandom.nextObject(TournamentId.class);

                UserAssociation userAssociationToSave = UserAssociation.builder()
                        .userAssociationKey(UserAssociationKey.builder()
                                .discordId(discordId)
                                .tournamentId(tournamentId)
                                .build())
                        .tentativeId(tentativeQueueId)
                        .serverId(serverId)
                        .build();
                TentativeQueue tentativeQueueToBeRemovedFrom = easyRandom.nextObject(TentativeQueue.class);

                when(tentativeService.findById(tentativeQueueId))
                        .thenReturn(Mono.empty());
                when(tentativeService.removeUserFromTentativeQueue(discordId, tentativeQueueId))
                        .thenReturn(Mono.just(tentativeQueueToBeRemovedFrom));
                PublisherProbe<Void> publisherProbe = PublisherProbe.empty();
                when(userAssociationService.delete(userAssociationToSave.getUserAssociationKey()))
                        .thenReturn(publisherProbe.mono());

                StepVerifier
                        .create(userAssignmentService.findAndRemoveUserFromTentativeQueue(discordId, tentativeQueueId))
                        .expectErrorMessage("Tentative Queue does not exist.")
                        .verify();

                verify(tentativeService, times(1))
                        .findById(tentativeQueueId);
                verify(tentativeService, times(0))
                        .removeUserFromTentativeQueue(discordId, tentativeQueueId);
                verify(userAssociationService, times(0))
                        .delete(userAssociationToSave.getUserAssociationKey());
            }

        }

    }
}
