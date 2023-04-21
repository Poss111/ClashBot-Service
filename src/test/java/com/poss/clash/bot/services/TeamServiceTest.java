package com.poss.clash.bot.services;

import com.poss.clash.bot.ClashBotTestingConfig;
import com.poss.clash.bot.daos.TeamDao;
import com.poss.clash.bot.daos.models.BasePlayerRecord;
import com.poss.clash.bot.daos.models.ClashTeam;
import com.poss.clash.bot.daos.models.TeamId;
import com.poss.clash.bot.daos.models.TournamentId;
import com.poss.clash.bot.openapi.model.Event;
import com.poss.clash.bot.openapi.model.Role;
import com.poss.clash.bot.openapi.model.Team;
import com.poss.clash.bot.source.TeamSource;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.publisher.PublisherProbe;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@Import(ClashBotTestingConfig.class)
public class TeamServiceTest {

    @InjectMocks
    TeamService teamService;

    @Mock
    TeamDao teamDao;

    @Mock
    IdUtils idUtils;

    @Mock
    TeamSource teamSource;

    @Spy
    TeamMapper teamMapper = Mappers.getMapper(TeamMapper.class);

    @Autowired
    EasyRandom easyRandom;

    @Captor
    private ArgumentCaptor<Team> teamEventArgumentCaptor;

    @Captor
    private ArgumentCaptor<String> causedByCaptor;

    @Nested
    @DisplayName("Create")
    class Create {

        @Test
        @DisplayName("When a Team is created, a user association should be created as well.")
        void test_createClashTeam() {
            String clashTeamId = "ct-12345";
            String discordId = "1";
            Map<Role, BasePlayerRecord> positionDetailMap = Map.of(Role.TOP, BasePlayerRecord.builder().discordId(discordId).build());
            String serverId = "1234";
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
        }

    }

    @Nested
    @DisplayName("Query")
    class Query {

        @Test
        @DisplayName("Find All - When no arguments are passed")
        void test_retrieveTeamBasedOnCriteria_shouldQueryForAll() {
            List<ClashTeam> clashTeams = List.of(
                    easyRandom.nextObject(ClashTeam.class),
                    easyRandom.nextObject(ClashTeam.class),
                    easyRandom.nextObject(ClashTeam.class)
            );

            when(teamDao.findAll())
                    .thenReturn(Mono.just(clashTeams)
                            .flatMapMany(Flux::fromIterable));

            StepVerifier
                    .create(teamService.retrieveTeamBasedOnCriteria(null, null, null, null))
                    .expectNext(clashTeams.get(0))
                    .expectNext(clashTeams.get(1))
                    .expectNext(clashTeams.get(2))
                    .verifyComplete();

            verify(teamDao, times(1))
                    .findAll();
        }

        @Test
        @DisplayName("Find All - When empty arguments are passed")
        void test_retrieveTeamBasedOnCriteria_shouldQueryForAll_empty() {
            List<ClashTeam> clashTeams = List.of(
                    easyRandom.nextObject(ClashTeam.class),
                    easyRandom.nextObject(ClashTeam.class),
                    easyRandom.nextObject(ClashTeam.class)
            );

            when(teamDao.findAll())
                    .thenReturn(Mono.just(clashTeams)
                            .flatMapMany(Flux::fromIterable));

            StepVerifier
                    .create(teamService.retrieveTeamBasedOnCriteria("", "", "", ""))
                    .expectNext(clashTeams.get(0))
                    .expectNext(clashTeams.get(1))
                    .expectNext(clashTeams.get(2))
                    .verifyComplete();

            verify(teamDao, times(1))
                    .findAll();
        }

