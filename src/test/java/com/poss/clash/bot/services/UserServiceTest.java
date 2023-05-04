package com.poss.clash.bot.services;

import com.poss.clash.bot.ClashBotTestingConfig;
import com.poss.clash.bot.daos.UserDao;
import com.poss.clash.bot.daos.models.*;
import com.poss.clash.bot.enums.UserSubscription;
import com.poss.clash.bot.openapi.model.Role;
import com.poss.clash.bot.utils.UserMapper;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.publisher.PublisherProbe;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@Import(ClashBotTestingConfig.class)
public class UserServiceTest {

    @InjectMocks
    UserService userService;

    @Mock
    UserDao userDao;

    UserMapper userMapper = Mappers.getMapper(UserMapper.class);

    @Autowired
    EasyRandom easyRandom;

    @Nested
    @DisplayName("Query")
    class Query {

        @Test
        @DisplayName("retrieveUser - should retrieve a user based on their Discord Id")
        void test() {
            User user = easyRandom.nextObject(User.class);

            when(userDao.findUserByDiscordId(user.getDiscordId()))
                    .thenReturn(Mono.just(user));

            StepVerifier
                    .create(userService.retrieveUser(user.getDiscordId()))
                    .expectNext(user)
                    .verifyComplete();
        }

    }

    @Nested
    @DisplayName("Create")
    class Create {

        @Test
        @DisplayName("saveUser - If user does not exist, then it should be saved")
        void test() {
            User user = easyRandom.nextObject(User.class);

            when(userDao.findUserByDiscordId(user.getDiscordId()))
                    .thenReturn(Mono.empty());
            PublisherProbe<User> publisherProbe = PublisherProbe.of(Mono.just(user));
            when(userDao.save(user))
                    .thenReturn(publisherProbe.mono());

            StepVerifier
                    .create(userService.saveUser(user))
                    .expectNext(user)
                    .verifyComplete();
            assertTrue(publisherProbe.wasSubscribed(), "User was not saved when it should have been");
        }

        @Test
        @DisplayName("saveUser - If the user does exist, then it should be passed back and not saved")
        void test2() {
            User user = easyRandom.nextObject(User.class);

            when(userDao.findUserByDiscordId(user.getDiscordId()))
                    .thenReturn(Mono.just(user));
            PublisherProbe<User> publisherProbe = PublisherProbe.empty();
            when(userDao.save(user))
                    .thenReturn(publisherProbe.mono());

            StepVerifier
                    .create(userService.saveUser(user))
                    .expectNext(user)
                    .verifyComplete();

            verify(userDao, times(1))
                    .findUserByDiscordId(user.getDiscordId());
            assertFalse(publisherProbe.wasSubscribed(), "User was saved when it should not have been");
        }

        @Test
        @DisplayName("createPreferredChampionsForUser - Should take in a Discord id and a set of preferred champions and overwrite whatever is saved for the User")
        void test3() {
            User user = easyRandom.nextObject(User.class);
            user.getPreferredChampions().clear();
            User expectedUser = userMapper.clone(user);

            LoLChampion championOne = easyRandom.nextObject(LoLChampion.class);
            Set<LoLChampion> loLChampionSetToCreate = Set.of(championOne);

            expectedUser.setPreferredChampions(loLChampionSetToCreate);

            when(userDao.findUserByDiscordId(user.getDiscordId()))
                    .thenReturn(Mono.just(user));
            when(userDao.save(user))
                    .thenReturn(Mono.just(user));

            StepVerifier
                    .create(userService.createPreferredChampionsForUser(
                            user.getDiscordId(),
                            loLChampionSetToCreate
                    ))
                    .expectNext(championOne)
                    .verifyComplete();
        }

    }

    @Nested
    @DisplayName("Update")
    class Update {

        @Test
        @DisplayName("updateUserDefaultServerId - should take in a Discord id and Server id and only update the Server Id.")
        void test() {
            User user = easyRandom.nextObject(User.class);
            String updatedServerId = easyRandom.nextObject(String.class);

            when(userDao.findUserByDiscordId(user.getDiscordId()))
                    .thenReturn(Mono.just(user));
            PublisherProbe<Long> publisherProbe = PublisherProbe.of(Mono.just(1L));
            when(userDao.updateUserDefaultServerId(user.getDiscordId(), updatedServerId))
                    .thenReturn(publisherProbe.mono());

            StepVerifier
                    .create(userService.updateUserDefaultServerId(user.getDiscordId(), updatedServerId))
                    .expectNext(user)
                    .verifyComplete();

            assertTrue(publisherProbe.wasSubscribed());
        }

