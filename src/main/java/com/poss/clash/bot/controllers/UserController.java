package com.poss.clash.bot.controllers;

import com.poss.clash.bot.daos.models.LoLChampion;
import com.poss.clash.bot.daos.models.User;
import com.poss.clash.bot.enums.UserSubscription;
import com.poss.clash.bot.exceptions.ClashBotControllerException;
import com.poss.clash.bot.openapi.api.UsersApi;
import com.poss.clash.bot.openapi.model.*;
import com.poss.clash.bot.services.UserService;
import com.poss.clash.bot.utils.UserMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.poss.clash.bot.constants.GlobalConstants.CAUSED_BY_KEY;

@RestController
@AllArgsConstructor
@Slf4j
public class UserController implements UsersApi {

    private final UserService userService;

    private final UserMapper userMapper;

    @Override
    public Mono<ResponseEntity<Champions>> addToPreferredChampionsForUser(String xCausedBy, String discordId, Mono<Champions> champions, ServerWebExchange exchange) {
        return updateListOfPreferredChampionsForUser(champions, mergeListOfChampions(discordId))
                .contextWrite(ctx -> ctx.put(CAUSED_BY_KEY, xCausedBy));
    }

    @Override
    public Mono<ResponseEntity<Servers>> addUsersSelectedServers(String xCausedBy, String discordId, Mono<Servers> servers, ServerWebExchange exchange) {
        return updateListOfSelectedServersForUser(servers, mergeListOfServers(discordId))
                .contextWrite(ctx -> ctx.put(CAUSED_BY_KEY, xCausedBy));
    }

    @Override
    public Mono<ResponseEntity<Champions>> createListOfPreferredChampionsForUser(String xCausedBy, String discordId, Mono<Champions> champions, ServerWebExchange exchange) {
        return updateListOfPreferredChampionsForUser(champions, createListOfPreferredChampionsFunc(discordId))
                .contextWrite(ctx -> ctx.put(CAUSED_BY_KEY, xCausedBy));
    }

    @Override
    public Mono<ResponseEntity<Player>> createUser(String xCausedBy, Mono<CreateUserRequest> createUserRequest, ServerWebExchange exchange) {
        return createUserRequest
                .map(userMapper::createUserRequestToUser)
                .map(user -> {
                    user.setDefaultRole(Role.TOP);
                    return user;
                })
                .flatMap(userService::saveUser)
                .map(userMapper::userToPlayer)
                .map(ResponseEntity::ok)
                .contextWrite(ctx -> ctx.put(CAUSED_BY_KEY, xCausedBy));
    }

    @Override
    public Mono<ResponseEntity<Servers>> createUsersSelectedServers(String xCausedBy, String discordId, Mono<Servers> servers, ServerWebExchange exchange) {
        return updateListOfSelectedServersForUser(servers, createListOfAvailableServersFunc(discordId))
                .contextWrite(ctx -> ctx.put(CAUSED_BY_KEY, xCausedBy));
    }

    @Override
    public Mono<ResponseEntity<Player>> getUser(String xCausedBy, String discordId, ServerWebExchange exchange) {
        return userService.retrieveUser(discordId)
                .map(userMapper::userToPlayer)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .contextWrite(ctx -> ctx.put(CAUSED_BY_KEY, xCausedBy));
    }

    @Override
    public Mono<ResponseEntity<Subscription>> isUserSubscribed(String xCausedBy, String discordId, SubscriptionType subscription, ServerWebExchange exchange) {
        return userService.retrieveUser(discordId)
                .map(user -> Subscription.builder()
                        .key(subscription)
                        .isOn(user.getUserSubscriptions()
                                .get(UserSubscription
                                        .fromValue(subscription.getValue())))
                        .build()
                ).map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .contextWrite(ctx -> ctx.put(CAUSED_BY_KEY, xCausedBy));
    }

    @Override
    public Mono<ResponseEntity<Champions>> removePreferredChampionForUser(String xCausedBy, String discordId, List<String> champions, ServerWebExchange exchange) {
        Champions championsPayload = Champions
                .builder()
                .champions(champions.stream().map(name -> Champion.builder().name(name).build())
                        .collect(Collectors.toList()))
                .build();
        return updateListOfPreferredChampionsForUser(Mono.just(championsPayload), removeListOfChampions(discordId))
                .contextWrite(ctx -> ctx.put(CAUSED_BY_KEY, xCausedBy));
    }

    @Override
    public Mono<ResponseEntity<Servers>> removeUsersSelectedServers(String xCausedBy, String discordId, List<String> servers, ServerWebExchange exchange) {
        Servers serversPayload = Servers
                .builder()
                .servers(servers.stream().map(name -> Server.builder().id(name).build())
                        .collect(Collectors.toList()))
                .build();
        return updateListOfSelectedServersForUser(Mono.just(serversPayload), removeListOfAvailableServers(discordId))
                .contextWrite(ctx -> ctx.put(CAUSED_BY_KEY, xCausedBy));
    }

