package com.poss.clash.bot.controllers;

import com.poss.clash.bot.ClashBotTestingConfig;
import com.poss.clash.bot.daos.models.LoLChampion;
import com.poss.clash.bot.daos.models.User;
import com.poss.clash.bot.enums.UserSubscription;
import com.poss.clash.bot.openapi.model.*;
import com.poss.clash.bot.services.UserService;
import com.poss.clash.bot.utils.UserMapper;
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
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.mockito.Mockito.when;

@ExtendWith({SpringExtension.class})
@Import(ClashBotTestingConfig.class)
public class UserControllerTest {

    @InjectMocks
    UserController userController;

    @Mock
    UserService userService;

    @Spy
    UserMapper userMapper = Mappers.getMapper(UserMapper.class);

    @Autowired
    EasyRandom easyRandom;

    @Nested
    @DisplayName("PATCH - addToPreferredChampionsForUser")
    class AddToPreferredChamps {

        @Test
        @DisplayName("200 - Should take in a list of champions and should invoke to merge the User's list and finally return an updated champions object")
        void test() {
            Long discordId = 1L;
            Champions championsListToAdd = easyRandom.nextObject(Champions.class);

            Set<LoLChampion> setOfLolChampions = championsListToAdd.getChampions()
                    .stream()
                    .map(userMapper::championToLoLChampions)
                    .collect(Collectors.toSet());
            championsListToAdd.setChampions(setOfLolChampions.stream()
                            .map(userMapper::loLChampionToChampion)
                            .collect(Collectors.toList()));

            when(userService.mergePreferredChampionsForUser(discordId.intValue(), setOfLolChampions))
                    .thenReturn(Mono.just(setOfLolChampions).flatMapMany(Flux::fromIterable));

            StepVerifier
                    .create(userController.addToPreferredChampionsForUser(discordId, Mono.just(championsListToAdd), null))
                    .expectNextMatches(response ->
                            championsListToAdd.getChampions().size() == response.getBody().getChampions().size()
                            && 200 == response.getStatusCodeValue())
                    .verifyComplete();
        }

        @Test
        @DisplayName("404 - Should return with 404 if the flux returned is empty")
        void test2() {
            Long discordId = 1L;
            Champions championsListToAdd = easyRandom.nextObject(Champions.class);

            Set<LoLChampion> setOfLolChampions = championsListToAdd.getChampions()
                    .stream()
                    .map(userMapper::championToLoLChampions)
                    .collect(Collectors.toSet());
            championsListToAdd.setChampions(setOfLolChampions.stream()
                    .map(userMapper::loLChampionToChampion)
                    .collect(Collectors.toList()));

            when(userService.mergePreferredChampionsForUser(discordId.intValue(), setOfLolChampions))
                    .thenReturn(Flux.empty());

            StepVerifier
                    .create(userController.addToPreferredChampionsForUser(discordId, Mono.just(championsListToAdd), null))
                    .expectErrorMessage("User not found.")
                    .verify();
        }

    }

    @Nested
    @DisplayName("POST - createListOfPreferredChampionsForUser")
    class CreateListOfPreferredChamps {

        @Test
        @DisplayName("200 - Should return with the new list of champions passed")
        void test() {
            Long discordId = 1L;
            Champions championsListToAdd = easyRandom.nextObject(Champions.class);

            Set<LoLChampion> setOfLolChampions = championsListToAdd.getChampions()
                    .stream()
                    .map(userMapper::championToLoLChampions)
                    .collect(Collectors.toSet());
            championsListToAdd.setChampions(setOfLolChampions.stream()
                    .map(userMapper::loLChampionToChampion)
                    .collect(Collectors.toList()));

            when(userService.createPreferredChampionsForUser(discordId.intValue(), setOfLolChampions))
                    .thenReturn(Mono.just(setOfLolChampions).flatMapMany(Flux::fromIterable));

            StepVerifier
                    .create(userController.createListOfPreferredChampionsForUser(discordId, Mono.just(championsListToAdd), null))
                    .expectNextMatches(response ->
                            championsListToAdd.getChampions().size() == response.getBody().getChampions().size()
                                    && 200 == response.getStatusCodeValue())
                    .verifyComplete();
        }