        @Test
        @DisplayName("Find by User's Discord Id - When a User's Discord Id is passed")
        void test_retrieveTeamBasedOnCriteria_shouldQueryForAllTeamsIncludingADiscordId() {
            String discordId = "1";
            List<ClashTeam> clashTeams = List.of(
                    easyRandom.nextObject(ClashTeam.class),
                    easyRandom.nextObject(ClashTeam.class),
                    easyRandom.nextObject(ClashTeam.class)
            );

            when(teamDao.findAllTeamsThatUserBelongsTo(discordId))
                    .thenReturn(Mono.just(clashTeams)
                            .flatMapMany(Flux::fromIterable));
            StepVerifier
                    .create(teamService.retrieveTeamBasedOnCriteria(discordId, null, null, null))
                    .expectNext(clashTeams.get(0))
                    .expectNext(clashTeams.get(1))
                    .expectNext(clashTeams.get(2))
                    .verifyComplete();

            verify(teamDao, times(1))
                    .findAllTeamsThatUserBelongsTo(discordId);
        }

        @Test
        @DisplayName("Find By Server id - When a Server Id is passed")
        void test_retrieveTeamBasedOnCriteria_shouldQueryForAllTeamsIncludingAServerId() {
            String serverId = "1234";
            List<ClashTeam> clashTeams = List.of(
                    easyRandom.nextObject(ClashTeam.class),
                    easyRandom.nextObject(ClashTeam.class),
                    easyRandom.nextObject(ClashTeam.class)
            );

            when(teamDao.findAllByServerId(serverId))
                    .thenReturn(Mono.just(clashTeams)
                            .flatMapMany(Flux::fromIterable));
            StepVerifier
                    .create(teamService.retrieveTeamBasedOnCriteria(null, serverId, null, null))
                    .expectNext(clashTeams.get(0))
                    .expectNext(clashTeams.get(1))
                    .expectNext(clashTeams.get(2))
                    .verifyComplete();

            verify(teamDao, times(1))
                    .findAllByServerId(serverId);
        }

        @Test
        @DisplayName("Find By Tournament Name - When a Tournament Name is passed")
        void test_retrieveTeamBasedOnCriteria_shouldQueryForAllTeamsIncludingATournamentName() {
            String tournamentName = "awesome-sauce";
            List<ClashTeam> clashTeams = List.of(
                    easyRandom.nextObject(ClashTeam.class),
                    easyRandom.nextObject(ClashTeam.class),
                    easyRandom.nextObject(ClashTeam.class)
            );

            when(teamDao.findAllByTeamId_TournamentId_TournamentName(tournamentName))
                    .thenReturn(Mono.just(clashTeams)
                            .flatMapMany(Flux::fromIterable));

            StepVerifier
                    .create(teamService.retrieveTeamBasedOnCriteria(null, null, tournamentName, null))
                    .expectNext(clashTeams.get(0))
                    .expectNext(clashTeams.get(1))
                    .expectNext(clashTeams.get(2))
                    .verifyComplete();

            verify(teamDao, times(1))
                    .findAllByTeamId_TournamentId_TournamentName(tournamentName);
        }

        @Test
        @DisplayName("Find By Tournament Day - When a Tournament Day is passed")
        void test_retrieveTeamBasedOnCriteria_shouldQueryForAllTeamsIncludingATournamentDay() {
            String tournamentDay = "1";
            List<ClashTeam> clashTeams = List.of(
                    easyRandom.nextObject(ClashTeam.class),
                    easyRandom.nextObject(ClashTeam.class),
                    easyRandom.nextObject(ClashTeam.class)
            );

            when(teamDao.findAllByTeamId_TournamentId_TournamentDay(tournamentDay))
                    .thenReturn(Mono.just(clashTeams)
                            .flatMapMany(Flux::fromIterable));

            StepVerifier
                    .create(teamService.retrieveTeamBasedOnCriteria(null, null, null, tournamentDay))
                    .expectNext(clashTeams.get(0))
                    .expectNext(clashTeams.get(1))
                    .expectNext(clashTeams.get(2))
                    .verifyComplete();

            verify(teamDao, times(1))
                    .findAllByTeamId_TournamentId_TournamentDay(tournamentDay);
        }

