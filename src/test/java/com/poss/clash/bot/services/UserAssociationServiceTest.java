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
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

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

}