        @Test
        @DisplayName("404 - Should return with 404 if the flux returned is empty")
        void test2() {
            Long discordId = 1L;
            Champions championsListToAdd = easyRandom.nextObject(Champions.class);

            Set<LoLChampion> setOfLolChampions = championsListToAdd.getChampions()
                    .stream()
                    .map(userMapper::championToLoLChampions)
                    .collect(Collectors.toSet());
            championsListToAdd.setChampions(setOfLolChampions.stream()
                    .map(userMapper::loLChampionToChampion)
                    .collect(Collectors.toList()));

            when(userService.createPreferredChampionsForUser(discordId.intValue(), setOfLolChampions))
                    .thenReturn(Flux.empty());

            StepVerifier
                    .create(userController.createListOfPreferredChampionsForUser(discordId, Mono.just(championsListToAdd), null))
                    .expectErrorMessage("User not found.")
                    .verify();
        }

    }

    @Nested
    @DisplayName("DELETE - removePreferredChampionForUser")
    class RemoveListOfPreferredChamps {

        @Test
        @DisplayName("200 - Should return with the updated list after champions are removed")
        void test() {
            Long discordId = 1L;
            Champions championsListToAdd = easyRandom.nextObject(Champions.class);
            List<String> listOfNames = championsListToAdd.getChampions().stream().map(Champion::getName).collect(Collectors.toList());

            Set<LoLChampion> setOfLolChampions = championsListToAdd.getChampions()
                    .stream()
                    .map(userMapper::championToLoLChampions)
                    .collect(Collectors.toSet());
            championsListToAdd.setChampions(setOfLolChampions.stream()
                    .map(userMapper::loLChampionToChampion)
                    .collect(Collectors.toList()));

            when(userService.removePreferredChampionsForUser(discordId.intValue(), setOfLolChampions))
                    .thenReturn(Mono.just(setOfLolChampions).flatMapMany(Flux::fromIterable));

            StepVerifier
                    .create(userController.removePreferredChampionForUser(discordId, listOfNames, null))
                    .expectNextMatches(response ->
                            championsListToAdd.getChampions().size() == response.getBody().getChampions().size()
                                    && 200 == response.getStatusCodeValue())
                    .verifyComplete();
        }

        @Test
        @DisplayName("404 - Should return with 404 if the flux returned is empty")
        void test2() {
            Long discordId = 1L;
            Champions championsListToAdd = easyRandom.nextObject(Champions.class);
            List<String> listOfNames = championsListToAdd.getChampions().stream().map(Champion::getName).collect(Collectors.toList());

            Set<LoLChampion> setOfLolChampions = championsListToAdd.getChampions()
                    .stream()
                    .map(userMapper::championToLoLChampions)
                    .collect(Collectors.toSet());
            championsListToAdd.setChampions(setOfLolChampions.stream()
                    .map(userMapper::loLChampionToChampion)
                    .collect(Collectors.toList()));

            when(userService.removePreferredChampionsForUser(discordId.intValue(), setOfLolChampions))
                    .thenReturn(Flux.empty());

            StepVerifier
                    .create(userController.removePreferredChampionForUser(discordId, listOfNames, null))
                    .expectErrorMessage("User not found.")
                    .verify();
        }

    }

    @Nested
    @DisplayName("GET - retrieveUsersPreferredChampions")
    class RetrieveChamps {

