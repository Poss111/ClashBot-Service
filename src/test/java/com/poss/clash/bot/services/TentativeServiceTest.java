package com.poss.clash.bot.services;

import com.poss.clash.bot.ClashBotTestingConfig;
import com.poss.clash.bot.daos.TentativeDao;
import com.poss.clash.bot.daos.models.*;
import com.poss.clash.bot.openapi.model.BaseTournament;
import com.poss.clash.bot.openapi.model.Tentative;
import com.poss.clash.bot.openapi.model.TentativePlayer;
import com.poss.clash.bot.utils.IdUtils;
import com.poss.clash.bot.utils.TentativeMapper;
import com.poss.clash.bot.utils.UserAssociationMapper;
import org.apache.commons.lang3.StringUtils;
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
import org.springframework.data.domain.Example;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@Import(ClashBotTestingConfig.class)
class TentativeServiceTest {

    @InjectMocks
    TentativeService tentativeService;

    @Mock
    TentativeDao tentativeDao;

    @Mock
    TeamService teamService;

    @Mock
    UserAssociationService userAssociationService;

    @Mock
    IdUtils idUtils;

    @Spy
    TentativeMapper tentativeMapper = Mappers.getMapper(TentativeMapper.class);

    @Spy
    UserAssociationMapper userAssociationMapper = Mappers.getMapper(UserAssociationMapper.class);

    @Autowired
    EasyRandom easyRandom;

