package com.poss.clash.bot.services;

import com.poss.clash.bot.ClashBotTestingConfig;
import com.poss.clash.bot.daos.UserAssociationDao;
import com.poss.clash.bot.daos.models.*;
import com.poss.clash.bot.openapi.model.Event;
import com.poss.clash.bot.openapi.model.EventType;
import com.poss.clash.bot.openapi.model.Team;
import com.poss.clash.bot.openapi.model.TeamEvent;
import com.poss.clash.bot.source.TeamSource;
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
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.publisher.PublisherProbe;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@Import(ClashBotTestingConfig.class)
class UserAssociationServiceTest {

  @InjectMocks
  UserAssociationService userAssociationService;

  @Mock
  UserAssociationDao userAssociationDaoMock;

  @Mock
  TournamentService tournamentService;

  @Mock
  TeamService teamService;

  @Mock
  UserService userService;

  @Mock
  TeamSource teamSource;

  @Spy
  TeamMapper teamMapper = Mappers.getMapper(TeamMapper.class);

  @Autowired
  EasyRandom easyRandom;

  @Nested
  @DisplayName("Check If User Exists On Team Or Tentative Queue")
  class CheckIfUserExistsOnTeamOrTentativeQueue {

    @Test
    @DisplayName("If a User exists on a Team or Tentative Queue for a Tournament, then it should be returned.")
    void test_retrieveUsersTeamOrTentativeQueueForTournament_ifUserBelongsToTeam_then_returnTeamId() {
      String discordId = easyRandom.nextObject(String.class);
      String serverId = easyRandom.nextObject(String.class);
      String tournamentName = "awesome_sauce";
      String tournamentDay = "1";

      UserAssociationKey userAssociationKey = UserAssociationKey
          .builder()
          .tournamentId(TournamentId
                            .builder()
                            .tournamentName(tournamentName)
                            .tournamentDay(tournamentDay)
                            .build())
          .discordId(discordId)
          .build();

      UserAssociation expectedUserAssociation = UserAssociation
          .builder()
          .teamId("123asdf")
          .serverId(serverId)
          .userAssociationKey(userAssociationKey)
          .build();
      when(userAssociationDaoMock.findById(userAssociationKey))
          .thenReturn(Mono.just(expectedUserAssociation));

      StepVerifier
          .create(userAssociationService.retrieveUsersTeamOrTentativeQueueForTournament(discordId, tournamentName,
                                                                                        tournamentDay))
          .expectNext(expectedUserAssociation)
          .verifyComplete();
    }

  }

  @Nested
  @DisplayName("Update involved Teams")
  class UpdateInvolvedTeams {

    @Test
    @DisplayName("updateInvolvedTeams - should retrieve the user associations for all active tournaments and send an event for each Team")
    void test_updateInvolvedTeams_shouldRetrieveAllActiveAssociationsAndSendAnUpdateEvent() {
      String discordId = easyRandom.nextObject(String.class);
      ClashTournament clashTournament = easyRandom.nextObject(ClashTournament.class);
      ClashTeam clashTeam = easyRandom.nextObject(ClashTeam.class);
      UserAssociation userAssociation = easyRandom.nextObject(UserAssociation.class);
      userAssociation
          .getUserAssociationKey()
          .setDiscordId(discordId);
      userAssociation
          .getUserAssociationKey()
          .setTournamentId(clashTournament.getTournamentId());
      userAssociation.setTentativeId(null);
      UserAssociationKey expectedAssociationKey = UserAssociationKey
          .builder()
          .discordId(discordId)
          .tournamentId(clashTournament.getTournamentId())
          .build();
      Team teamPayload = teamMapper.clashTeamToTeam(clashTeam);
      Event expectedEvent = Event
          .builder()
          .teamEvent(TeamEvent
                         .builder()
                         .eventType(EventType.UPDATED)
                         .team(teamPayload)
                         .build())
          .build();

      when(tournamentService
               .retrieveAllTournaments(true))
          .thenReturn(Mono
                          .just(clashTournament)
                          .flux());
      when(userAssociationDaoMock.findByUserAssociationKeyAndTentativeIdIsNull(
          expectedAssociationKey))
          .thenReturn(Mono
                          .just(userAssociation)
                          .flux());
      when(teamService.findTeamById(userAssociation.getTeamId()))
          .thenReturn(Mono.just(clashTeam));
      when(userService.enrichClashTeamWithUserDetails(clashTeam))
          .thenReturn(Mono.just(clashTeam));
      when(teamSource.sendTeamUpdateEvent(teamPayload))
          .thenReturn(Mono.just(expectedEvent));

      StepVerifier
          .create(userAssociationService.updateInvolvedTeams(discordId))
          .expectNextMatches(event -> {
            assertEquals(expectedEvent, event);
            return true;
          })
          .verifyComplete();

      verify(tournamentService, times(1))
          .retrieveAllTournaments(true);
      verify(userAssociationDaoMock, times(1))
          .findByUserAssociationKeyAndTentativeIdIsNull(expectedAssociationKey);
      verify(teamService, times(1))
          .findTeamById(userAssociation.getTeamId());
      verify(userService, times(1))
          .enrichClashTeamWithUserDetails(clashTeam);
    }