    @Override
    public Mono<ResponseEntity<Champions>> retrieveUsersPreferredChampions(String xCausedBy, String discordId, ServerWebExchange exchange) {
        return userService.retrieveUser(discordId)
                .map(userMapper::userToPlayer)
                .map(player -> ResponseEntity.ok(Champions.builder().champions(player.getChampions()).build()))
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .contextWrite(ctx -> ctx.put(CAUSED_BY_KEY, xCausedBy));
    }

    @Override
    public Mono<ResponseEntity<Servers>> retrieveUsersSelectedServers(String xCausedBy, String discordId, ServerWebExchange exchange) {
        return userService.retrieveUser(discordId)
                .map(User::getSelectedServers)
                .map(listOfIds -> ResponseEntity.ok(Servers.builder().servers(listOfIds.stream()
                                .map(id -> Server.builder().id(id).build())
                                .collect(Collectors.toList()))
                        .build()))
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .contextWrite(ctx -> ctx.put(CAUSED_BY_KEY, xCausedBy));
    }

    @Override
    public Mono<ResponseEntity<Subscription>> subscribeUser(String xCausedBy, String discordId, SubscriptionType subscription, ServerWebExchange exchange) {
        return userService.toggleUserSubscription(discordId, UserSubscription.fromValue(subscription.getValue()), true)
                .map(subscriptionMap -> Subscription.builder()
                        .key(SubscriptionType.fromValue(subscription.getValue()))
                        .isOn(subscriptionMap.get(UserSubscription.fromValue(subscription.getValue())))
                        .build())
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .contextWrite(ctx -> ctx.put(CAUSED_BY_KEY, xCausedBy));
    }

    @Override
    public Mono<ResponseEntity<Subscription>> unsubscribeUser(String xCausedBy, String discordId, SubscriptionType subscription, ServerWebExchange exchange) {
        return userService.toggleUserSubscription(discordId, UserSubscription.fromValue(subscription.getValue()), false)
                .map(subscriptionMap -> Subscription.builder()
                        .key(SubscriptionType.fromValue(subscription.getValue()))
                        .isOn(subscriptionMap.get(UserSubscription.fromValue(subscription.getValue())))
                        .build())
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .contextWrite(ctx -> ctx.put(CAUSED_BY_KEY, xCausedBy));
    }

    @Override
    public Mono<ResponseEntity<Player>> updateUser(String xCausedBy, String discordId, Mono<UpdateUserRequest> updateUserRequest, ServerWebExchange exchange) {
        return updateUserRequest
                .flatMap(request -> userService.updateUserDefaultServerId(discordId, request.getServerId()))
                .map(userMapper::userToPlayer)
                .map(ResponseEntity::ok)
                .contextWrite(ctx -> ctx.put(CAUSED_BY_KEY, xCausedBy));
    }

    private Mono<ResponseEntity<Champions>> updateListOfPreferredChampionsForUser(Mono<Champions> champions, Function<Set<LoLChampion>, Publisher<? extends LoLChampion>> handler) {
        return champions
                .flatMapIterable(Champions::getChampions)
                .map(userMapper::championToLoLChampions)
                .collect(Collectors.toSet())
                .log()
                .flatMapMany(handler::apply)
                .switchIfEmpty(Mono.error(new ClashBotControllerException("User not found.", HttpStatus.NOT_FOUND)))
                .map(userMapper::loLChampionToChampion)
                .collect(Collectors.toSet())
                .map(setOfChampions -> Champions.builder()
                        .champions(new ArrayList<>(setOfChampions))
                        .build())
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    private Function<Set<LoLChampion>, Publisher<? extends LoLChampion>> createListOfPreferredChampionsFunc(String discordId) {
        return preferredChampions -> userService.createPreferredChampionsForUser(discordId, preferredChampions);
    }

    private Function<Set<LoLChampion>, Publisher<? extends LoLChampion>> mergeListOfChampions(String discordId) {
        return preferredChampions -> userService.mergePreferredChampionsForUser(discordId, preferredChampions);
    }

    private Function<Set<LoLChampion>, Publisher<? extends LoLChampion>> removeListOfChampions(String discordId) {
        return preferredChampions -> userService.removePreferredChampionsForUser(discordId, preferredChampions);
    }

    private Mono<ResponseEntity<Servers>> updateListOfSelectedServersForUser(Mono<Servers> servers, Function<Set<String>, Publisher<? extends String>> handler) {
        return servers
                .flatMapIterable(Servers::getServers)
                .map(Server::getId)
                .collect(Collectors.toSet())
                .log()
                .flatMapMany(handler::apply)
                .switchIfEmpty(Mono.error(new ClashBotControllerException("User not found.", HttpStatus.NOT_FOUND)))
                .map(serverId -> Server.builder().id(serverId).build())
                .collectList()
                .map(listOfServers -> Servers.builder()
                        .servers(listOfServers)
                        .build())
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    private Function<Set<String>, Publisher<? extends String>> createListOfAvailableServersFunc(String discordId) {
        return servers -> userService.overwriteSelectedServers(discordId, servers);
    }

    private Function<Set<String>, Publisher<? extends String>> mergeListOfServers(String discordId) {
        return servers -> userService.mergeSelectedServers(discordId, servers);
    }

    private Function<Set<String>, Publisher<? extends String>> removeListOfAvailableServers(String discordId) {
        return servers -> userService.removeSelectedServers(discordId, servers);
    }

}