        @Test
        @DisplayName("Find By Server Id, Tournament Name and Day")
        void test6() {
            String serverId = "1234";
            String tournamentName = "awesome_sauce";
            String tournamentDay = "1";
            List<ClashTeam> clashTeams = List.of(
                    easyRandom.nextObject(ClashTeam.class),
                    easyRandom.nextObject(ClashTeam.class),
                    easyRandom.nextObject(ClashTeam.class)
            );

            when(teamDao.findAllByServerId_AndTeamId_TournamentId_TournamentName_AndTeamId_TournamentId_TournamentDay(
                    serverId,
                    tournamentName,
                    tournamentDay
            )).thenReturn(Mono.just(clashTeams)
                    .flatMapMany(Flux::fromIterable));

            StepVerifier
                    .create(teamService.retrieveTeamBasedOnCriteria(null, serverId, tournamentName, tournamentDay))
                    .expectNext(clashTeams.get(0))
                    .expectNext(clashTeams.get(1))
                    .expectNext(clashTeams.get(2))
                    .verifyComplete();

            verify(teamDao, times(1))
                    .findAllByServerId_AndTeamId_TournamentId_TournamentName_AndTeamId_TournamentId_TournamentDay(
                            serverId,
                            tournamentName,
                            tournamentDay
                    );
        }

        @Test
        @DisplayName("Find By Tournament Name and Day")
        void test7() {
            String tournamentName = "awesome_sauce";
            String tournamentDay = "1";
            List<ClashTeam> clashTeams = List.of(
                    easyRandom.nextObject(ClashTeam.class),
                    easyRandom.nextObject(ClashTeam.class),
                    easyRandom.nextObject(ClashTeam.class)
            );

            when(teamDao.findAllByTeamId_TournamentId_TournamentName_AndTeamId_TournamentId_TournamentName(
                    tournamentName,
                    tournamentDay
            )).thenReturn(Mono.just(clashTeams)
                    .flatMapMany(Flux::fromIterable));

            StepVerifier
                    .create(teamService.retrieveTeamBasedOnCriteria(null, null, tournamentName, tournamentDay))
                    .expectNext(clashTeams.get(0))
                    .expectNext(clashTeams.get(1))
                    .expectNext(clashTeams.get(2))
                    .verifyComplete();

            verify(teamDao, times(1))
                    .findAllByTeamId_TournamentId_TournamentName_AndTeamId_TournamentId_TournamentName(
                            tournamentName,
                            tournamentDay
                    );
        }

        @Test
        @DisplayName("Find By Server Id and Tournament Name")
        void test8() {
            String serverId = "1234";
            String tournamentName = "awesome_sauce";
            List<ClashTeam> clashTeams = List.of(
                    easyRandom.nextObject(ClashTeam.class),
                    easyRandom.nextObject(ClashTeam.class),
                    easyRandom.nextObject(ClashTeam.class)
            );

            when(teamDao.findAllByServerId_AndTeamId_TournamentId_TournamentName(
                    serverId,
                    tournamentName
            )).thenReturn(Mono.just(clashTeams)
                    .flatMapMany(Flux::fromIterable));

            StepVerifier
                    .create(teamService.retrieveTeamBasedOnCriteria(null, serverId, tournamentName, null))
                    .expectNext(clashTeams.get(0))
                    .expectNext(clashTeams.get(1))
                    .expectNext(clashTeams.get(2))
                    .verifyComplete();

            verify(teamDao, times(1))
                    .findAllByServerId_AndTeamId_TournamentId_TournamentName(
                            serverId,
                            tournamentName
                    );
        }