    @Test
    @DisplayName("updateInvolvedTeams - if no associations exist, then it should return empty.")
    void test_updateInvolvedTeams_userDoesNotBelongToATeamShouldReturnEmpty() {
      String discordId = easyRandom.nextObject(String.class);
      ClashTournament clashTournament = easyRandom.nextObject(ClashTournament.class);
      ClashTeam clashTeam = easyRandom.nextObject(ClashTeam.class);
      UserAssociation userAssociation = easyRandom.nextObject(UserAssociation.class);
      userAssociation
          .getUserAssociationKey()
          .setDiscordId(discordId);
      userAssociation
          .getUserAssociationKey()
          .setTournamentId(clashTournament.getTournamentId());
      userAssociation.setTentativeId(null);
      UserAssociationKey expectedAssociationKey = UserAssociationKey
          .builder()
          .discordId(discordId)
          .tournamentId(clashTournament.getTournamentId())
          .build();
      Team teamPayload = teamMapper.clashTeamToTeam(clashTeam);
      Event expectedEvent = Event
          .builder()
          .teamEvent(TeamEvent
                         .builder()
                         .eventType(EventType.UPDATED)
                         .team(teamPayload)
                         .build())
          .build();

      when(tournamentService
               .retrieveAllTournaments(true))
          .thenReturn(Mono
                          .just(clashTournament)
                          .flux());
      when(userAssociationDaoMock.findByUserAssociationKeyAndTentativeIdIsNull(
          expectedAssociationKey))
          .thenReturn(Flux.empty());
      when(teamService.findTeamById(userAssociation.getTeamId()))
          .thenReturn(Mono.just(clashTeam));
      when(userService.enrichClashTeamWithUserDetails(clashTeam))
          .thenReturn(Mono.just(clashTeam));
      when(teamSource.sendTeamUpdateEvent(teamPayload))
          .thenReturn(Mono.just(expectedEvent));

      StepVerifier
          .create(userAssociationService.updateInvolvedTeams(discordId))
          .verifyComplete();

      verify(tournamentService, times(1))
          .retrieveAllTournaments(true);
      verify(userAssociationDaoMock, times(1))
          .findByUserAssociationKeyAndTentativeIdIsNull(expectedAssociationKey);
      verify(teamService, times(0))
          .findTeamById(userAssociation.getTeamId());
      verify(userService, times(0))
          .enrichClashTeamWithUserDetails(clashTeam);
    }

  }

  @Nested
  @DisplayName("Retrieve User Associations")
  class RetrieveUserAssignments {

    @Test
    @DisplayName("retrieveUserAssociationsForATournament - should return a list of user associations for a specific Tournament")
    void test_retrieveUserAssignmentsForATournament() {
      TournamentId tournamentId = easyRandom.nextObject(TournamentId.class);
      UserAssociation userAssociation = easyRandom.nextObject(UserAssociation.class);

      List<TournamentId> tournamentIds = List.of(tournamentId);

      PublisherProbe<UserAssociation> userAssociationPublisherProbe = PublisherProbe.of(Flux.just(userAssociation));
      when(userAssociationDaoMock.findByUserAssociationKey_TournamentIdIsIn(tournamentIds)).thenReturn(
          userAssociationPublisherProbe.flux());

      StepVerifier
          .create(userAssociationService.retrieveUserAssociationsForATournament(tournamentIds))
          .recordWith(ArrayList::new)
          .expectNextCount(1)
          .expectNext()
          .verifyComplete();

      userAssociationPublisherProbe.assertWasSubscribed();
    }

  }

}