        @Test
        @DisplayName("updateUserDefaultServerId - If a user is not found, it should return empty.")
        void test1() {
            User user = easyRandom.nextObject(User.class);
            String updatedServerId = easyRandom.nextObject(String.class);

            when(userDao.findUserByDiscordId(user.getDiscordId()))
                    .thenReturn(Mono.empty());
            PublisherProbe<Long> publisherProbe = PublisherProbe.of(Mono.just(1L));
            when(userDao.updateUserDefaultServerId(user.getDiscordId(), updatedServerId))
                    .thenReturn(publisherProbe.mono());

            StepVerifier
                    .create(userService.updateUserDefaultServerId(user.getDiscordId(), updatedServerId))
                    .verifyComplete();

            assertFalse(publisherProbe.wasSubscribed());
        }


        @Test
        @DisplayName("toggleUserSubscription - should take in a Discord id and a User Subscription as well as the expected status and set it to that status.")
        void test2() {
            User user = easyRandom.nextObject(User.class);
            User updatedUser = userMapper.clone(user);

            updatedUser.getUserSubscriptions().put(UserSubscription.DISCORD_MONDAY_NOTIFICATION, true);
            when(userDao.findUserByDiscordId(user.getDiscordId()))
                    .thenReturn(Mono.just(user));
            PublisherProbe<User> publisherProbe = PublisherProbe.of(Mono.just(updatedUser));
            when(userDao.save(updatedUser))
                    .thenReturn(publisherProbe.mono());

            StepVerifier
                    .create(userService.toggleUserSubscription(user.getDiscordId(), UserSubscription.DISCORD_MONDAY_NOTIFICATION, true))
                    .expectNext(updatedUser.getUserSubscriptions())
                    .verifyComplete();

            assertTrue(publisherProbe.wasSubscribed());
        }

        @Test
        @DisplayName("toggleUserSubscription - If a User Subscription map did not exist before, it should set a new one to be updated.")
        void test3() {
            User user = easyRandom.nextObject(User.class);
            User updatedUser = userMapper.clone(user);

            user.setUserSubscriptions(null);
            updatedUser.getUserSubscriptions().put(UserSubscription.DISCORD_MONDAY_NOTIFICATION, true);
            when(userDao.findUserByDiscordId(user.getDiscordId()))
                    .thenReturn(Mono.just(user));
            PublisherProbe<User> publisherProbe = PublisherProbe.of(Mono.just(updatedUser));
            when(userDao.save(updatedUser))
                    .thenReturn(publisherProbe.mono());

            StepVerifier
                    .create(userService.toggleUserSubscription(user.getDiscordId(), UserSubscription.DISCORD_MONDAY_NOTIFICATION, true))
                    .expectNext(updatedUser.getUserSubscriptions())
                    .verifyComplete();

            assertTrue(publisherProbe.wasSubscribed());
        }

        @Test
        @DisplayName("mergePreferredChampionsForUser - should take in a Discord id and a list of champions and merge the current list with the new list.")
        void test4() {
            User user = easyRandom.nextObject(User.class);
            User expectedUser = userMapper.clone(user);

            LoLChampion championOne = easyRandom.nextObject(LoLChampion.class);
            championOne.setName("Anivia");
            LoLChampion championTwo = easyRandom.nextObject(LoLChampion.class);
            championTwo.setName("Annie");
            Set<LoLChampion> originalLoLChampionSet = new HashSet<>();
            originalLoLChampionSet.add(championOne);
            Set<LoLChampion> loLChampionSetToMerge = new HashSet<>();
            loLChampionSetToMerge.add(championTwo);
            Set<LoLChampion> expectedMergedChampionSet = new HashSet<>();
            expectedMergedChampionSet.add(championOne);
            expectedMergedChampionSet.add(championTwo);

            user.setPreferredChampions(originalLoLChampionSet);
            expectedUser.setPreferredChampions(expectedMergedChampionSet);

            when(userDao.findUserByDiscordId(user.getDiscordId()))
                    .thenReturn(Mono.just(user));
            when(userDao.save(user))
                    .thenReturn(Mono.just(user));

            StepVerifier
                    .create(userService.mergePreferredChampionsForUser(
                            user.getDiscordId(),
                            loLChampionSetToMerge
                    ))
                    .expectNext(championOne)
                    .expectNext(championTwo)
                    .verifyComplete();
        }