    @Test
    void test_retrieveTentativeQueues_retrieveAllQueues() {
        TentativeQueue tentativeQueue = easyRandom.nextObject(TentativeQueue.class);
        TentativeQueue tentativeQueue2 = easyRandom.nextObject(TentativeQueue.class);
        TentativeQueue tentativeQueue3 = easyRandom.nextObject(TentativeQueue.class);
        TentativeQueue tentativeQueue4 = easyRandom.nextObject(TentativeQueue.class);

        Tentative tentative = tentativeQueueToTentative(tentativeQueue);
        Tentative tentative1 = tentativeQueueToTentative(tentativeQueue2);
        Tentative tentative2 = tentativeQueueToTentative(tentativeQueue3);
        Tentative tentative3 = tentativeQueueToTentative(tentativeQueue4);


        Flux<TentativeQueue> dbResponse = Flux.just(tentativeQueue, tentativeQueue2, tentativeQueue3, tentativeQueue4);

        when(tentativeDao.findAll())
                .thenReturn(dbResponse);

        StepVerifier
                .create(tentativeService.retrieveTentativeQueues())
                .expectNext(tentative)
                .expectNext(tentative1)
                .expectNext(tentative2)
                .expectNext(tentative3)
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("retrieveTentativeQueuesByExample - should retrieve Tentative Queues based on the tournament name passed.")
    void test_retrieveTentativeQueuesByTournament_byTournamentName() {
        String tournamentName = "tournament1";
        TentativeQueue tentativeQueue = easyRandom.nextObject(TentativeQueue.class);
        TentativeQueue tentativeQueue2 = easyRandom.nextObject(TentativeQueue.class);
        TentativeQueue tentativeQueue3 = easyRandom.nextObject(TentativeQueue.class);
        TentativeQueue tentativeQueue4 = easyRandom.nextObject(TentativeQueue.class);

        Tentative tentative = tentativeQueueToTentative(tentativeQueue);
        Tentative tentative1 = tentativeQueueToTentative(tentativeQueue2);
        Tentative tentative2 = tentativeQueueToTentative(tentativeQueue3);
        Tentative tentative3 = tentativeQueueToTentative(tentativeQueue4);


        Flux<TentativeQueue> dbResponse = Flux.just(tentativeQueue, tentativeQueue2, tentativeQueue3, tentativeQueue4);

        TentativeQueue example = TentativeQueue.builder()
                .tentativeId(TentativeId.builder()
                                     .tournamentId(
                                             TournamentId.builder()
                                                     .tournamentName(tournamentName)
                                                     .build())
                                     .build())
                .build();

        when(tentativeDao.findAll(Example.of(example)))
                .thenReturn(dbResponse);

        StepVerifier
                .create(tentativeService.retrieveTentativeQueues(null, tournamentName, null))
                .expectNext(tentative)
                .expectNext(tentative1)
                .expectNext(tentative2)
                .expectNext(tentative3)
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("retrieveTentativeQueuesByExample - should retrieve Tentative Queues based on the tournament day passed.")
    void test_retrieveTentativeQueuesByTournament_byTournamentDay() {
        String tournamentDay = "1";
        TentativeQueue tentativeQueue = easyRandom.nextObject(TentativeQueue.class);
        TentativeQueue tentativeQueue2 = easyRandom.nextObject(TentativeQueue.class);
        TentativeQueue tentativeQueue3 = easyRandom.nextObject(TentativeQueue.class);
        TentativeQueue tentativeQueue4 = easyRandom.nextObject(TentativeQueue.class);

        Tentative tentative = tentativeQueueToTentative(tentativeQueue);
        Tentative tentative1 = tentativeQueueToTentative(tentativeQueue2);
        Tentative tentative2 = tentativeQueueToTentative(tentativeQueue3);
        Tentative tentative3 = tentativeQueueToTentative(tentativeQueue4);


        Flux<TentativeQueue> dbResponse = Flux.just(tentativeQueue, tentativeQueue2, tentativeQueue3, tentativeQueue4);

        TentativeQueue example = TentativeQueue.builder()
                .tentativeId(TentativeId.builder()
                                     .tournamentId(
                                             TournamentId.builder()
                                                     .tournamentDay(tournamentDay)
                                                     .build())
                                     .build())
                .build();

        when(tentativeDao.findAll(Example.of(example)))
                .thenReturn(dbResponse);

        StepVerifier
                .create(tentativeService.retrieveTentativeQueues(null, null, tournamentDay))
                .expectNext(tentative)
                .expectNext(tentative1)
                .expectNext(tentative2)
                .expectNext(tentative3)
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("retrieveTentativeQueuesByExample - should retrieve Tentative Queues based on the tournament details passed.")
    void test_retrieveTentativeQueuesByTournament_byTournamentDetails() {
        String tournamentName = "tournament1";
        String tournamentDay = "1";
        TentativeQueue tentativeQueue = easyRandom.nextObject(TentativeQueue.class);
        TentativeQueue tentativeQueue2 = easyRandom.nextObject(TentativeQueue.class);
        TentativeQueue tentativeQueue3 = easyRandom.nextObject(TentativeQueue.class);
        TentativeQueue tentativeQueue4 = easyRandom.nextObject(TentativeQueue.class);

        Tentative tentative = tentativeQueueToTentative(tentativeQueue);
        Tentative tentative1 = tentativeQueueToTentative(tentativeQueue2);
        Tentative tentative2 = tentativeQueueToTentative(tentativeQueue3);
        Tentative tentative3 = tentativeQueueToTentative(tentativeQueue4);


        Flux<TentativeQueue> dbResponse = Flux.just(tentativeQueue, tentativeQueue2, tentativeQueue3, tentativeQueue4);

        TentativeQueue example = TentativeQueue.builder()
                .tentativeId(TentativeId.builder()
                                     .tournamentId(
                                             TournamentId.builder()
                                                     .tournamentName(tournamentName)
                                                     .tournamentDay(tournamentDay)
                                                     .build())
                                     .build())
                .build();

        when(tentativeDao.findAll(Example.of(example)))
                .thenReturn(dbResponse);

        StepVerifier
                .create(tentativeService.retrieveTentativeQueues(null, tournamentName, tournamentDay))
                .expectNext(tentative)
                .expectNext(tentative1)
                .expectNext(tentative2)
                .expectNext(tentative3)
                .expectComplete()
                .verify();
    }

    @Nested
    @DisplayName("Save")
    class Save {

        @Test
        @DisplayName("Should be able to save tentative Queues and map them back to Tentative")
        void test_mapBackToTentative() {
            String mockTentativeId = "tq-123asdf";
            TentativeQueue tentativeQueue = easyRandom.nextObject(TentativeQueue.class);
            tentativeQueue.getTentativeId().setTentativeId(null);
            Tentative expectedMappedTentative = Tentative.builder()
                    .tentativePlayers(tentativeQueue.getDiscordIds().stream()
                                              .map(id -> TentativePlayer.builder().discordId(id).build()).collect(
                                    Collectors.toList()))
                    .serverId(tentativeQueue.getTentativeId().getServerId())
                    .tournamentDetails(
                            BaseTournament.builder()
                                    .tournamentName(
                                            tentativeQueue.getTentativeId().getTournamentId().getTournamentName())
                                    .tournamentDay(tentativeQueue.getTentativeId().getTournamentId().getTournamentDay())
                                    .build())
                    .build();

            ClashTeam clashTeam = easyRandom.nextObject(ClashTeam.class);
            clashTeam.getTeamId().setTournamentId(tentativeQueue.getTentativeId().getTournamentId());
            when(idUtils.retrieveNewTentativeQueueId()).thenReturn(mockTentativeId);
            List<UserAssociation> listOfUserAssociations = new ArrayList<>();
            tentativeQueue.getDiscordIds()
                            .forEach((id) -> {
                                UserAssociation userAssociation = UserAssociation.builder()
                                        .userAssociationKey(UserAssociationKey.builder()
                                                .tournamentId(tentativeQueue.getTentativeId().getTournamentId())
                                                .discordId(id)
                                                .build())
                                        .teamId(clashTeam.getTeamId().getId())
                                        .serverId(tentativeQueue.getTentativeId().getServerId())
                                        .build();
                                when(userAssociationService.retrieveUsersTeamOrTentativeQueueForTournament(id,
                                        tentativeQueue.getTentativeId().getTournamentId().getTournamentName(),
                                        tentativeQueue.getTentativeId().getTournamentId().getTournamentDay()))
                                        .thenReturn(Mono.just(userAssociation));
                                listOfUserAssociations.add(userAssociation);
                            }
                        );
            tentativeQueue.getDiscordIds()
                    .forEach((id) -> when(teamService.removeUserFromTeam(clashTeam.getTeamId(), id))
                                            .thenReturn(Mono.just(clashTeam)));
            listOfUserAssociations.forEach(association -> when(userAssociationService.save(association))
                    .thenReturn(Mono.just(association)));
            when(tentativeDao.save(tentativeQueue))
                    .thenReturn(Mono.just(tentativeQueue));


            StepVerifier
                    .create(tentativeService.createTentativeQueue(tentativeQueue))
                    .expectNextMatches(tentative ->
                            StringUtils.isNotBlank(tentative.getId())
                            && expectedMappedTentative.getTentativePlayers().equals(tentative.getTentativePlayers())
                            && expectedMappedTentative.getServerId().equals(tentative.getServerId())
                            && expectedMappedTentative.getTournamentDetails().equals(tentative.getTournamentDetails())
                    )
                    .expectComplete()
                    .verify();
        }

        @Test
        @DisplayName("If User does not belong to anything for a Tournament, they should have a user association created for them and they should be able to save tentative Queues and map them back to Tentative")
        void test_mapBackToTentative_ifUserDoesNotHaveAnAssociation_createAnAssociation() {
            String mockTentativeId = "tq-123asdf";
            TentativeQueue tentativeQueue = easyRandom.nextObject(TentativeQueue.class);
            tentativeQueue.getTentativeId().setTentativeId(null);
            Tentative expectedMappedTentative = Tentative.builder()
                    .tentativePlayers(tentativeQueue.getDiscordIds().stream()
                            .map(id -> TentativePlayer.builder().discordId(id).build()).collect(Collectors.toList()))
                    .serverId(tentativeQueue.getTentativeId().getServerId())
                    .tournamentDetails(
                            BaseTournament.builder()
                                    .tournamentName(
                                            tentativeQueue.getTentativeId().getTournamentId().getTournamentName())
                                    .tournamentDay(tentativeQueue.getTentativeId().getTournamentId().getTournamentDay())
                                    .build())
                    .build();

            ClashTeam clashTeam = easyRandom.nextObject(ClashTeam.class);
            clashTeam.getTeamId().setTournamentId(tentativeQueue.getTentativeId().getTournamentId());
            when(idUtils.retrieveNewTentativeQueueId()).thenReturn(mockTentativeId);
            tentativeQueue.getDiscordIds()
                            .forEach((id) -> {
                                when(userAssociationService.retrieveUsersTeamOrTentativeQueueForTournament(id,
                                        tentativeQueue.getTentativeId().getTournamentId().getTournamentName(),
                                        tentativeQueue.getTentativeId().getTournamentId().getTournamentDay()))
                                        .thenReturn(Mono.empty());
                            }
                        );
            tentativeQueue.getDiscordIds()
                    .forEach((id) -> when(teamService.removeUserFromTeam(clashTeam.getTeamId(), id))
                                            .thenReturn(Mono.just(clashTeam)));

            List<UserAssociation> listOfUserAssociations = tentativeQueue.getDiscordIds()
                            .stream()
                                    .map(id -> UserAssociation.builder().userAssociationKey(UserAssociationKey.builder()
                                                    .discordId(id)
                                                    .tournamentId(tentativeQueue.getTentativeId().getTournamentId())
                                                    .build())
                                            .tentativeId(mockTentativeId)
                                            .teamId(null)
                                            .serverId(tentativeQueue.getTentativeId().getServerId())
                                            .build())
                                            .collect(Collectors.toList());

            listOfUserAssociations.forEach(association -> when(userAssociationService.save(association))
                    .thenReturn(Mono.just(association)));
            when(tentativeDao.save(tentativeQueue))
                    .thenReturn(Mono.just(tentativeQueue));

            StepVerifier
                    .create(tentativeService.createTentativeQueue(tentativeQueue))
                    .expectNextMatches(tentative ->
                            StringUtils.isNotBlank(tentative.getId())
                            && expectedMappedTentative.getTentativePlayers().equals(tentative.getTentativePlayers())
                            && expectedMappedTentative.getServerId().equals(tentative.getServerId())
                            && expectedMappedTentative.getTournamentDetails().equals(tentative.getTournamentDetails())
                    )
                    .expectComplete()
                    .verify();
        }

    }

    @Nested
    @DisplayName("Swap from Team to Tentative")
    class SwapFromTeamToTentative {

        @Test
        @DisplayName("Swapping from Team to Tentative - association has a Team Id so it should be nulled and tentativeId should be populated")
        void test_swapFromTeamToTentative_associationHasTeamIdSoItShouldBeSwapped() {
            String tqId = "tq-123qwer";
            Integer discordId = 1;
            Integer serverId = 1;
            String teamId = "ct-123asdf";
            UserAssociation userAssociationWithTeam = UserAssociation.builder()
                    .userAssociationKey(UserAssociationKey
                            .builder()
                            .tournamentId(easyRandom.nextObject(TournamentId.class))
                            .discordId(discordId)
                            .build())
                    .teamId(teamId)
                    .serverId(serverId)
                    .build();
            UserAssociation expectedUserAssociation = userAssociationMapper.clone(userAssociationWithTeam);
            expectedUserAssociation.setTentativeId(tqId);
            expectedUserAssociation.setTeamId(null);

            when(teamService.removeUserFromTeam(TeamId.builder()
                            .id(teamId)
                            .tournamentId(userAssociationWithTeam.getUserAssociationKey()
                                    .getTournamentId())
                    .build(), discordId))
                    .thenReturn(Mono.just(ClashTeam.builder().build()));
            StepVerifier
                    .create(tentativeService.swapFromTeamToTentative(tqId)
                            .apply(userAssociationWithTeam))
                    .expectNext(expectedUserAssociation)
                    .verifyComplete();
        }

        @Test
        @DisplayName("Swapping from Team to Tentative - association does not have a Team Id so it should pass through")
        void test_swapFromTeamToTentative_theAssociationShouldPassThrough() {
            String tqId = "tq-123qwer";
            Integer discordId = 1;
            Integer serverId = 1;
            String teamId = "ct-123asdf";
            UserAssociation userAssociationWithTeam = UserAssociation.builder()
                    .userAssociationKey(UserAssociationKey
                            .builder()
                            .tournamentId(easyRandom.nextObject(TournamentId.class))
                            .discordId(discordId)
                            .build())
                    .tentativeId(tqId)
                    .serverId(serverId)
                    .build();
            UserAssociation expectedUserAssociation = userAssociationMapper.clone(userAssociationWithTeam);
            expectedUserAssociation.setTentativeId(tqId);
            expectedUserAssociation.setTeamId(null);

            StepVerifier
                    .create(tentativeService.swapFromTeamToTentative(tqId)
                            .apply(userAssociationWithTeam))
                    .expectNext(expectedUserAssociation)
                    .verifyComplete();
            verify(teamService, times(0))
                    .removeUserFromTeam(TeamId.builder()
                            .tournamentId(userAssociationWithTeam.getUserAssociationKey().getTournamentId())
                            .id(teamId)
                            .build(), discordId);
        }

    }

    private Tentative tentativeQueueToTentative(TentativeQueue tentativeQueue) {
        return Tentative.builder()
                .id(tentativeQueue.getTentativeId().getTentativeId())
                .tentativePlayers(tentativeQueue.getDiscordIds().stream()
                                          .map(id -> TentativePlayer.builder().discordId(id).build()).collect(
                                Collectors.toList()))
                .serverId(tentativeQueue.getTentativeId().getServerId())
                .tournamentDetails(
                        BaseTournament.builder()
                                .tournamentName(tentativeQueue.getTentativeId().getTournamentId().getTournamentName())
                                .tournamentDay(tentativeQueue.getTentativeId().getTournamentId().getTournamentDay())
                                .build())
                .build();
    }

}
