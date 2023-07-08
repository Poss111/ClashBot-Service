package com.poss.clash.bot.controllers;

import com.poss.clash.bot.ClashBotTestingConfig;
import com.poss.clash.bot.daos.models.LoLChampion;
import com.poss.clash.bot.daos.models.User;
import com.poss.clash.bot.enums.UserSubscription;
import com.poss.clash.bot.openapi.model.*;
import com.poss.clash.bot.services.UserAssociationService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.poss.clash.bot.constants.GlobalConstants.CAUSED_BY_KEY;
import static org.mockito.Mockito.when;

@ExtendWith({SpringExtension.class})
@Import(ClashBotTestingConfig.class)
public class UserControllerTest {

  @InjectMocks
  UserController userController;

  @Mock
  UserService userService;

  @Mock
  UserAssociationService userAssociationService;

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
      String discordId = easyRandom.nextObject(String.class);
      Champions championsListToAdd = easyRandom.nextObject(Champions.class);
      String xCausedBy = easyRandom.nextObject(String.class);

      Set<LoLChampion> setOfLolChampions = championsListToAdd
          .getChampions()
          .stream()
          .map(userMapper::championToLoLChampions)
          .collect(Collectors.toSet());
      championsListToAdd.setChampions(setOfLolChampions
                                          .stream()
                                          .map(userMapper::loLChampionToChampion)
                                          .collect(Collectors.toList()));

      when(userService.mergePreferredChampionsForUser(discordId, setOfLolChampions))
          .thenReturn(Mono
                          .just(setOfLolChampions)
                          .flatMapMany(Flux::fromIterable));
      when(userAssociationService.updateInvolvedTeams(xCausedBy))
          .thenReturn(Mono
                          .just(Event
                                    .builder()
                                    .build())
                          .flux());