        @Test
        @DisplayName("removePreferredChampionsForUser - should take in a Discord id and remove the list of preferred champions that are found set to the User")
        void test5() {
            User user = easyRandom.nextObject(User.class);
            User expectedUser = userMapper.clone(user);

            LoLChampion championOne = easyRandom.nextObject(LoLChampion.class);
            LoLChampion championTwo = easyRandom.nextObject(LoLChampion.class);
            Set<LoLChampion> originalLoLChampionSet = new HashSet<>();
            originalLoLChampionSet.add(championOne);
            originalLoLChampionSet.add(championTwo);
            Set<LoLChampion> loLChampionSetToDelete = new HashSet<>();
            loLChampionSetToDelete.add(championOne);
            Set<LoLChampion> expectedMergedChampionSet = new HashSet<>();
            expectedMergedChampionSet.add(championTwo);

            user.setPreferredChampions(originalLoLChampionSet);
            expectedUser.setPreferredChampions(expectedMergedChampionSet);

            when(userDao.findUserByDiscordId(user.getDiscordId()))
                    .thenReturn(Mono.just(user));
            when(userDao.save(user))
                    .thenReturn(Mono.just(user));

            StepVerifier
                    .create(userService.removePreferredChampionsForUser(
                            user.getDiscordId(),
                            loLChampionSetToDelete
                    ))
                    .expectNext(championTwo)
                    .verifyComplete();
        }

        @Nested
        @DisplayName("Selected Server")
        class SelectedServerUpdates {

            @Test
            @DisplayName("mergeSelectedServers - should merge server id list into user list and save the User after the update")
            void test_mergeSelectedServers_shouldMergeTheUserList_returnNewServerIdList() {
                Set<String> newServerIds = Set.of("1", "2");
                Set<String> oldListOfServerIds = new HashSet<>();
                oldListOfServerIds.add("1");
                String userDiscordId = "1";
                User returnedUser = User.builder()
                        .discordId(userDiscordId)
                        .selectedServers(oldListOfServerIds)
                        .build();
                User expectedUserToSave = userMapper.clone(returnedUser);
                expectedUserToSave.setSelectedServers(newServerIds);

                when(userDao.findUserByDiscordId(userDiscordId))
                        .thenReturn(Mono.just(returnedUser));
                when(userDao.save(expectedUserToSave))
                        .thenReturn(Mono.just(expectedUserToSave));
                StepVerifier.create(userService.mergeSelectedServers(userDiscordId, newServerIds))
                        .recordWith(HashSet::new)
                        .expectNextCount(2)
                        .consumeRecordedWith(list -> assertEquals(newServerIds, list))
                        .verifyComplete();
            }

            @TestFactory
            Stream<DynamicTest> testMergeSelectedServers() {
                List<Set<String>> originalUserSelectedServerIds = new ArrayList<>();
                originalUserSelectedServerIds.add(Set.of("1", "2", "3"));
                originalUserSelectedServerIds.add(Set.of("1", "2", "3"));
                originalUserSelectedServerIds.add(new HashSet<>());
                originalUserSelectedServerIds.add(null);
                List<Set<String>> listToMergeUserSelectedServerIds = List.of(
                        Set.of("4"),
                        Set.of("3"),
                        Set.of("4"),
                        Set.of("4")
                );

                List<Set<String>> expectedOutputSelectedServerIds = Arrays.asList(
                        Set.of("1", "2", "3", "4"),
                        Set.of("1", "2", "3"),
                        Set.of("4"),
                        Set.of("4")
                );

                return originalUserSelectedServerIds.stream()
                        .map(dom -> {
                            int index = originalUserSelectedServerIds.indexOf(dom);
                            Set<String> setToMergeWith = listToMergeUserSelectedServerIds.get(index);
                            Set<String> expectedSet = expectedOutputSelectedServerIds.get(index);
                            return DynamicTest.dynamicTest(MessageFormat.format("Given starting Selected Servers of {0} and merging {1} should equal {2}", dom, setToMergeWith, expectedSet),
                                    () -> {
                                        User ogUser = User.builder().selectedServers(dom).build();
                                        User updated = userMapper.clone(ogUser);
                                        assertEquals(expectedSet, userService.mergeSelectedServersList(setToMergeWith).apply(updated).getSelectedServers());
                                    });
                        });
            }
        }