        @Test
        @DisplayName("Find By Server Id and Tournament Day")
        void test9() {
            String serverId = "1234";
            String tournamentDay = "1";
            List<ClashTeam> clashTeams = List.of(
                    easyRandom.nextObject(ClashTeam.class),
                    easyRandom.nextObject(ClashTeam.class),
                    easyRandom.nextObject(ClashTeam.class)
            );

            when(teamDao.findAllByServerId_AndTeamId_TournamentId_TournamentDay(
                    serverId,
                    tournamentDay
            )).thenReturn(Mono.just(clashTeams)
                    .flatMapMany(Flux::fromIterable));

            StepVerifier
                    .create(teamService.retrieveTeamBasedOnCriteria(null, serverId, null, tournamentDay))
                    .expectNext(clashTeams.get(0))
                    .expectNext(clashTeams.get(1))
                    .expectNext(clashTeams.get(2))
                    .verifyComplete();

            verify(teamDao, times(1))
                    .findAllByServerId_AndTeamId_TournamentId_TournamentDay(
                            serverId,
                            tournamentDay
                    );
        }
    }

    @Nested
    @DisplayName("Update")
    class Update {

        @Test
        @DisplayName("Update Team name")
        void test() {
            String clashTeamId = "ct-1234";
            String newTeamName = "New Name";

            ClashTeam clashTeam = easyRandom.nextObject(ClashTeam.class);
            clashTeam.setTeamName(newTeamName);

            PublisherProbe<Void> publisherProbe = PublisherProbe.empty();
            when(teamDao.findByTeamId_Id(clashTeamId))
                    .thenReturn(Mono.just(clashTeam));
            when(teamDao.updateTeamName(clashTeamId, newTeamName))
                    .thenReturn(publisherProbe.mono());
            when(teamSource.sendTeamUpdateEvent(teamEventArgumentCaptor.capture(), causedByCaptor.capture()))
                    .thenReturn(Mono.just(Event.builder().build()));

            StepVerifier
                    .create(teamService.updateTeamName(clashTeamId, newTeamName))
                    .expectNext(clashTeam)
                    .verifyComplete();

            verify(teamDao, times(1))
                    .findByTeamId_Id(clashTeamId);
            verify(teamDao, times(1))
                    .updateTeamName(clashTeamId, newTeamName);
            verify(teamSource, times(1))
                    .sendTeamUpdateEvent(any(Team.class), anyString());

            assertAll(() -> {
                assertEquals(1, teamEventArgumentCaptor.getAllValues().size());
                assertEquals(1, causedByCaptor.getAllValues().size());
                Team updatedTeamEvent = teamEventArgumentCaptor.getAllValues().get(0);
                assertEquals(clashTeam.getServerId(), updatedTeamEvent.getServerId());
                assertEquals(teamMapper.clashTeamToTeam(clashTeam), updatedTeamEvent);
                assertEquals("0", causedByCaptor.getAllValues().get(0));
            });
        }

        @Test
        @DisplayName("Remove User")
        void test2() {
            String clashTeamId = "ct-1234";
            String discordId = easyRandom.nextObject(String.class);

            HashMap<Role, BasePlayerRecord> positions = new HashMap<>();
            positions.put(Role.TOP, BasePlayerRecord.builder()
                    .discordId(discordId)
                    .build());
            positions.put(Role.BOT, BasePlayerRecord.builder()
                    .discordId("2")
                    .build());
            ClashTeam clashTeam = easyRandom.nextObject(ClashTeam.class);
            clashTeam.setPositions(positions);

            HashMap<Role, BasePlayerRecord> expectedPositions = new HashMap<>();
            expectedPositions.put(Role.BOT, BasePlayerRecord.builder()
                    .discordId("2")
                    .build());
            ClashTeam updatedClashTeam = teamMapper.clone(clashTeam);
            updatedClashTeam.setPositions(expectedPositions);

            when(teamDao.findByTeamId_Id(clashTeamId))
                    .thenReturn(Mono.just(clashTeam));
            when(teamDao.save(updatedClashTeam))
                    .thenReturn(Mono.just(updatedClashTeam));

            StepVerifier
                    .create(teamService.removeUserFromTeam(clashTeamId, discordId))
                    .expectNext(updatedClashTeam)
                    .verifyComplete();

            verify(teamDao, times(1))
                    .findByTeamId_Id(clashTeamId);
        }
    }

}
