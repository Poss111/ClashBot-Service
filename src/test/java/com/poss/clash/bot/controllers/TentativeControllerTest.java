//package com.poss.clash.bot.controllers;
//
//import com.poss.clash.bot.ClashBotTestingConfig;
//import com.poss.clash.bot.daos.models.TentativeId;
//import com.poss.clash.bot.daos.models.TentativeQueue;
//import com.poss.clash.bot.daos.models.TournamentId;
//import com.poss.clash.bot.exceptions.ClashBotControllerException;
//import com.poss.clash.bot.openapi.model.*;
//import com.poss.clash.bot.services.ArchivedService;
//import com.poss.clash.bot.services.TentativeService;
//import com.poss.clash.bot.services.TournamentService;
//import com.poss.clash.bot.services.UserService;
//import com.poss.clash.bot.utils.TentativeMapper;
//import com.poss.clash.bot.utils.TournamentMapper;
//import org.jeasy.random.EasyRandom;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Nested;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mapstruct.factory.Mappers;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.Spy;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Import;
//import org.springframework.http.ResponseEntity;
//import org.springframework.test.context.junit.jupiter.SpringExtension;
//import reactor.core.publisher.Flux;
//import reactor.core.publisher.Mono;
//import reactor.test.StepVerifier;
//import reactor.util.function.Tuple2;
//import reactor.util.function.Tuples;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Set;
//import java.util.stream.Collectors;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.mockito.Mockito.*;
//
//@ExtendWith({SpringExtension.class})
//@Import(ClashBotTestingConfig.class)
//class TentativeControllerTest {
//
//    @InjectMocks
//    TentativeController tentativeController;
//    @Mock
//    TournamentService tournamentService;
//    @Mock
//    ArchivedService archivedService;
//    @Mock
//    TentativeService tentativeService;
//    @Mock
//    UserService userService;
//    @Spy
//    TentativeMapper tentativeMapper = Mappers.getMapper(TentativeMapper.class);
//    @Spy
//    TournamentMapper tournamentMapper = Mappers.getMapper(TournamentMapper.class);
//    @Autowired
//    EasyRandom easyRandom;
//
//    @Nested
//    @DisplayName("Utils")
//    class TentativeControllerUtils {
//
//        @Test
//        @DisplayName("validateTentativeRequest - Should validate that list is not null")
//        void test_validateTentativeRequest_shouldThrowAnExceptionIfListIsNotNull() {
//            assertThrows(ClashBotControllerException.class, () -> tentativeController
//                    .validateTentativeRequest(TentativeRequired.builder().build()));
//        }
//
//        @Test
//        @DisplayName("validateTentativeRequest - Should validate that list is not empty")
//        void test_validateTentativeRequest_shouldThrowAnExceptionIfListIsNotEmpty() {
//            assertThrows(ClashBotControllerException.class, () -> tentativeController
//                    .validateTentativeRequest(TentativeRequired.builder().tentativePlayers(List.of()).build()));
//        }
//
//        @Test
//        @DisplayName("validateTentativeRequest - Should validate that list has discord id")
//        void test_validateTentativeRequest_shouldThrowAnExceptionIfListDoesNotHaveDiscordIds() {
//            assertThrows(ClashBotControllerException.class, () -> tentativeController
//                    .validateTentativeRequest(TentativeRequired.builder()
//                            .tentativePlayers(List.of(TentativePlayer.builder()
//                                    .build()))
//                            .build()));
//        }
//
//        @Test
//        @DisplayName("buildTupleOfTentativeAndSetOfDiscordIds - should take in a List of Tentative and build a tuple of a set of Discord Ids")
//        void test_buildTupleOfTentativeAndSetOfDiscordIds_shouldReturnATuple() {
//            Tentative tentativeOne = easyRandom.nextObject(Tentative.class);
//            Tentative tentativeTwo = easyRandom.nextObject(Tentative.class);
//            Tentative tentativeThree = easyRandom.nextObject(Tentative.class);
//            List<Tentative> listOfTentative = List.of(tentativeOne,
//                                                      tentativeTwo,
//                                                      tentativeThree);
//            Tuple2<Tentatives, Set<Integer>> tuple = tentativeController.buildTupleOfTentativesAndSetOfDiscordIds(
//                    listOfTentative);
//            Tentatives expectedTentatives = Tentatives.builder()
//                    .queues(listOfTentative)
//                    .build();
//            Set<Integer> expectedSetOfDiscordIds = tentativeOne
//                    .getTentativePlayers()
//                    .stream()
//                    .map(TentativePlayer::getDiscordId)
//                    .collect(Collectors.toSet());
//            expectedSetOfDiscordIds
//                    .addAll(tentativeTwo.getTentativePlayers()
//                                    .stream()
//                                    .map(TentativePlayer::getDiscordId)
//                                    .collect(Collectors.toSet()));
//            expectedSetOfDiscordIds
//                    .addAll(tentativeThree.getTentativePlayers().stream().map(TentativePlayer::getDiscordId)
//                                    .collect(Collectors.toSet()));
//
//            assertEquals(expectedTentatives, tuple.getT1());
//            assertEquals(expectedSetOfDiscordIds, tuple.getT2());
//        }
//
//        @Test
//        @DisplayName("populateTupleWithTentativePlayerDetails - should return a Mono containing a tuple of Tentatives to a Map of Discord Id to TentativePlayer")
//        void test_populateTupleWithTentativePlayerDetails_shouldReturnAMonoContainingATuple() {
//            Tentatives tentatives = Tentatives.builder().build();
//
//            Player playerOne = easyRandom.nextObject(Player.class);
//            Player playerTwo = easyRandom.nextObject(Player.class);
//            Player playerThree = easyRandom.nextObject(Player.class);
//
//            TentativePlayer tentativePlayerOne = TentativePlayer.builder()
//                    .discordId(playerOne.getDiscordId())
//                    .name(playerOne.getName())
//                    .champions(playerOne.getChampions())
//                    .role(playerOne.getRole())
//                    .build();
//            TentativePlayer tentativePlayerTwo = TentativePlayer.builder()
//                    .discordId(playerTwo.getDiscordId())
//                    .name(playerTwo.getName())
//                    .champions(playerTwo.getChampions())
//                    .role(playerTwo.getRole())
//                    .build();
//            TentativePlayer tentativePlayerThree = TentativePlayer.builder()
//                    .discordId(playerThree.getDiscordId())
//                    .name(playerThree.getName())
//                    .champions(playerThree.getChampions())
//                    .role(playerThree.getRole())
//                    .build();
//
//            Set<Integer> discordIds = Set.of(playerOne.getDiscordId(), playerTwo.getDiscordId(),
//                                             playerThree.getDiscordId());
//
//            HashMap<Integer, TentativePlayer> integerTentativePlayerHashMap = new HashMap<>();
//            integerTentativePlayerHashMap.put(playerOne.getDiscordId(), tentativePlayerOne);
//            integerTentativePlayerHashMap.put(playerTwo.getDiscordId(), tentativePlayerTwo);
//            integerTentativePlayerHashMap.put(playerThree.getDiscordId(), tentativePlayerThree);
//
//            when(userService.retrieveUser(playerOne.getDiscordId()))
//                    .thenReturn(Mono.just(playerOne));
//            when(userService.retrieveUser(playerTwo.getDiscordId()))
//                    .thenReturn(Mono.just(playerTwo));
//            when(userService.retrieveUser(playerThree.getDiscordId()))
//                    .thenReturn(Mono.just(playerThree));
//
//            StepVerifier
//                    .create(tentativeController.populateTupleWithTentativePlayerDetails(
//                            Tuples.of(tentatives, discordIds)))
//                    .expectNext(Tuples.of(tentatives, integerTentativePlayerHashMap))
//                    .expectComplete()
//                    .verify();
//        }
//
//        @Test
//        @DisplayName("populateTupleWithTentativePlayerDetails - if there the Player record is missing then it should map just to the Tentative Player object")
//        void test_populateTupleWithTentativePlayerDetails_ifDiscordIdDoesNotExist_shouldReturnAMonoContainingATuple() {
//            Tentatives tentatives = Tentatives.builder().build();
//
//            Player playerOne = easyRandom.nextObject(Player.class);
//            Player playerTwo = easyRandom.nextObject(Player.class);
//            Player playerThree = easyRandom.nextObject(Player.class);
//
//            TentativePlayer tentativePlayerOne = TentativePlayer.builder()
//                    .discordId(playerOne.getDiscordId())
//                    .name(playerOne.getName())
//                    .champions(playerOne.getChampions())
//                    .role(playerOne.getRole())
//                    .build();
//            TentativePlayer tentativePlayerTwo = TentativePlayer.builder()
//                    .discordId(playerTwo.getDiscordId())
//                    .name(playerTwo.getName())
//                    .champions(playerTwo.getChampions())
//                    .role(playerTwo.getRole())
//                    .build();
//            TentativePlayer tentativePlayerThree = TentativePlayer.builder()
//                    .discordId(playerThree.getDiscordId())
//                    .build();
//
//            Set<Integer> discordIds = Set.of(playerOne.getDiscordId(), playerTwo.getDiscordId(),
//                                             playerThree.getDiscordId());
//
//            HashMap<Integer, TentativePlayer> integerTentativePlayerHashMap = new HashMap<>();
//            integerTentativePlayerHashMap.put(playerOne.getDiscordId(), tentativePlayerOne);
//            integerTentativePlayerHashMap.put(playerTwo.getDiscordId(), tentativePlayerTwo);
//            integerTentativePlayerHashMap.put(playerThree.getDiscordId(), tentativePlayerThree);
//
//            when(userService.retrieveUser(playerOne.getDiscordId()))
//                    .thenReturn(Mono.just(playerOne));
//            when(userService.retrieveUser(playerTwo.getDiscordId()))
//                    .thenReturn(Mono.just(playerTwo));
//            when(userService.retrieveUser(playerThree.getDiscordId()))
//                    .thenReturn(Mono.empty());
//
//            StepVerifier
//                    .create(tentativeController.populateTupleWithTentativePlayerDetails(
//                            Tuples.of(tentatives, discordIds)))
//                    .expectNext(Tuples.of(tentatives, integerTentativePlayerHashMap))
//                    .expectComplete()
//                    .verify();
//        }
//    }
//
//    @Nested
//    @DisplayName("PATCH - assignUserToATentativeQueue")
//    class AssignUserToTentativeQueue {
//
//        @Test
//        @DisplayName("200 - A tentative queue was found and the id was added")
//        void test_assignUserToATentativeQueue_successfullyAddedUserToTentativeQueue() {
//            String tqId = "tq1234";
//            Long discordId = 1L;
//
//            Tentative tentative = easyRandom.nextObject(Tentative.class);
//
//            when(tentativeService.assignUserToTentativeQueue(discordId.intValue(), tqId))
//                    .thenReturn(Mono.just(tentative));
//
//            StepVerifier
//                    .create(tentativeController.assignUserToATentativeQueue(tqId, discordId, null))
//                    .expectNext(ResponseEntity.ok(tentative))
//                    .verifyComplete();
//        }
//
//        @Test
//        @DisplayName("404 - A tentative queue was not found")
//        void test_assignUserToATentativeQueue_unableToFindTentativeQueueWithId() {
//            String tqId = "tq1234";
//            Long discordId = 1L;
//
//            when(tentativeService.assignUserToTentativeQueue(discordId.intValue(), tqId))
//                    .thenReturn(Mono.empty());
//
//            StepVerifier
//                    .create(tentativeController.assignUserToATentativeQueue(tqId, discordId, null))
//                    .expectNext(ResponseEntity.notFound().build())
//                    .verifyComplete();
//        }
//
//    }
//
//    @Nested
//    @DisplayName("DELETE - assignUserToATentativeQueue")
//    class RemoteUserFromTentativeQueue {
//
//        @Test
//        @DisplayName("200 - A tentative queue was found and the id was removed")
//        void test_removeUserFromTentativeQueue_successfullyRemovedUserFromTentativeQueue() {
//            String tqId = "tq1234";
//            Long discordId = 1L;
//
//            Tentative tentative = easyRandom.nextObject(Tentative.class);
//
//            when(tentativeService.removeUserFromTentativeQueue(discordId.intValue(), tqId))
//                    .thenReturn(Mono.just(tentative));
//
//            StepVerifier
//                    .create(tentativeController.removeUserFromTentativeQueue(tqId, discordId, null))
//                    .expectNext(ResponseEntity.ok(tentative))
//                    .verifyComplete();
//        }
//
//        @Test
//        @DisplayName("404 - A tentative queue was not found")
//        void test_removeUserFromTentativeQueue_unableToFindTentativeQueueWithId() {
//            String tqId = "tq1234";
//            Long discordId = 1L;
//
//            when(tentativeService.removeUserFromTentativeQueue(discordId.intValue(), tqId))
//                    .thenReturn(Mono.empty());
//
//            StepVerifier
//                    .create(tentativeController.removeUserFromTentativeQueue(tqId, discordId, null))
//                    .expectNext(ResponseEntity.notFound().build())
//                    .verifyComplete();
//        }
//
//    }
//
//    @Nested
//    @DisplayName("POST - createTentativeQueueBasedOnServerAndTournamentAndDay")
//    class CreateTentativeQueueBasedOnServerAndTournamentAndDay {
//
//        @Test
//        @DisplayName("Should create a Tentative Queue based on the Server, Tournament Name, and Day if one does not already exist.")
//        void test_createTentativeQueueBasedOnServerAndTournamentAndDay_shouldCreateATentativeQueueAndSaveIt() {
//            String tournamentName = "awesome_sauce";
//            String tournamentDay = "1";
//            int discordId = 1;
//            int serverId = 2;
//
//            TentativeRequired tentativePayload = TentativeRequired.builder()
//                    .serverId(serverId)
//                    .tentativePlayers(List.of(TentativePlayer
//                            .builder()
//                            .discordId(discordId)
//                            .build()))
//                    .tournamentDetails(
//                            BaseTournament.builder()
//                                    .tournamentName(tournamentName)
//                                    .tournamentDay(tournamentDay)
//                                    .build()
//                    )
//                    .build();
//
//            TentativeQueue tentativeQueueToSave =
//                    TentativeQueue.builder()
//                            .tentativeId(TentativeId.builder()
//                                                 .tournamentId(TournamentId.builder()
//                                                                       .tournamentName(tournamentName)
//                                                                       .tournamentDay(tournamentDay)
//                                                                       .build())
//                                                 .serverId(serverId)
//                                                 .build())
//                            .discordIds(Set.of(1))
//                            .build();
//
//            Tentative savedMappedObject = easyRandom.nextObject(Tentative.class);
//
//            when(tentativeService.retrieveTentativeQueues(serverId, tournamentName, tournamentDay))
//                    .thenReturn(Flux.empty());
//            when(tentativeService.createTentativeQueue(tentativeQueueToSave))
//                    .thenReturn(Mono.just(savedMappedObject));
//
//            StepVerifier
//                    .create(tentativeController.createTentativeQueue(Mono.just(tentativePayload), null))
//                    .expectNext(ResponseEntity.ok(savedMappedObject))
//                    .expectComplete()
//                    .verify();
//
//            verify(tentativeService, times(1))
//                    .retrieveTentativeQueues(serverId, tournamentName, tournamentDay);
//        }
//
//        @Test
//        @DisplayName("Should create a Tentative Queue based on the Server, Tournament Name, and Day if one does not already exist at all.")
//        void test_createTentativeQueueBasedOnServerAndTournamentAndDay_noTentativeQueues_shouldCreateATentativeQueueAndSaveIt() {
//            String tournamentName = "awesome_sauce";
//            String tournamentDay = "1";
//            int discordId = 1;
//            int serverId = 2;
//
//            TentativeRequired tentativePayload = TentativeRequired.builder()
//                    .serverId(serverId)
//                    .tentativePlayers(List.of(TentativePlayer
//                            .builder()
//                            .discordId(discordId)
//                            .build()))
//                    .tournamentDetails(
//                            BaseTournament.builder()
//                                    .tournamentName(tournamentName)
//                                    .tournamentDay(tournamentDay)
//                                    .build()
//                    )
//                    .build();
//
//            TentativeQueue tentativeQueueToSave =
//                    TentativeQueue.builder()
//                            .tentativeId(TentativeId.builder()
//                                    .tournamentId(TournamentId.builder()
//                                            .tournamentName(tournamentName)
//                                            .tournamentDay(tournamentDay)
//                                            .build())
//                                    .serverId(serverId)
//                                    .build())
//                            .discordIds(Set.of(1))
//                            .build();
//
//            Tentative savedMappedObject = easyRandom.nextObject(Tentative.class);
//
//            when(tentativeService.retrieveTentativeQueues(serverId, tournamentName, tournamentDay))
//                    .thenReturn(Flux.empty());
//            when(tentativeService.createTentativeQueue(tentativeQueueToSave))
//                    .thenReturn(Mono.just(savedMappedObject));
//
//            StepVerifier
//                    .create(tentativeController.createTentativeQueue(Mono.just(tentativePayload), null))
//                    .expectNext(ResponseEntity.ok(savedMappedObject))
//                    .expectComplete()
//                    .verify();
//
//            verify(tentativeService, times(1))
//                    .retrieveTentativeQueues(serverId, tournamentName, tournamentDay);
//        }
//
//        @Test
//        @DisplayName("400 - Should return 400 and not create a Tentative Queue if a Queue exists based on the Server, Tournament Name, and Day.")
//        void test_createTentativeQueueBasedOnServerAndTournamentAndDay_shouldNotCreateATentativeQueueAndSaveItIfOneExists() {
//            String tournamentName = "awesome_sauce";
//            String tournamentDay = "1";
//            int discordId = 1;
//            int serverId = 2;
//
//            TentativeRequired tentativePayload = TentativeRequired.builder()
//                    .serverId(serverId)
//                    .tentativePlayers(List.of(TentativePlayer
//                            .builder()
//                            .discordId(discordId)
//                            .build()))
//                    .tournamentDetails(
//                            BaseTournament.builder()
//                                    .tournamentName(tournamentName)
//                                    .tournamentDay(tournamentDay)
//                                    .build()
//                    )
//                    .build();
//
//            TentativeQueue tentativeQueueToSave =
//                    TentativeQueue.builder()
//                            .tentativeId(TentativeId.builder()
//                                                 .tournamentId(TournamentId.builder()
//                                                                       .tournamentName(tournamentName)
//                                                                       .tournamentDay(tournamentDay)
//                                                                       .build())
//                                                 .serverId(serverId)
//                                                 .build())
//                            .discordIds(Set.of(1))
//                            .build();
//
//            Tentative savedMappedObject = easyRandom.nextObject(Tentative.class);
//
//            when(tentativeService.retrieveTentativeQueues(serverId, tournamentName, tournamentDay))
//                    .thenReturn(Flux.just(easyRandom.nextObject(Tentative.class)));
//            when(tentativeService.createTentativeQueue(tentativeQueueToSave))
//                    .thenReturn(Mono.just(savedMappedObject));
//
//            StepVerifier
//                    .create(tentativeController.createTentativeQueue(Mono.just(tentativePayload), null))
//                    .expectError(ClashBotControllerException.class)
//                    .verify();
//
//            verify(tentativeService, times(1))
//                    .retrieveTentativeQueues(serverId, tournamentName, tournamentDay);
//            verify(tentativeService, times(0))
//                    .createTentativeQueue(tentativeQueueToSave);
//        }
//
//        @Test
//        @DisplayName("400 - Should return 400 and not create a Tentative Queue if the tentativePlayer list is empty.")
//        void test_createTentativeQueueBasedOnServerAndTournamentAndDay_shouldNotCreateATentativeQueueAndSaveItIfNoTentativePlayers() {
//            String tournamentName = "awesome_sauce";
//            String tournamentDay = "1";
//            int serverId = 2;
//
//            TentativeRequired tentativePayload = TentativeRequired.builder()
//                    .serverId(serverId)
//                    .tournamentDetails(
//                            BaseTournament.builder()
//                                    .tournamentName(tournamentName)
//                                    .tournamentDay(tournamentDay)
//                                    .build()
//                    )
//                    .build();
//
//            TentativeQueue tentativeQueueToSave =
//                    TentativeQueue.builder()
//                            .tentativeId(TentativeId.builder()
//                                    .tournamentId(TournamentId.builder()
//                                            .tournamentName(tournamentName)
//                                            .tournamentDay(tournamentDay)
//                                            .build())
//                                    .serverId(serverId)
//                                    .build())
//                            .discordIds(Set.of(1))
//                            .build();
//
//            Tentative savedMappedObject = easyRandom.nextObject(Tentative.class);
//
//            when(tentativeService.createTentativeQueue(tentativeQueueToSave))
//                    .thenReturn(Mono.just(savedMappedObject));
//
//            StepVerifier
//                    .create(tentativeController.createTentativeQueue(Mono.just(tentativePayload), null))
//                    .expectError(ClashBotControllerException.class)
//                    .verify();
//
//            verify(tentativeService, times(0))
//                    .retrieveTentativeQueues(serverId, tournamentName, tournamentDay);
//        }
//
//    }
//
//    @Nested
//    @DisplayName("GET - retrieveTentativeQueues")
//    class RetrieveTentativeQueues {
//
//        @Test
//        @DisplayName("200 - Return all Tentative Queues")
//        void test_retrieveTentativeQueues_mapTentativeQueuesAndPlayerDetails() {
//            Integer serverId = 1;
//            String tournamentName = "awesome_sauce";
//            String tournamentDay = "1";
//            String tentativeQueueId = "abcd";
//            Player playerOne = easyRandom.nextObject(Player.class);
//            playerOne.setChampions(playerOne.getChampions().stream().limit(3).collect(Collectors.toList()));
//            Player playerTwo = easyRandom.nextObject(Player.class);
//            playerTwo.setChampions(playerTwo.getChampions().stream().limit(3).collect(Collectors.toList()));
//            Player playerThree = easyRandom.nextObject(Player.class);
//            playerThree.setChampions(playerThree.getChampions().stream().limit(3).collect(Collectors.toList()));
//            List<TentativePlayer> tentativePlayers = List.of(
//                    TentativePlayer.builder().discordId(playerOne.getDiscordId()).build(),
//                    TentativePlayer.builder().discordId(playerTwo.getDiscordId()).build(),
//                    TentativePlayer.builder().discordId(playerThree.getDiscordId()).build());
//
//            BaseTournament baseTournament = BaseTournament.builder()
//                    .tournamentName(tournamentName)
//                    .tournamentDay(tournamentDay)
//                    .build();
//            ArrayList<Tentative> tentativeQueues = new ArrayList<>();
//            tentativeQueues.add(Tentative.builder()
//                                        .serverId(serverId)
//                                        .tournamentDetails(baseTournament)
//                                        .tentativePlayers(tentativePlayers)
//                                        .id(tentativeQueueId)
//                                        .build());
//            when(tentativeService.retrieveTentativeQueues(null, null, null, null))
//                    .thenReturn(Mono.just(tentativeQueues)
//                                        .flatMapIterable(tentatives -> tentatives));
//            when(userService.retrieveUser(playerOne.getDiscordId()))
//                    .thenReturn(Mono.just(playerOne));
//            when(userService.retrieveUser(playerTwo.getDiscordId()))
//                    .thenReturn(Mono.just(playerTwo));
//            when(userService.retrieveUser(playerThree.getDiscordId()))
//                    .thenReturn(Mono.just(playerThree));
//
//            List<TentativePlayer> tentativePlayerList = List.of(TentativePlayer.builder()
//                                                                        .discordId(playerOne.getDiscordId())
//                                                                        .name(playerOne.getName())
//                                                                        .champions(playerOne.getChampions())
//                                                                        .role(playerOne.getRole())
//                                                                        .build(),
//                                                                TentativePlayer.builder()
//                                                                        .discordId(playerTwo.getDiscordId())
//                                                                        .name(playerTwo.getName())
//                                                                        .champions(playerTwo.getChampions())
//                                                                        .role(playerTwo.getRole())
//                                                                        .build(),
//                                                                TentativePlayer.builder()
//                                                                        .discordId(playerThree.getDiscordId())
//                                                                        .name(playerThree.getName())
//                                                                        .champions(playerThree.getChampions())
//                                                                        .role(playerThree.getRole())
//                                                                        .build());
//
//            Tentatives tentatives = Tentatives.builder()
//                    .queues(List.of(Tentative.builder()
//                                            .serverId(serverId)
//                                            .tournamentDetails(baseTournament)
//                                            .tentativePlayers(tentativePlayerList)
//                                            .id(tentativeQueueId)
//                                            .build()))
//                    .count(1)
//                    .build();
//
//            StepVerifier.create(tentativeController.retrieveTentativeQueues(false, null, null, null, null, null))
//                    .expectNext(ResponseEntity.ok(tentatives))
//                    .expectComplete()
//                    .verify();
//        }
//
//        @Test
//        @DisplayName("200 - Return all Tentative Queues that have been archived")
//        void test_retrieveTentativeQueues_mapTentativeQueuesAndPlayerDetails_onlyActiveTournaments() {
//            Integer serverId = 1;
//            Player playerOne = easyRandom.nextObject(Player.class);
//            playerOne.setChampions(playerOne.getChampions().stream().limit(3).collect(Collectors.toList()));
//            Player playerTwo = easyRandom.nextObject(Player.class);
//            playerTwo.setChampions(playerTwo.getChampions().stream().limit(3).collect(Collectors.toList()));
//            Player playerThree = easyRandom.nextObject(Player.class);
//            playerThree.setChampions(playerThree.getChampions().stream().limit(3).collect(Collectors.toList()));
//            String tentativeQueueIdOne = "12345";
//            String tentativeQueueIdTwo = "12346";
//            List<TentativePlayer> tentativePlayers = List.of(
//                    TentativePlayer.builder().discordId(playerOne.getDiscordId()).build(),
//                    TentativePlayer.builder().discordId(playerTwo.getDiscordId()).build(),
//                    TentativePlayer.builder().discordId(playerThree.getDiscordId()).build());
//
//            BaseTournament baseTournamentOne = easyRandom.nextObject(BaseTournament.class);
//            BaseTournament baseTournamentTwo = easyRandom.nextObject(BaseTournament.class);
//            ArrayList<Tentative> tentativeQueues = new ArrayList<>();
//            tentativeQueues.add(Tentative.builder()
//                                        .id(tentativeQueueIdOne)
//                                        .serverId(serverId)
//                                        .tournamentDetails(baseTournamentOne)
//                                        .tentativePlayers(tentativePlayers)
//                                        .build());
//            tentativeQueues.add(Tentative.builder()
//                                        .id(tentativeQueueIdTwo)
//                                        .serverId(serverId)
//                                        .tournamentDetails(baseTournamentTwo)
//                                        .tentativePlayers(tentativePlayers)
//                                        .build());
//            DetailedTournament activeTournament = DetailedTournament.builder()
//                    .tournamentName(baseTournamentTwo.getTournamentName())
//                    .tournamentDay(baseTournamentTwo.getTournamentDay())
//                    .build();
//
//            when(tournamentService.retrieveAllTournaments(true))
//                    .thenReturn(Flux.just(activeTournament));
//            when(archivedService.retrieveArchivedTentativeQueues(null, null, null, null))
//                    .thenReturn(Mono.just(tentativeQueues)
//                                        .flatMapIterable(tentatives -> tentatives));
//            when(userService.retrieveUser(playerOne.getDiscordId()))
//                    .thenReturn(Mono.just(playerOne));
//            when(userService.retrieveUser(playerTwo.getDiscordId()))
//                    .thenReturn(Mono.just(playerTwo));
//            when(userService.retrieveUser(playerThree.getDiscordId()))
//                    .thenReturn(Mono.just(playerThree));
//
//            List<TentativePlayer> tentativePlayerList = List.of(TentativePlayer.builder()
//                                                                        .discordId(playerOne.getDiscordId())
//                                                                        .name(playerOne.getName())
//                                                                        .champions(playerOne.getChampions())
//                                                                        .role(playerOne.getRole())
//                                                                        .build(),
//                                                                TentativePlayer.builder()
//                                                                        .discordId(playerTwo.getDiscordId())
//                                                                        .name(playerTwo.getName())
//                                                                        .champions(playerTwo.getChampions())
//                                                                        .role(playerTwo.getRole())
//                                                                        .build(),
//                                                                TentativePlayer.builder()
//                                                                        .discordId(playerThree.getDiscordId())
//                                                                        .name(playerThree.getName())
//                                                                        .champions(playerThree.getChampions())
//                                                                        .role(playerThree.getRole())
//                                                                        .build());
//
//            Tentatives tentatives = Tentatives.builder()
//                    .queues(List.of(Tentative.builder()
//                                            .id(tentativeQueueIdOne)
//                                            .serverId(serverId)
//                                            .tournamentDetails(baseTournamentOne)
//                                            .tentativePlayers(tentativePlayerList)
//                                            .build(),
//                            Tentative.builder()
//                                    .id(tentativeQueueIdTwo)
//                                    .serverId(serverId)
//                                    .tournamentDetails(baseTournamentTwo)
//                                    .tentativePlayers(tentativePlayerList)
//                                    .build()))
//                    .count(2)
//                    .build();
//
//            StepVerifier.create(tentativeController.retrieveTentativeQueues(true, null, null, null, null, null))
//                    .expectNext(ResponseEntity.ok(tentatives))
//                    .expectComplete()
//                    .verify();
//        }
//    }
//
//    @Nested
//    @DisplayName("GET - retrieveTentativeQueue")
//    class RetrieveTentativeQueue {
//
//        @Test
//        @DisplayName("200 - Successfully retrieved Tentative Queue based on id")
//        void test_retrieveTentativeQueue_ifTentativeQueueWasFoundThenItShouldBeReturned() {
//            String tqId = "tq1234";
//
//            Tentative tentative = easyRandom.nextObject(Tentative.class);
//
//            when(tentativeService.findById(tqId))
//                    .thenReturn(Mono.just(tentative));
//
//            StepVerifier
//                    .create(tentativeController.retrieveTentativeQueue(tqId, null))
//                    .expectNext(ResponseEntity.ok(tentative))
//                    .verifyComplete();
//        }
//
//        @Test
//        @DisplayName("404 - Was unable to find Tentative Queue based on id")
//        void test_retrieveTentativeQueue_ifTentativeQueueWasNotFoundThen404ShouldBeReturned() {
//            String tqId = "tq1234";
//            when(tentativeService.findById(tqId))
//                    .thenReturn(Mono.empty());
//            StepVerifier
//                    .create(tentativeController.retrieveTentativeQueue(tqId, null))
//                    .expectNext(ResponseEntity.notFound().build())
//                    .verifyComplete();
//        }
//    }
//
//}
