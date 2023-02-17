//package com.poss.clash.bot.services;
//
//import com.poss.clash.bot.ClashBotTestingConfig;
//import com.poss.clash.bot.daos.models.*;
//import com.poss.clash.bot.openapi.model.Role;
//import com.poss.clash.bot.openapi.model.Tentative;
//import com.poss.clash.bot.utils.UserAssociationMapper;
//import com.poss.clash.bot.utils.UserMapper;
//import org.jeasy.random.EasyRandom;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Nested;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mapstruct.factory.Mappers;
//import org.mockito.ArgumentCaptor;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Import;
//import org.springframework.test.context.junit.jupiter.SpringExtension;
//import reactor.core.publisher.Mono;
//import reactor.test.StepVerifier;
//import reactor.test.publisher.PublisherProbe;
//
//import java.util.HashMap;
//import java.util.Map;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertNull;
//import static org.mockito.Mockito.*;
//import static org.mockito.Mockito.times;
//
//@ExtendWith(SpringExtension.class)
//@Import(ClashBotTestingConfig.class)
//public class SwapServiceTest {
//
//    @InjectMocks
//    SwapService swapService;
//
//    @Mock
//    TeamService teamService;
//
//    @Mock
//    TentativeService tentativeService;
//
//    UserAssociationMapper userAssociationMapper = Mappers.getMapper(UserAssociationMapper.class);
//
//    @Autowired
//    EasyRandom easyRandom;
//
//    @Nested
//    @DisplayName("Association to Team")
//    class Associate {
//
//        @Test
//        @DisplayName("associateToTeam - should return the user association if it is empty or if the team id is the same.")
//        void test_associateToTeam_shouldReturnSameAssociationIfItAlreadyBelongsToTheTeamId() {
//            String teamId = "ct-12345";
//
//            UserAssociation userAssociation = UserAssociation.builder()
//                    .teamId(teamId)
//                    .build();
//
//            StepVerifier
//                    .create(swapService.associateToTeam(teamId)
//                            .apply(userAssociation))
//                    .expectNext(userAssociation)
//                    .verifyComplete();
//        }
//
//        @Test
//        @DisplayName("associateToTeam (Tentative Queue -> Clash Team) - if user association belongs to a Tentative Queue then the user should be removed from the Tentative Queue and the User Association should be updated to the Team.")
//        void test_associateToTeam_ifUserAssociationBelongsToTentativeQueueThenRemoveFromTentativeQueueAndMapToTeam() {
//            String teamId = "ct-12345";
//            String tentativeId = "tq-123456";
//            int discordId = 1;
//
//            UserAssociation userAssociation = UserAssociation.builder()
//                    .tentativeId(tentativeId)
//                    .userAssociationKey(UserAssociationKey
//                            .builder()
//                            .discordId(discordId)
//                            .build())
//                    .build();
//
//            UserAssociation expectedUpdatedUserAssociation = UserAssociation.builder()
//                    .teamId(teamId)
//                    .userAssociationKey(UserAssociationKey
//                            .builder()
//                            .discordId(discordId)
//                            .build())
//                    .build();
//
//            when(tentativeService.removeUserFromTentativeQueue(discordId, tentativeId))
//                    .thenReturn(Mono.just(Tentative.builder().build()));
//
//            StepVerifier
//                    .create(swapService.associateToTeam(teamId)
//                            .apply(userAssociation))
//                    .expectNext(expectedUpdatedUserAssociation)
//                    .verifyComplete();
//
//            verify(tentativeService, times(1))
//                    .removeUserFromTentativeQueue(discordId, tentativeId);
//        }
//
//        @Test
//        @DisplayName("associateToTeam (Clash Team 1 -> Clash Team 2) - if user association belongs to a Tentative Queue then the user should be removed from the Tentative Queue and the User Association should be updated to the Team.")
//        void test_associateToTeam_ifUserAssociationBelongsToAnotherTeamThenRemoveFromOtherTeamAndFinalMapToTeam() {
//            String teamIdOne = "ct-12346";
//            String teamIdTwo = "ct-12345";
//            int discordId = 1;
//
//            TournamentId tournamentId = easyRandom.nextObject(TournamentId.class);
//            UserAssociation userAssociation = UserAssociation.builder()
//                    .teamId(teamIdOne)
//                    .userAssociationKey(UserAssociationKey
//                            .builder()
//                            .discordId(discordId)
//                            .tournamentId(tournamentId)
//                            .build())
//                    .build();
//
//            UserAssociation expectedUpdatedUserAssociation = UserAssociation.builder()
//                    .teamId(teamIdTwo)
//                    .userAssociationKey(UserAssociationKey
//                            .builder()
//                            .discordId(discordId)
//                            .tournamentId(tournamentId)
//                            .build())
//                    .build();
//
//            Map<Role, BasePlayerRecord> map = new HashMap<>();
//            map.put(Role.TOP, BasePlayerRecord.builder()
//                    .discordId(1)
//                    .build());
//            TeamId teamId = TeamId.builder()
//                    .id(teamIdOne)
//                    .tournamentId(tournamentId)
//                    .build();
//            ClashTeam foundClashTeam = ClashTeam.builder()
//                    .teamId(teamId)
//                    .positions(map)
//                    .build();
//            ClashTeam updatedClashTeam = ClashTeam.builder()
//                    .teamId(teamId)
//                    .positions(Map.of())
//                    .build();
//            when(teamService.removeUserFromTeam(teamId, discordId))
//                    .thenReturn(Mono.just(updatedClashTeam));
//
//            StepVerifier
//                    .create(swapService.associateToTeam(teamIdTwo)
//                            .apply(userAssociation))
//                    .expectNext(expectedUpdatedUserAssociation)
//                    .verifyComplete();
//        }
//    }
//
//    @Nested
//    @DisplayName("Swap from Team to Tentative")
//    class SwapFromTeamToTentative {
//
//        @Test
//        @DisplayName("Swapping from Team to Tentative - association has a Team Id so it should be nulled and tentativeId should be populated")
//        void test_swapFromTeamToTentative_associationHasTeamIdSoItShouldBeSwapped() {
//            String tqId = "tq-123qwer";
//            Integer discordId = 1;
//            Integer serverId = 1;
//            String teamId = "ct-123asdf";
//            UserAssociation userAssociationWithTeam = UserAssociation.builder()
//                    .userAssociationKey(UserAssociationKey
//                            .builder()
//                            .tournamentId(easyRandom.nextObject(TournamentId.class))
//                            .discordId(discordId)
//                            .build())
//                    .teamId(teamId)
//                    .serverId(serverId)
//                    .build();
//            UserAssociation expectedUserAssociation = userAssociationMapper.clone(userAssociationWithTeam);
//            expectedUserAssociation.setTentativeId(tqId);
//            expectedUserAssociation.setTeamId(null);
//
//            when(teamService.removeUserFromTeam(TeamId.builder()
//                    .id(teamId)
//                    .tournamentId(userAssociationWithTeam.getUserAssociationKey()
//                            .getTournamentId())
//                    .build(), discordId))
//                    .thenReturn(Mono.just(ClashTeam.builder().build()));
//            StepVerifier
//                    .create(swapService.swapFromTeamToTentative(tqId)
//                            .apply(userAssociationWithTeam))
//                    .expectNext(expectedUserAssociation)
//                    .verifyComplete();
//        }
//
//        @Test
//        @DisplayName("Swapping from Team to Tentative - association does not have a Team Id so it should pass through")
//        void test_swapFromTeamToTentative_theAssociationShouldPassThrough() {
//            String tqId = "tq-123qwer";
//            Integer discordId = 1;
//            Integer serverId = 1;
//            String teamId = "ct-123asdf";
//            UserAssociation userAssociationWithTeam = UserAssociation.builder()
//                    .userAssociationKey(UserAssociationKey
//                            .builder()
//                            .tournamentId(easyRandom.nextObject(TournamentId.class))
//                            .discordId(discordId)
//                            .build())
//                    .tentativeId(tqId)
//                    .serverId(serverId)
//                    .build();
//            UserAssociation expectedUserAssociation = userAssociationMapper.clone(userAssociationWithTeam);
//            expectedUserAssociation.setTentativeId(tqId);
//            expectedUserAssociation.setTeamId(null);
//
//            StepVerifier
//                    .create(swapService.swapFromTeamToTentative(tqId)
//                            .apply(userAssociationWithTeam))
//                    .expectNext(expectedUserAssociation)
//                    .verifyComplete();
//            verify(teamService, times(0))
//                    .removeUserFromTeam(TeamId.builder()
//                            .tournamentId(userAssociationWithTeam.getUserAssociationKey().getTournamentId())
//                            .id(teamId)
//                            .build(), discordId);
//        }
//
//        @Test
//        @DisplayName("Swap from Team to Tentative - If the user belongs to a Team, then they should be removed from it.")
//        void test_swapFromTeamToTentative_ifTeamIdExistsOnTheAssociationThenInvokeRemoveFromTeam() {
//            String tentativeQueueId = "tq-1234";
//            String teamId = "ct-1234";
//            UserAssociation userAssociation = UserAssociation.builder()
//                    .userAssociationKey(easyRandom.nextObject(UserAssociationKey.class))
//                    .serverId(1234)
//                    .teamId(teamId)
//                    .build();
//            ArgumentCaptor<TeamId> teamIdArgumentCaptor = ArgumentCaptor.forClass(TeamId.class);
//            ArgumentCaptor<Integer> discordIdCaptor = ArgumentCaptor.forClass(Integer.class);
//
//            when(teamService.removeUserFromTeam(teamIdArgumentCaptor.capture(), discordIdCaptor.capture()))
//                    .thenReturn(Mono.just(easyRandom.nextObject(ClashTeam.class)));
//            swapService.swapFromTeamToTentative(tentativeQueueId)
//                    .apply(userAssociation);
//
//            assertEquals(teamId, teamIdArgumentCaptor.getValue().getId());
//            assertEquals(tentativeQueueId, userAssociation.getTentativeId());
//            assertNull(userAssociation.getTeamId());
//        }
//
//        @Test
//        @DisplayName("Swap from Team to Tentative - If the user does not belong to a Team, then the association should be passed through.")
//        void test_swapFromTeamToTentative_ifTeamIdDoesNotExistOnTheAssociationThenItShouldBePassedThrough() {
//            String tentativeQueueId = "tq-1234";
//            String teamId = "ct-1234";
//            UserAssociation userAssociation = UserAssociation.builder()
//                    .userAssociationKey(easyRandom.nextObject(UserAssociationKey.class))
//                    .serverId(1234)
//                    .tentativeId(tentativeQueueId)
//                    .build();
//            swapService.swapFromTeamToTentative(tentativeQueueId)
//                    .apply(userAssociation);
//
//            assertEquals(tentativeQueueId, userAssociation.getTentativeId());
//            assertNull(userAssociation.getTeamId());
//            verify(teamService, times(0))
//                    .removeUserFromTeam(TeamId.builder()
//                            .id(teamId)
//                            .tournamentId(userAssociation.getUserAssociationKey().getTournamentId())
//                            .build(), userAssociation.getUserAssociationKey().getDiscordId());
//        }
//
//    }
//
//}