        @Test
        @DisplayName("200 - Should return the list of the User's preferred champions")
        void test() {
            User user = easyRandom.nextObject(User.class);

            when(userService.retrieveUser(user.getDiscordId()))
                    .thenReturn(Mono.just(user));

            Player player = userMapper.userToPlayer(user);

            StepVerifier
                    .create(userController.retrieveUsersPreferredChampions(Long.valueOf(user.getDiscordId()), null))
                    .expectNext(ResponseEntity.ok(Champions.builder().champions(player.getChampions()).build()))
                    .verifyComplete();
        }

        @Test
        @DisplayName("404 - Should return a 404 if the user was not founds")
        void test2() {
            User user = easyRandom.nextObject(User.class);

            when(userService.retrieveUser(user.getDiscordId()))
                    .thenReturn(Mono.empty());

            Player player = userMapper.userToPlayer(user);

            StepVerifier
                    .create(userController.retrieveUsersPreferredChampions(Long.valueOf(user.getDiscordId()), null))
                    .expectNext(ResponseEntity.notFound().build())
                    .verifyComplete();
        }

    }

    @Nested
    @DisplayName("POST - createUser")
    class CreateUser {

        @Test
        @DisplayName("200 - Should invoke to create a new User")
        void test() {
            CreateUserRequest request = easyRandom.nextObject(CreateUserRequest.class);

            User user = userMapper.createUserRequestToUser(request);
            user.setDefaultRole(Role.TOP);

            when(userService.saveUser(user))
                    .thenReturn(Mono.just(user));

            StepVerifier
                    .create(userController.createUser(Mono.just(request), null))
                    .expectNext(ResponseEntity.ok(userMapper.userToPlayer(user)))
                    .verifyComplete();
        }

    }

    @Nested
    @DisplayName("GET - getUser")
    class GetUser {

        @Test
        @DisplayName("200 - Should retrieve the User based on Id")
        void test() {
            User request = easyRandom.nextObject(User.class);

            when(userService.retrieveUser(request.getDiscordId()))
                    .thenReturn(Mono.just(request));

            StepVerifier
                    .create(userController.getUser(request.getDiscordId(), null))
                    .expectNext(ResponseEntity.ok(userMapper.userToPlayer(request)))
                    .verifyComplete();
        }

    }

    @Nested
    @DisplayName("PATCH - updateUser")
    class UpdateUser {

        @Test
        @DisplayName("200 - Should take in a create user request and invoke an update")
        void test() {
            Long discordId = 1L;
            UpdateUserRequest request = easyRandom.nextObject(UpdateUserRequest.class);
            User user = easyRandom.nextObject(User.class);

            when(userService.updateUserDefaultServerId(discordId.intValue(), request.getServerId()))
                    .thenReturn(Mono.just(user));
            StepVerifier
                    .create(userController.updateUser(discordId, Mono.just(request), null))
                    .expectNext(ResponseEntity.ok(userMapper.userToPlayer(user)))
                    .verifyComplete();
        }

    }

    @Nested
    @DisplayName("POST - subscribeUser")
    class SubscribeUser {

        @Test
        @DisplayName("200 - Should invoke to subscribe user to the passed Subscription")
        void test() {
            User user = easyRandom.nextObject(User.class);

            Subscription expectedSubscription = Subscription.builder()
                    .key(SubscriptionType.DISCORD_MONDAY_NOTIFICATION)
                    .isOn(true)
                    .build();

            when(userService.toggleUserSubscription(user.getDiscordId(), UserSubscription.DISCORD_MONDAY_NOTIFICATION, true))
                    .thenReturn(Mono.just(Map.of(UserSubscription.DISCORD_MONDAY_NOTIFICATION, true)));

            StepVerifier
                    .create(userController.subscribeUser(user.getDiscordId().longValue(), SubscriptionType.DISCORD_MONDAY_NOTIFICATION, null))
                    .expectNext(ResponseEntity.ok(expectedSubscription))
                    .verifyComplete();
        }

