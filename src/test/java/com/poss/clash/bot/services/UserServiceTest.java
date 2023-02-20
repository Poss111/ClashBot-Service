package com.poss.clash.bot.services;

import com.poss.clash.bot.ClashBotTestingConfig;
import com.poss.clash.bot.daos.UserDao;
import com.poss.clash.bot.daos.models.LoLChampion;
import com.poss.clash.bot.daos.models.User;
import com.poss.clash.bot.enums.UserSubscription;
import com.poss.clash.bot.utils.UserMapper;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
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

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
            Integer updatedServerId = 124;

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
            Integer updatedServerId = 124;

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
            LoLChampion championTwo = easyRandom.nextObject(LoLChampion.class);
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

    }

}