      StepVerifier
          .create(
              userController.addToPreferredChampionsForUser(xCausedBy, discordId, Mono.just(championsListToAdd), null))
          .expectAccessibleContext()
          .contains(CAUSED_BY_KEY, xCausedBy)
          .then()
          .expectNextMatches(response ->
                                 championsListToAdd
                                     .getChampions()
                                     .size() == response
                                     .getBody()
                                     .getChampions()
                                     .size()
                                 && 200 == response.getStatusCodeValue())
          .verifyComplete();
    }

    @Test
    @DisplayName("404 - Should return with 404 if the flux returned is empty")
    void test2() {
      String discordId = easyRandom.nextObject(String.class);
      Champions championsListToAdd = easyRandom.nextObject(Champions.class);

      Set<LoLChampion> setOfLolChampions = championsListToAdd
          .getChampions()
          .stream()
          .map(userMapper::championToLoLChampions)
          .collect(Collectors.toSet());
      championsListToAdd.setChampions(setOfLolChampions
                                          .stream()
                                          .map(userMapper::loLChampionToChampion)
                                          .collect(Collectors.toList()));

      when(userService.mergePreferredChampionsForUser(discordId, setOfLolChampions))
          .thenReturn(Flux.empty());

      String xCausedBy = easyRandom.nextObject(String.class);
      StepVerifier
          .create(
              userController.addToPreferredChampionsForUser(xCausedBy, discordId, Mono.just(championsListToAdd), null))
          .expectAccessibleContext()
          .contains(CAUSED_BY_KEY, xCausedBy)
          .then()
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
      String discordId = easyRandom.nextObject(String.class);
      Champions championsListToAdd = easyRandom.nextObject(Champions.class);
      String xCausedBy = easyRandom.nextObject(String.class);

      Set<LoLChampion> setOfLolChampions = championsListToAdd
          .getChampions()
          .stream()
          .map(userMapper::championToLoLChampions)
          .collect(Collectors.toSet());
      championsListToAdd.setChampions(setOfLolChampions
                                          .stream()
                                          .map(userMapper::loLChampionToChampion)
                                          .collect(Collectors.toList()));

      when(userService.createPreferredChampionsForUser(discordId, setOfLolChampions))
          .thenReturn(Mono
                          .just(setOfLolChampions)
                          .flatMapMany(Flux::fromIterable));
      when(userAssociationService.updateInvolvedTeams(xCausedBy))
          .thenReturn(Mono
                          .just(Event
                                    .builder()
                                    .build())
                          .flux());
      StepVerifier
          .create(
              userController.createListOfPreferredChampionsForUser(xCausedBy, discordId, Mono.just(championsListToAdd),
                                                                   null))
          .expectAccessibleContext()
          .contains(CAUSED_BY_KEY, xCausedBy)
          .then()
          .expectNextMatches(response ->
                                 championsListToAdd
                                     .getChampions()
                                     .size() == response
                                     .getBody()
                                     .getChampions()
                                     .size()
                                 && 200 == response.getStatusCodeValue())
          .verifyComplete();
    }

    @Test
    @DisplayName("404 - Should return with 404 if the flux returned is empty")
    void test2() {
      String discordId = easyRandom.nextObject(String.class);
      Champions championsListToAdd = easyRandom.nextObject(Champions.class);

      Set<LoLChampion> setOfLolChampions = championsListToAdd
          .getChampions()
          .stream()
          .map(userMapper::championToLoLChampions)
          .collect(Collectors.toSet());
      championsListToAdd.setChampions(setOfLolChampions
                                          .stream()
                                          .map(userMapper::loLChampionToChampion)
                                          .collect(Collectors.toList()));

      when(userService.createPreferredChampionsForUser(discordId, setOfLolChampions))
          .thenReturn(Flux.empty());

      String xCausedBy = easyRandom.nextObject(String.class);
      StepVerifier
          .create(
              userController.createListOfPreferredChampionsForUser(xCausedBy, discordId, Mono.just(championsListToAdd),
                                                                   null))
          .expectAccessibleContext()
          .contains(CAUSED_BY_KEY, xCausedBy)
          .then()
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
      String discordId = easyRandom.nextObject(String.class);
      Champions championsListToAdd = easyRandom.nextObject(Champions.class);
      String xCausedBy = easyRandom.nextObject(String.class);

      List<String> listOfNames = championsListToAdd
          .getChampions()
          .stream()
          .map(Champion::getName)
          .collect(Collectors.toList());

      Set<LoLChampion> setOfLolChampions = championsListToAdd
          .getChampions()
          .stream()
          .map(userMapper::championToLoLChampions)
          .collect(Collectors.toSet());
      championsListToAdd.setChampions(setOfLolChampions
                                          .stream()
                                          .map(userMapper::loLChampionToChampion)
                                          .collect(Collectors.toList()));

      when(userService.removePreferredChampionsForUser(discordId, setOfLolChampions))
          .thenReturn(Mono
                          .just(setOfLolChampions)
                          .flatMapMany(Flux::fromIterable));
      when(userAssociationService.updateInvolvedTeams(xCausedBy))
          .thenReturn(Mono
                          .just(Event
                                    .builder()
                                    .build())
                          .flux());

      StepVerifier
          .create(userController.removePreferredChampionForUser(xCausedBy, discordId, listOfNames, null))
          .expectAccessibleContext()
          .contains(CAUSED_BY_KEY, xCausedBy)
          .then()
          .expectNextMatches(response ->
                                 championsListToAdd
                                     .getChampions()
                                     .size() == response
                                     .getBody()
                                     .getChampions()
                                     .size()
                                 && 200 == response.getStatusCodeValue())
          .verifyComplete();
    }

    @Test
    @DisplayName("404 - Should return with 404 if the flux returned is empty")
    void test2() {
      String discordId = easyRandom.nextObject(String.class);
      Champions championsListToAdd = easyRandom.nextObject(Champions.class);
      List<String> listOfNames = championsListToAdd
          .getChampions()
          .stream()
          .map(Champion::getName)
          .collect(Collectors.toList());

      Set<LoLChampion> setOfLolChampions = championsListToAdd
          .getChampions()
          .stream()
          .map(userMapper::championToLoLChampions)
          .collect(Collectors.toSet());
      championsListToAdd.setChampions(setOfLolChampions
                                          .stream()
                                          .map(userMapper::loLChampionToChampion)
                                          .collect(Collectors.toList()));

      when(userService.removePreferredChampionsForUser(discordId, setOfLolChampions))
          .thenReturn(Flux.empty());

      String xCausedBy = easyRandom.nextObject(String.class);
      StepVerifier
          .create(userController.removePreferredChampionForUser(xCausedBy, discordId, listOfNames, null))
          .expectAccessibleContext()
          .contains(CAUSED_BY_KEY, xCausedBy)
          .then()
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

      String xCausedBy = easyRandom.nextObject(String.class);
      StepVerifier
          .create(userController.retrieveUsersPreferredChampions(xCausedBy, user.getDiscordId(), null))
          .expectAccessibleContext()
          .contains(CAUSED_BY_KEY, xCausedBy)
          .then()
          .expectNext(ResponseEntity.ok(Champions
                                            .builder()
                                            .champions(player.getChampions())
                                            .build()))
          .verifyComplete();
    }

    @Test
    @DisplayName("404 - Should return a 404 if the user was not founds")
    void test2() {
      User user = easyRandom.nextObject(User.class);

      when(userService.retrieveUser(user.getDiscordId()))
          .thenReturn(Mono.empty());

      String xCausedBy = easyRandom.nextObject(String.class);
      StepVerifier
          .create(userController.retrieveUsersPreferredChampions(xCausedBy, user.getDiscordId(), null))
          .expectAccessibleContext()
          .contains(CAUSED_BY_KEY, xCausedBy)
          .then()
          .expectNext(ResponseEntity
                          .notFound()
                          .build())
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

      String xCausedBy = easyRandom.nextObject(String.class);
      StepVerifier
          .create(userController.createUser(xCausedBy, Mono.just(request), null))
          .expectAccessibleContext()
          .contains(CAUSED_BY_KEY, xCausedBy)
          .then()
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

      String xCausedBy = easyRandom.nextObject(String.class);
      StepVerifier
          .create(userController.getUser(xCausedBy, request.getDiscordId(), null))
          .expectAccessibleContext()
          .contains(CAUSED_BY_KEY, xCausedBy)
          .then()
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
      String discordId = easyRandom.nextObject(String.class);
      UpdateUserRequest request = easyRandom.nextObject(UpdateUserRequest.class);
      User user = easyRandom.nextObject(User.class);

      when(userService.updateUserDefaultServerId(discordId, request.getServerId()))
          .thenReturn(Mono.just(user));
      String xCausedBy = easyRandom.nextObject(String.class);
      StepVerifier
          .create(userController.updateUser(xCausedBy, discordId, Mono.just(request), null))
          .expectAccessibleContext()
          .contains(CAUSED_BY_KEY, xCausedBy)
          .then()
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

      Subscription expectedSubscription = Subscription
          .builder()
          .key(SubscriptionType.DISCORD_MONDAY_NOTIFICATION)
          .isOn(true)
          .build();

      when(userService.toggleUserSubscription(user.getDiscordId(), UserSubscription.DISCORD_MONDAY_NOTIFICATION, true))
          .thenReturn(Mono.just(Map.of(UserSubscription.DISCORD_MONDAY_NOTIFICATION, true)));

      String xCausedBy = easyRandom.nextObject(String.class);
      StepVerifier
          .create(
              userController.subscribeUser(xCausedBy, user.getDiscordId(), SubscriptionType.DISCORD_MONDAY_NOTIFICATION,
                                           null))
          .expectAccessibleContext()
          .contains(CAUSED_BY_KEY, xCausedBy)
          .then()
          .expectNext(ResponseEntity.ok(expectedSubscription))
          .verifyComplete();
    }

    @Test
    @DisplayName("404 - User not found")
    void test2() {
      User user = easyRandom.nextObject(User.class);

      when(userService.toggleUserSubscription(user.getDiscordId(), UserSubscription.DISCORD_MONDAY_NOTIFICATION, true))
          .thenReturn(Mono.empty());

      String xCausedBy = easyRandom.nextObject(String.class);
      StepVerifier
          .create(
              userController.subscribeUser(xCausedBy, user.getDiscordId(), SubscriptionType.DISCORD_MONDAY_NOTIFICATION,
                                           null))
          .expectAccessibleContext()
          .contains(CAUSED_BY_KEY, xCausedBy)
          .then()
          .expectNext(ResponseEntity
                          .notFound()
                          .build())
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

      Subscription expectedSubscription = Subscription
          .builder()
          .key(SubscriptionType.DISCORD_MONDAY_NOTIFICATION)
          .isOn(false)
          .build();

      when(userService.toggleUserSubscription(user.getDiscordId(), UserSubscription.DISCORD_MONDAY_NOTIFICATION, false))
          .thenReturn(Mono.just(Map.of(UserSubscription.DISCORD_MONDAY_NOTIFICATION, false)));

      String xCausedBy = easyRandom.nextObject(String.class);
      StepVerifier
          .create(userController.unsubscribeUser(xCausedBy, user.getDiscordId(),
                                                 SubscriptionType.DISCORD_MONDAY_NOTIFICATION, null))
          .expectAccessibleContext()
          .contains(CAUSED_BY_KEY, xCausedBy)
          .then()
          .expectNext(ResponseEntity.ok(expectedSubscription))
          .verifyComplete();
    }

    @Test
    @DisplayName("404 - if the user does not exist")
    void test2() {
      User user = easyRandom.nextObject(User.class);

      when(userService.toggleUserSubscription(user.getDiscordId(), UserSubscription.DISCORD_MONDAY_NOTIFICATION, false))
          .thenReturn(Mono.empty());

      String xCausedBy = easyRandom.nextObject(String.class);
      StepVerifier
          .create(userController.unsubscribeUser(xCausedBy, user.getDiscordId(),
                                                 SubscriptionType.DISCORD_MONDAY_NOTIFICATION, null))
          .expectAccessibleContext()
          .contains(CAUSED_BY_KEY, xCausedBy)
          .then()
          .expectNext(ResponseEntity
                          .notFound()
                          .build())
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

      Subscription subscription = Subscription
          .builder()
          .key(SubscriptionType.DISCORD_MONDAY_NOTIFICATION)
          .isOn(user
                    .getUserSubscriptions()
                    .get(UserSubscription.DISCORD_MONDAY_NOTIFICATION))
          .build();

      String xCausedBy = easyRandom.nextObject(String.class);
      StepVerifier
          .create(userController.isUserSubscribed(
              xCausedBy,
              user.getDiscordId(),
              SubscriptionType.DISCORD_MONDAY_NOTIFICATION,
              null))
          .expectAccessibleContext()
          .contains(CAUSED_BY_KEY, xCausedBy)
          .then()
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

      String xCausedBy = easyRandom.nextObject(String.class);
      StepVerifier
          .create(userController.isUserSubscribed(
              xCausedBy,
              user.getDiscordId(),
              SubscriptionType.DISCORD_MONDAY_NOTIFICATION,
              null))
          .expectAccessibleContext()
          .contains(CAUSED_BY_KEY, xCausedBy)
          .then()
          .expectNext(ResponseEntity
                          .notFound()
                          .build())
          .verifyComplete();
    }

  }

  @Nested
  @DisplayName("POST - createUsersSelectedServers")
  class PostUserSelectedServers {

    @Test
    @DisplayName("200 - User's list of selected servers should be saved")
    void test1() {
      String discordId = easyRandom.nextObject(String.class);
      Servers servers = Servers
          .builder()
          .servers(List.of(
              easyRandom.nextObject(Server.class),
              easyRandom.nextObject(Server.class),
              easyRandom.nextObject(Server.class)
          ))
          .build();

      Set<String> setOfServers = servers
          .getServers()
          .stream()
          .map(Server::getId)
          .collect(Collectors.toSet());
      List<Server> expectedServers = servers
          .getServers()
          .stream()
          .map(Server::getId)
          .map(serverId -> Server
              .builder()
              .id(serverId)
              .build())
          .collect(Collectors.toList());

      when(userService.overwriteSelectedServers(discordId, setOfServers))
          .thenReturn(Mono
                          .just(setOfServers)
                          .flatMapMany(Flux::fromIterable));

      String xCausedBy = easyRandom.nextObject(String.class);
      StepVerifier
          .create(userController.createUsersSelectedServers(xCausedBy, discordId, Mono.just(servers), null))
          .expectAccessibleContext()
          .contains(CAUSED_BY_KEY, xCausedBy)
          .then()
          .expectNextMatches(response -> HttpStatus.OK.value() == response.getStatusCodeValue()
                                         && null != response.getBody()
                                         && response
                                             .getBody()
                                             .getServers()
                                             .containsAll(expectedServers))
          .verifyComplete();
    }

    @Test
    @DisplayName("404 - User is not found")
    void test2() {
      String discordId = easyRandom.nextObject(String.class);
      Servers servers = easyRandom.nextObject(Servers.class);

      Set<String> setOfServers = servers
          .getServers()
          .stream()
          .map(Server::getId)
          .collect(Collectors.toSet());

      when(userService.overwriteSelectedServers(discordId, setOfServers))
          .thenReturn(Flux.empty());

      String xCausedBy = easyRandom.nextObject(String.class);
      StepVerifier
          .create(userController.createUsersSelectedServers(xCausedBy, discordId, Mono.just(servers), null))
          .expectAccessibleContext()
          .contains(CAUSED_BY_KEY, xCausedBy)
          .then()
          .expectErrorMessage("User not found.")
          .verify();
    }

  }

  @Nested
  @DisplayName("PATCH - addUsersSelectedServers")
  class PatchUserSelectedServers {

    @Test
    @DisplayName("200 - User's list of selected servers should be added to")
    void test1() {
      String discordId = easyRandom.nextObject(String.class);
      Servers servers = easyRandom.nextObject(Servers.class);

      Set<String> setOfServers = servers
          .getServers()
          .stream()
          .map(Server::getId)
          .collect(Collectors.toSet());

      List<Server> expectedServers = servers
          .getServers()
          .stream()
          .map(Server::getId)
          .map(serverId -> Server
              .builder()
              .id(serverId)
              .build())
          .collect(Collectors.toList());

      when(userService.mergeSelectedServers(discordId, setOfServers))
          .thenReturn(Mono
                          .just(setOfServers)
                          .flatMapMany(Flux::fromIterable));

      String xCausedBy = easyRandom.nextObject(String.class);
      StepVerifier
          .create(userController.addUsersSelectedServers(xCausedBy, discordId, Mono.just(servers), null))
          .expectAccessibleContext()
          .contains(CAUSED_BY_KEY, xCausedBy)
          .then()
          .expectNextMatches(response -> HttpStatus.OK.value() == response.getStatusCodeValue()
                                         && null != response.getBody()
                                         && response
                                             .getBody()
                                             .getServers()
                                             .containsAll(expectedServers))
          .verifyComplete();
    }

    @Test
    @DisplayName("404 - User is not found")
    void test2() {
      String discordId = easyRandom.nextObject(String.class);
      Servers servers = easyRandom.nextObject(Servers.class);

      Set<String> setOfServers = servers
          .getServers()
          .stream()
          .map(Server::getId)
          .collect(Collectors.toSet());

      when(userService.mergeSelectedServers(discordId, setOfServers))
          .thenReturn(Flux.empty());

      String xCausedBy = easyRandom.nextObject(String.class);
      StepVerifier
          .create(userController.addUsersSelectedServers(xCausedBy, discordId, Mono.just(servers), null))
          .expectAccessibleContext()
          .contains(CAUSED_BY_KEY, xCausedBy)
          .then()
          .expectErrorMessage("User not found.")
          .verify();
    }

  }

  @Nested
  @DisplayName("DELETE - removeUsersSelectedServers")
  class DeleteUserSelectedServers {

    @Test
    @DisplayName("200 - User's list of selected servers should be removed")
    void test1() {
      String discordId = easyRandom.nextObject(String.class);
      List<String> ids = List.of(
          easyRandom.nextObject(String.class),
          easyRandom.nextObject(String.class),
          easyRandom.nextObject(String.class)
      );

      List<Server> listOfServers = ids
          .stream()
          .map(id -> Server
              .builder()
              .id(id)
              .build())
          .collect(Collectors.toList());
      Set<String> set = ids
          .parallelStream()
          .collect(Collectors.toSet());

      when(userService.removeSelectedServers(discordId, set))
          .thenReturn(Mono
                          .just(set)
                          .flatMapMany(Flux::fromIterable));

      String xCausedBy = easyRandom.nextObject(String.class);
      StepVerifier
          .create(userController.removeUsersSelectedServers(xCausedBy, discordId, ids, null))
          .expectAccessibleContext()
          .contains(CAUSED_BY_KEY, xCausedBy)
          .then()
          .expectNextMatches(response -> HttpStatus.OK.value() == response.getStatusCodeValue()
                                         && null != response.getBody()
                                         && response
                                             .getBody()
                                             .getServers()
                                             .containsAll(listOfServers))
          .verifyComplete();
    }

    @Test
    @DisplayName("404 - User is not found")
    void test2() {
      String discordId = easyRandom.nextObject(String.class);
      Servers servers = easyRandom.nextObject(Servers.class);

      Set<String> setOfServers = servers
          .getServers()
          .stream()
          .map(Server::getId)
          .collect(Collectors.toSet());
      List<String> serversIdList = servers
          .getServers()
          .stream()
          .map(Server::getId)
          .collect(Collectors.toList());

      when(userService.removeSelectedServers(discordId, setOfServers))
          .thenReturn(Flux.empty());

      String xCausedBy = easyRandom.nextObject(String.class);
      StepVerifier
          .create(userController.removeUsersSelectedServers(xCausedBy, discordId, serversIdList, null))
          .expectAccessibleContext()
          .contains(CAUSED_BY_KEY, xCausedBy)
          .then()
          .expectErrorMessage("User not found.")
          .verify();
    }

  }

  @Nested
  @DisplayName("GET - retrieveUsersSelectedServers")
  class GetUserSelectedServers {

    @Test
    @DisplayName("200 - Should return a list of the User's selected servers")
    void test1() {
      String discordId = easyRandom.nextObject(String.class);
      Servers servers = easyRandom.nextObject(Servers.class);
      User user = easyRandom.nextObject(User.class);
      user.setDiscordId(discordId);

      Set<String> serversIdList = servers
          .getServers()
          .stream()
          .map(Server::getId)
          .collect(Collectors.toSet());
      List<Server> serverIds = serversIdList
          .stream()
          .map(id -> Server
              .builder()
              .id(id)
              .build())
          .distinct()
          .collect(Collectors.toList());

      user.setSelectedServers(serversIdList);

      when(userService.retrieveUser(discordId))
          .thenReturn(Mono.just(user));

      servers.servers(serverIds);

      String xCausedBy = easyRandom.nextObject(String.class);
      StepVerifier
          .create(userController.retrieveUsersSelectedServers(xCausedBy, discordId, null))
          .expectAccessibleContext()
          .contains(CAUSED_BY_KEY, xCausedBy)
          .then()
          .expectNext(ResponseEntity.ok(servers))
          .verifyComplete();
    }

    @Test
    @DisplayName("404 - User is not found")
    void test2() {
      String discordId = easyRandom.nextObject(String.class);
      when(userService.retrieveUser(discordId))
          .thenReturn(Mono.empty());
      String xCausedBy = easyRandom.nextObject(String.class);
      StepVerifier
          .create(userController.retrieveUsersSelectedServers(xCausedBy, discordId, null))
          .expectAccessibleContext()
          .contains(CAUSED_BY_KEY, xCausedBy)
          .then()
          .expectNext(ResponseEntity
                          .notFound()
                          .build())
          .verifyComplete();
    }

  }

}