        @Test
        @DisplayName("overwriteSelectedServers - should overwrite server id list into user list and save the User after the update")
        void test_overwriteSelectedServers_shouldOverwriteTheUserList_returnNewServerIdList() {
            Set<String> newServerIds = Set.of("1", "2");
            Set<String> oldListOfServerIds = new HashSet<>();
            oldListOfServerIds.add("3");
            String userDiscordId = "1";
            User returnedUser = User.builder()
                    .discordId(userDiscordId)
                    .selectedServers(oldListOfServerIds)
                    .build();
            User expectedUserToSave = userMapper.clone(returnedUser);
            expectedUserToSave.setSelectedServers(newServerIds);

            when(userDao.findUserByDiscordId(userDiscordId))
                    .thenReturn(Mono.just(returnedUser));
            when(userDao.save(expectedUserToSave))
                    .thenReturn(Mono.just(expectedUserToSave));
            StepVerifier.create(userService.overwriteSelectedServers(userDiscordId, newServerIds))
                    .recordWith(HashSet::new)
                    .expectNextCount(newServerIds.size())
                    .consumeRecordedWith(list -> assertEquals(newServerIds, list))
                    .verifyComplete();
        }

        @TestFactory
        Stream<DynamicTest> testOverwriteSelectedServers() {
            List<Set<String>> originalUserSelectedServerIds = new ArrayList<>();
            originalUserSelectedServerIds.add(Set.of("1", "2", "3"));
            originalUserSelectedServerIds.add(Set.of("1", "2", "3"));
            originalUserSelectedServerIds.add(new HashSet<>());
            originalUserSelectedServerIds.add(null);
            List<Set<String>> listToMergeUserSelectedServerIds = List.of(
                    Set.of("4"),
                    Set.of("3"),
                    Set.of("4"),
                    Set.of("4")
            );

            List<Set<String>> expectedOutputSelectedServerIds = Arrays.asList(
                    Set.of("4"),
                    Set.of("4"),
                    Set.of("4"),
                    Set.of("4")
            );

            return originalUserSelectedServerIds.stream()
                    .map(dom -> {
                        int index = originalUserSelectedServerIds.indexOf(dom);
                        Set<String> setToMergeWith = listToMergeUserSelectedServerIds.get(index);
                        Set<String> expectedSet = expectedOutputSelectedServerIds.get(index);
                        return DynamicTest.dynamicTest(MessageFormat.format("Given starting Selected Servers of {0} and being overwritten with {1} it should equal {2}", dom, setToMergeWith, expectedSet),
                                () -> {
                                    User ogUser = User.builder().selectedServers(dom).build();
                                    User updated = userMapper.clone(ogUser);
                                    assertEquals(expectedSet, userService.overwriteSelectedServersList(setToMergeWith).apply(updated).getSelectedServers());
                                });
                    });
        }

        @Test
        @DisplayName("removeSelectedServers - should remove server ids from user list and save the User after the update")
        void test_removeSelectedServers_shouldRemoveSelectedServersInTheUserList_returnNewServerIdList() {
            Set<String> newServerIds = Set.of("9");
            Set<String> oldListOfServerIds = new HashSet<>();
            oldListOfServerIds.add("1");
            oldListOfServerIds.add("9");
            oldListOfServerIds.add("3");
            Set<String> expectedServerIdsToSave = new HashSet<>();
            expectedServerIdsToSave.add("1");
            expectedServerIdsToSave.add("3");
            String userDiscordId = "1";
            User returnedUser = User.builder()
                    .discordId(userDiscordId)
                    .selectedServers(oldListOfServerIds)
                    .build();
            User expectedUserToSave = userMapper.clone(returnedUser);
            expectedUserToSave.setSelectedServers(expectedServerIdsToSave);

            when(userDao.findUserByDiscordId(userDiscordId))
                    .thenReturn(Mono.just(returnedUser));
            when(userDao.save(expectedUserToSave))
                    .thenReturn(Mono.just(expectedUserToSave));
            StepVerifier.create(userService.removeSelectedServers(userDiscordId, newServerIds))
                    .recordWith(HashSet::new)
                    .expectNextCount(2)
                    .consumeRecordedWith(list -> assertEquals(expectedServerIdsToSave, list))
                    .verifyComplete();
        }

