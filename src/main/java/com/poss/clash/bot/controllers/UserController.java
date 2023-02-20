package com.poss.clash.bot.controllers;

import com.poss.clash.bot.daos.models.LoLChampion;
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

@RestController
@AllArgsConstructor
@Slf4j
public class UserController implements UsersApi {

    private final UserService userService;

    private final UserMapper userMapper;

    @Override
    public Mono<ResponseEntity<Champions>> addToPreferredChampionsForUser(Long discordId, Mono<Champions> champions, ServerWebExchange exchange) {
        return updateListOfPreferredChampionsForUser(champions, mergeListOfChampions(discordId));
    }

    @Override
    public Mono<ResponseEntity<Champions>> createListOfPreferredChampionsForUser(Long discordId, Mono<Champions> champions, ServerWebExchange exchange) {
        return updateListOfPreferredChampionsForUser(champions, createListOfPrefferedChampionsFunc(discordId));
    }

    @Override
    public Mono<ResponseEntity<Player>> createUser(Mono<CreateUserRequest> createUserRequest, ServerWebExchange exchange) {
        return createUserRequest
                .map(userMapper::createUserRequestToUser)
                .map(user -> {
                    user.setDefaultRole(Role.TOP);
                    return user;
                })
                .flatMap(userService::saveUser)
                .map(userMapper::userToPlayer)
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<Player>> getUser(Integer discordId, ServerWebExchange exchange) {
        return userService.retrieveUser(discordId)
                .map(userMapper::userToPlayer)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @Override
    public Mono<ResponseEntity<Subscription>> isUserSubscribed(Long discordId, SubscriptionType subscription, ServerWebExchange exchange) {
        return userService.retrieveUser(discordId.intValue())
                .map(user -> Subscription.builder()
                                .key(subscription)
                                .isOn(user.getUserSubscriptions()
                                        .get(UserSubscription
                                                .fromValue(subscription.getValue())))
                                .build()
                ).map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @Override
    public Mono<ResponseEntity<Champions>> removePreferredChampionForUser(Long discordId, List<String> champions, ServerWebExchange exchange) {
        Champions championsPayload = Champions
                .builder()
                .champions(champions.stream().map(name -> Champion.builder().name(name).build())
                        .collect(Collectors.toList()))
                .build();
        return updateListOfPreferredChampionsForUser(Mono.just(championsPayload), removeListOfChampions(discordId));
    }

    @Override
    public Mono<ResponseEntity<Champions>> retrieveUsersPreferredChampions(Long discordId, ServerWebExchange exchange) {
        return userService.retrieveUser(discordId.intValue())
                .map(userMapper::userToPlayer)
                .map(player -> ResponseEntity.ok(Champions.builder().champions(player.getChampions()).build()))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @Override
    public Mono<ResponseEntity<Subscription>> subscribeUser(Long discordId, SubscriptionType subscription, ServerWebExchange exchange) {
        return userService.toggleUserSubscription(discordId.intValue(), UserSubscription.fromValue(subscription.getValue()), true)
                .map(subscriptionMap -> Subscription.builder()
                        .key(SubscriptionType.fromValue(subscription.getValue()))
                        .isOn(subscriptionMap.get(UserSubscription.fromValue(subscription.getValue())))
                        .build())
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @Override
    public Mono<ResponseEntity<Subscription>> unsubscribeUser(Long discordId, SubscriptionType subscription, ServerWebExchange exchange) {
        return userService.toggleUserSubscription(discordId.intValue(), UserSubscription.fromValue(subscription.getValue()), false)
                .map(subscriptionMap -> Subscription.builder()
                        .key(SubscriptionType.fromValue(subscription.getValue()))
                        .isOn(subscriptionMap.get(UserSubscription.fromValue(subscription.getValue())))
                        .build())
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @Override
    public Mono<ResponseEntity<Player>> updateUser(Long discordId, Mono<UpdateUserRequest> updateUserRequest, ServerWebExchange exchange) {
        return updateUserRequest
                .flatMap(request -> userService.updateUserDefaultServerId(discordId.intValue(), request.getServerId()))
                .map(userMapper::userToPlayer)
                .map(ResponseEntity::ok);
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

    private Function<Set<LoLChampion>, Publisher<? extends LoLChampion>> createListOfPrefferedChampionsFunc(Long discordId) {
        return preferredChampions -> userService.createPreferredChampionsForUser(discordId.intValue(), preferredChampions);
    }

    private Function<Set<LoLChampion>, Publisher<? extends LoLChampion>> mergeListOfChampions(Long discordId) {
        return preferredChampions -> userService.mergePreferredChampionsForUser(discordId.intValue(), preferredChampions);
    }

    private Function<Set<LoLChampion>, Publisher<? extends LoLChampion>> removeListOfChampions(Long discordId) {
        return preferredChampions -> userService.removePreferredChampionsForUser(discordId.intValue(), preferredChampions);
    }

}