        @Test
        @DisplayName("404 - User not found")
        void test2() {
            User user = easyRandom.nextObject(User.class);

            when(userService.toggleUserSubscription(user.getDiscordId(), UserSubscription.DISCORD_MONDAY_NOTIFICATION, true))
                    .thenReturn(Mono.empty());

            StepVerifier
                    .create(userController.subscribeUser(user.getDiscordId().longValue(), SubscriptionType.DISCORD_MONDAY_NOTIFICATION, null))
                    .expectNext(ResponseEntity.notFound().build())
                    .verifyComplete();
        }

    }

    @Nested
    @DisplayName("DELETE - unsubscribeUser")
    class UnsubscribeUser {

        @Test
        @DisplayName("200 - Should invoke to unsubscribe user to the passed Subscription")
        void test() {
            User user = easyRandom.nextObject(User.class);

            Subscription expectedSubscription = Subscription.builder()
                    .key(SubscriptionType.DISCORD_MONDAY_NOTIFICATION)
                    .isOn(false)
                    .build();

            when(userService.toggleUserSubscription(user.getDiscordId(), UserSubscription.DISCORD_MONDAY_NOTIFICATION, false))
                    .thenReturn(Mono.just(Map.of(UserSubscription.DISCORD_MONDAY_NOTIFICATION, false)));

            StepVerifier
                    .create(userController.unsubscribeUser(user.getDiscordId().longValue(), SubscriptionType.DISCORD_MONDAY_NOTIFICATION, null))
                    .expectNext(ResponseEntity.ok(expectedSubscription))
                    .verifyComplete();
        }

        @Test
        @DisplayName("404 - if the user does not exist")
        void test2() {
            User user = easyRandom.nextObject(User.class);

            when(userService.toggleUserSubscription(user.getDiscordId(), UserSubscription.DISCORD_MONDAY_NOTIFICATION, false))
                    .thenReturn(Mono.empty());

            StepVerifier
                    .create(userController.unsubscribeUser(user.getDiscordId().longValue(), SubscriptionType.DISCORD_MONDAY_NOTIFICATION, null))
                    .expectNext(ResponseEntity.notFound().build())
                    .verifyComplete();
        }

    }

    @Nested
    @DisplayName("GET - isUserSubscribed")
    class RetrieveUserSubscription {

        @Test
        @DisplayName("200 - Should return the Users subscription status based on the subscription")
        void test() {
            User user = easyRandom.nextObject(User.class);

            when(userService.retrieveUser(
                    user.getDiscordId())
            ).thenReturn(Mono.just(user));

            Subscription subscription = Subscription.builder()
                    .key(SubscriptionType.DISCORD_MONDAY_NOTIFICATION)
                    .isOn(user.getUserSubscriptions().get(UserSubscription.DISCORD_MONDAY_NOTIFICATION))
                    .build();

            StepVerifier
                    .create(userController.isUserSubscribed(
                            user.getDiscordId().longValue(),
                            SubscriptionType.DISCORD_MONDAY_NOTIFICATION,
                            null))
                    .expectNext(ResponseEntity.ok(subscription))
                    .verifyComplete();
        }

        @Test
        @DisplayName("404 - Should return 404 if the user does not exist")
        void test2() {
            User user = easyRandom.nextObject(User.class);

            when(userService.retrieveUser(
                    user.getDiscordId())
            ).thenReturn(Mono.empty());

            Subscription subscription = Subscription.builder()
                    .key(SubscriptionType.DISCORD_MONDAY_NOTIFICATION)
                    .isOn(user.getUserSubscriptions().get(UserSubscription.DISCORD_MONDAY_NOTIFICATION))
                    .build();

            StepVerifier
                    .create(userController.isUserSubscribed(
                            user.getDiscordId().longValue(),
                            SubscriptionType.DISCORD_MONDAY_NOTIFICATION,
                            null))
                    .expectNext(ResponseEntity.notFound().build())
                    .verifyComplete();
        }

    }

}