        @TestFactory
        Stream<DynamicTest> testRemoveSelectedServers() {
            List<Set<String>> originalUserSelectedServerIds = new ArrayList<>();
            originalUserSelectedServerIds.add(Set.of("1", "2", "3", "4"));
            originalUserSelectedServerIds.add(Set.of("1", "2", "4"));
            originalUserSelectedServerIds.add(new HashSet<>());
            originalUserSelectedServerIds.add(null);
            List<Set<String>> listToMergeUserSelectedServerIds = List.of(
                    Set.of("4"),
                    Set.of("1", "4"),
                    Set.of("4"),
                    Set.of("4")
            );

            List<Set<String>> expectedOutputSelectedServerIds = Arrays.asList(
                    Set.of("1", "2", "3"),
                    Set.of("2"),
                    new HashSet<>(),
                    null
            );

            return originalUserSelectedServerIds.stream()
                    .map(dom -> {
                        int index = originalUserSelectedServerIds.indexOf(dom);
                        Set<String> setToMergeWith = listToMergeUserSelectedServerIds.get(index);
                        Set<String> expectedSet = expectedOutputSelectedServerIds.get(index);
                        return DynamicTest.dynamicTest(MessageFormat.format("Given starting Selected Servers of {0} and removing {1} it should equal {2}", dom, setToMergeWith, expectedSet),
                                () -> {
                                    User ogUser = User.builder().selectedServers(dom).build();
                                    User updated = userMapper.clone(ogUser);
                                    assertEquals(expectedSet, userService.removeSelectedServersList(setToMergeWith).apply(updated).getSelectedServers());
                                });
                    });
        }

    }

    @Nested
    @DisplayName("Populate User Details")
    class PopulateUserDetails {

        @Test
        void test_enrichClashTeamsWithUserDetails_populateListOfUserDetailsForListOfClashTeams() {
            String teamId = "ct-1234";
            String awesomeTeam = "Awesome Team";
            HashMap<Role, BasePlayerRecord> positions = new HashMap<>();
            String discordId = "1";
            String discordIdTwo = "2";
            String discordIdThree = "3";
            String discordIdFour = "4";
            String discordIdFive = "5";
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

            TournamentId tournamentId = TournamentId.builder()
                    .tournamentName("awesome_sauce")
                    .tournamentDay("1")
                    .build();
            String serverId = "1234";
            String teamIdTwo = "ct-1235";
            String awesomeTeamTwo = "Awesome Team Two";
            HashMap<Role, BasePlayerRecord> positionsTwo = new HashMap<>();
            String discordIdSix = "6";
            String discordIdSeven = "7";
            String discordIdEight = "8";
            String discordIdNine = "9";
            String discordIdTen = "10";
            positionsTwo.put(Role.TOP, BasePlayerRecord.builder()
                    .discordId(discordIdSix)
                    .build());
            positionsTwo.put(Role.JG, BasePlayerRecord.builder()
                    .discordId(discordIdSeven)
                    .build());
            positionsTwo.put(Role.MID, BasePlayerRecord.builder()
                    .discordId(discordIdEight)
                    .build());
            positionsTwo.put(Role.BOT, BasePlayerRecord.builder()
                    .discordId(discordIdNine)
                    .build());
            positionsTwo.put(Role.SUPP, BasePlayerRecord.builder()
                    .discordId(discordIdTen)
                    .build());

            TournamentId tournamentIdTwo = TournamentId.builder()
                    .tournamentName("awesome_sauce")
                    .tournamentDay("1")
                    .build();
            String serverIdTwo = "1234";
            ClashTeam clashTeamEntity = ClashTeam.builder()
                    .teamId(TeamId.builder()
                            .id(teamId)
                            .tournamentId(tournamentId)
                            .build())
                    .teamName(awesomeTeam)
                    .serverId(serverId)
                    .positions(positions)
                    .build();
            ClashTeam clashTeamEntityTwo = ClashTeam.builder()
                    .teamId(TeamId.builder()
                            .id(teamIdTwo)
                            .tournamentId(tournamentIdTwo)
                            .build())
                    .teamName(awesomeTeamTwo)
                    .serverId(serverIdTwo)
                    .positions(positionsTwo)
                    .build();
            Set<LoLChampion> expectedPreferredChamps = Set.of(LoLChampion.builder()
                    .name("Anivia")
                    .build());
            List<ClashTeam> clashTeamListToEnrich = List.of(clashTeamEntity, clashTeamEntityTwo);
            positions.forEach((key, value) -> when(userService.retrieveUser(value.getDiscordId()))
                    .thenReturn(Mono.just(User.builder().discordId(value.getDiscordId()).preferredChampions(expectedPreferredChamps).build())));
            positionsTwo.forEach((key, value) -> when(userService.retrieveUser(value.getDiscordId()))
                    .thenReturn(Mono.just(User.builder().discordId(value.getDiscordId()).preferredChampions(expectedPreferredChamps).build())));
            StepVerifier
                    .create(userService.enrichClashTeamsWithUserDetails(clashTeamListToEnrich))
                    .recordWith(HashSet::new)
                    .expectNextCount(2)
                    .consumeRecordedWith(enrichedClashTeams -> {
                        for (ClashTeam clashTeam : enrichedClashTeams) {
                            clashTeam.getPositions().forEach((key, value) -> assertEquals(expectedPreferredChamps, value.getChampionsToPlay()));
                        }
                    })
                    .verifyComplete();
        }

