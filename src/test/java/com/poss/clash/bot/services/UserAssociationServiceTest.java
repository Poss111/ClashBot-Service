package com.poss.clash.bot.services;

import com.poss.clash.bot.ClashBotTestingConfig;
import com.poss.clash.bot.daos.UserAssociationDao;
import com.poss.clash.bot.daos.models.TournamentId;
import com.poss.clash.bot.daos.models.UserAssociation;
import com.poss.clash.bot.daos.models.UserAssociationKey;
import com.poss.clash.bot.utils.UserAssociationMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.reactivestreams.Publisher;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@Import(ClashBotTestingConfig.class)
class UserAssociationServiceTest {

    @InjectMocks
    UserAssociationService userAssociationService;

    @Mock
    UserAssociationDao userAssociationDaoMock;

    @Spy
    UserAssociationMapper userAssociationMapper = Mappers.getMapper(UserAssociationMapper.class);

    @Nested
    @DisplayName("Check If User Exists On Team Or Tentative Queue")
    class CheckIfUserExistsOnTeamOrTentativeQueue {

        @Test
        @DisplayName("If a User exists on a Team or Tentative Queue for a Tournament, then it should be returned.")
        void test_retrieveUsersTeamOrTentativeQueueForTournament_ifUserBelongsToTeam_then_returnTeamId() {
            int discordId = 1;
            int serverId = 123;
            String tournamentName = "awesome_sauce";
            String tournamentDay = "1";

            UserAssociationKey userAssociationKey = UserAssociationKey.builder()
                    .tournamentId(TournamentId.builder()
                            .tournamentName(tournamentName)
                            .tournamentDay(tournamentDay)
                            .build())
                    .discordId(discordId)
                    .build();

            UserAssociation expectedUserAssociation = UserAssociation.builder()
                    .teamId("123asdf")
                    .serverId(serverId)
                    .userAssociationKey(userAssociationKey)
                    .build();
            when(userAssociationDaoMock.findById(userAssociationKey))
                    .thenReturn(Mono.just(expectedUserAssociation));

            StepVerifier
                    .create(userAssociationService.retrieveUsersTeamOrTentativeQueueForTournament(discordId, tournamentName, tournamentDay))
                    .expectNext(expectedUserAssociation)
                    .verifyComplete();
        }

    }

    @Nested
    @DisplayName("Swap user associations")
    class SwapBetweenUserAssociationTypes {

        @Test
        @DisplayName("Swap from Team to Tentative")
        void test_swapUserAssociationBetweenTeamAndTentative_ifUsersExistInOtherTeams() {
            Set<Integer> discordIds = Set.of(1, 2, 3);
            Integer serverId = 1;
            String tqId = "tq-123";
            TournamentId tournamentId = TournamentId.builder()
                    .tournamentName("awesome_sauce")
                    .tournamentDay("1")
                    .build();
            List<UserAssociation> uaList = new ArrayList<>();
            UserAssociation userAssociationDefault = UserAssociation.builder()
                    .userAssociationKey(UserAssociationKey
                            .builder()
                            .tournamentId(tournamentId)
                            .build())
                    .tentativeId(tqId)
                    .serverId(serverId)
                    .build();
            Function<UserAssociation, Publisher<UserAssociation>> userAssociationPublisherFunction
                    = Mono::just;
            discordIds.forEach(id -> {
                UserAssociationKey key = UserAssociationKey
                        .builder()
                        .tournamentId(tournamentId)
                        .discordId(id)
                        .build();
                UserAssociation userAssociation = UserAssociation.builder()
                        .userAssociationKey(key)
                        .teamId("id-" + id)
                        .serverId(serverId)
                        .build();
                uaList.add(userAssociation);
                when(userAssociationDaoMock.findById(
                        UserAssociationKey.builder()
                                .discordId(id)
                                .tournamentId(tournamentId)
                                .build()))
                        .thenReturn(Mono.just(userAssociation));
                when(userAssociationDaoMock.save(userAssociation))
                        .thenReturn(Mono.just(userAssociation));
            });
            StepVerifier
                    .create(userAssociationService
                    .swapUserAssociationBetweenTeamAndTentative(
                            discordIds,
                            tournamentId,
                            userAssociationDefault,
                            userAssociationPublisherFunction
                    ))
                    .expectNext(uaList)
                    .verifyComplete();
        }

        @Test
        @DisplayName("Swap from Team to Tentative when user does not have an association")
        void test_swapUserAssociationBetweenTeamAndTentative_ifUsersDoNotExistInOtherTeams() {
            Set<Integer> discordIds = Set.of(1, 2, 3);
            Integer serverId = 1;
            String tqId = "tq-123";
            TournamentId tournamentId = TournamentId.builder()
                    .tournamentName("awesome_sauce")
                    .tournamentDay("1")
                    .build();
            List<UserAssociation> uaList = new ArrayList<>();
            UserAssociation userAssociationDefault = UserAssociation.builder()
                    .userAssociationKey(UserAssociationKey
                            .builder()
                            .tournamentId(tournamentId)
                            .build())
                    .tentativeId(tqId)
                    .serverId(serverId)
                    .build();
            Function<UserAssociation, Publisher<UserAssociation>> userAssociationPublisherFunction
                    = Mono::just;
            discordIds.forEach(id -> {
                uaList.add(userAssociationDefault);
                when(userAssociationDaoMock.findById(
                        UserAssociationKey.builder()
                                .discordId(id)
                                .tournamentId(tournamentId)
                                .build()))
                        .thenReturn(Mono.empty());
                when(userAssociationDaoMock.save(userAssociationDefault))
                        .thenReturn(Mono.just(userAssociationDefault));
            });
            StepVerifier
                    .create(userAssociationService
                            .swapUserAssociationBetweenTeamAndTentative(
                                    discordIds,
                                    tournamentId,
                                    userAssociationDefault,
                                    userAssociationPublisherFunction
                            ))
                    .expectNext(uaList)
                    .verifyComplete();
        }
    }

}
