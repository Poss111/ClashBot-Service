package com.poss.clash.bot.services;

import com.poss.clash.bot.ClashBotTestingConfig;
import com.poss.clash.bot.daos.TeamDao;
import com.poss.clash.bot.daos.models.BasePlayerRecord;
import com.poss.clash.bot.daos.models.ClashTeam;
import com.poss.clash.bot.daos.models.TeamId;
import com.poss.clash.bot.daos.models.TournamentId;
import com.poss.clash.bot.openapi.model.Role;
import com.poss.clash.bot.utils.IdUtils;
import com.poss.clash.bot.utils.TeamMapper;
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

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@Import(ClashBotTestingConfig.class)
public class TeamServiceTest {

    @InjectMocks
    TeamService teamService;

    @Mock
    TeamDao teamDao;

    @Mock
    IdUtils idUtils;

    @Spy
    TeamMapper teamMapper = Mappers.getMapper(TeamMapper.class);

    @Autowired
    EasyRandom easyRandom;

    @Captor
    private ArgumentCaptor<TournamentId> tournamentIdCaptor;

    @Nested
    @DisplayName("createClashTeam")
    class Create {

        @Test
        @DisplayName("When a Team is created, a user association should be created as well.")
        void test_createClashTeam() {
            String clashTeamId = "ct-12345";
            int discordId = 1;
            Map<Role, BasePlayerRecord> positionDetailMap = Map.of(Role.TOP, BasePlayerRecord.builder().discordId(discordId).build());
            int serverId = 1234;
            TournamentId tournamentId = easyRandom.nextObject(TournamentId.class);
            ClashTeam teamToPersist = ClashTeam.builder()
                    .teamName("RandomTeamName")
                    .serverId(serverId)
                    .positions(positionDetailMap)
                    .teamId(TeamId
                            .builder()
                            .tournamentId(tournamentId)
                            .build())
                    .build();
            ClashTeam teamAfterSave = ClashTeam.builder()
                    .teamName("RandomTeamName")
                    .serverId(serverId)
                    .positions(positionDetailMap)
                    .teamId(TeamId
                            .builder()
                            .id(clashTeamId)
                            .tournamentId(tournamentId)
                            .build())
                    .build();
            when(idUtils.retrieveNewClashTeamId())
                    .thenReturn(clashTeamId);
            when(teamDao.save(teamAfterSave))
                    .thenReturn(Mono.just(teamAfterSave));


            StepVerifier
                    .create(teamService.createClashTeam(teamToPersist))
                    .expectNext(teamAfterSave)
                    .verifyComplete();

            assertEquals(tournamentId, tournamentIdCaptor.getValue());
        }

    }

}