        @Test
        void test_enrichClashTeamWithUserDetails_populateUserClashTeamDetails() {
            String teamId = "ct-1234";
            String awesomeTeam = "Awesome Team";
            HashMap<Role, BasePlayerRecord> positions = new HashMap<>();
            String discordId = "1";
            String discordIdTwo = "2";
            String discordIdThree = "3";
            String discordIdFour = "4";
            String discordIdFive = "5";
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

            TournamentId tournamentId = TournamentId.builder()
                    .tournamentName("awesome_sauce")
                    .tournamentDay("1")
                    .build();
            String serverId = "1234";

            ClashTeam clashTeamEntity = ClashTeam.builder()
                    .teamId(TeamId.builder()
                            .id(teamId)
                            .tournamentId(tournamentId)
                            .build())
                    .teamName(awesomeTeam)
                    .serverId(serverId)
                    .positions(positions)
                    .build();
            Set<LoLChampion> expectedPreferredChamps = Set.of(LoLChampion.builder()
                    .name("Anivia")
                    .build());
            positions.forEach((key, value) -> when(userService.retrieveUser(value.getDiscordId()))
                    .thenReturn(Mono.just(User.builder().discordId(value.getDiscordId()).preferredChampions(expectedPreferredChamps).build())));
            StepVerifier
                    .create(userService.enrichClashTeamWithUserDetails(clashTeamEntity))
                    .expectNextMatches(enrichedClashTeam -> {
                        enrichedClashTeam.getPositions().forEach((key, value) -> assertEquals(expectedPreferredChamps, value.getChampionsToPlay()));
                        return true;
                    })
                    .verifyComplete();
        }

        @Test
        void test_populateUserClashTeamDetails() {
            String teamId = "ct-1234";
            String awesomeTeam = "Awesome Team";
            HashMap<Role, BasePlayerRecord> positions = new HashMap<>();
            String discordId = "1";
            String discordIdTwo = "2";
            String discordIdThree = "3";
            String discordIdFour = "4";
            String discordIdFive = "5";
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

            TournamentId tournamentId = TournamentId.builder()
                    .tournamentName("awesome_sauce")
                    .tournamentDay("1")
                    .build();
            String serverId = "1234";
            ClashTeam clashTeamEntity = ClashTeam.builder()
                    .teamId(TeamId.builder()
                            .id(teamId)
                            .tournamentId(tournamentId)
                            .build())
                    .teamName(awesomeTeam)
                    .serverId(serverId)
                    .positions(positions)
                    .build();
            Set<LoLChampion> expectedPreferredChamps = Set.of(LoLChampion.builder()
                    .name("Anivia")
                    .build());
            Map<String, User> idToPlayerDetailsMap = new HashMap<>();
            positions.forEach((key, value) -> idToPlayerDetailsMap.put(value.getDiscordId(), User.builder()
                    .discordId(value.getDiscordId())
                    .preferredChampions(expectedPreferredChamps)
                    .build()));
            ClashTeam populatedTeam = userService.populateTeamUserDetails(clashTeamEntity, idToPlayerDetailsMap);
            assertNotNull(populatedTeam.getPositions().get(Role.TOP).getChampionsToPlay());
            assertEquals(expectedPreferredChamps, populatedTeam.getPositions().get(Role.TOP).getChampionsToPlay());
        }
    }

}
