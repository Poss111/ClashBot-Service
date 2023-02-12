package com.poss.clash.bot.controllers;

import com.poss.clash.bot.daos.models.LoLChampion;
import com.poss.clash.bot.enums.UserSubscription;
import com.poss.clash.bot.openapi.api.UsersApi;
import com.poss.clash.bot.openapi.model.*;
import com.poss.clash.bot.services.UserService;
import com.poss.clash.bot.utils.UserMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
@Slf4j
public class UserController implements UsersApi {

    private final UserService userService;

    private final UserMapper userMapper;

    @Override
    public Mono<ResponseEntity<Champions>> addToPreferredChampionsForUser(Long discordId, Mono<Champions> champions, ServerWebExchange exchange) {
        return champions
                .flatMapIterable(Champions::getChampions)
                .map(userMapper::championToLoLChampions)
                .collect(Collectors.toSet())
                .log()
                .flatMap(preferredChampions -> userService.mergePreferredChampionsForUser(discordId.intValue(), preferredChampions))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @Override
    public Mono<ResponseEntity<Champions>> createListOfPreferredChampionsForUser(Long discordId, Mono<Champions> champions, ServerWebExchange exchange) {
        return champions
                .flatMapIterable(Champions::getChampions)
                .map(userMapper::championToLoLChampions)
                .collect(Collectors.toSet())
                .log()
                .flatMap(preferredChampions -> userService.createPreferredChampionsForUser(discordId.intValue(), preferredChampions))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
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
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @Override
    public Mono<ResponseEntity<Player>> getUser(Integer discordId, ServerWebExchange exchange) {
        return userService.retrieveUser(discordId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @Override
    public Mono<ResponseEntity<Subscription>> isUserSubscribed(Long discordId, String subscription, ServerWebExchange exchange) {
        return userService.retrieveUser(discordId.intValue())
                .flatMapIterable(Player::getSubscriptions)
                .filter(subscription1 -> StringUtils.equals(subscription, subscription1.getKey().getValue()))
                .last()
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @Override
    public Mono<ResponseEntity<Champions>> removePreferredChampionForUser(Long discordId, String champion, ServerWebExchange exchange) {
        Set<LoLChampion> championSet = new HashSet<>();
        championSet.add(LoLChampion.builder().name(champion).build());
        return userService.removePreferredChampionsForUser(discordId.intValue(), championSet)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @Override
    public Mono<ResponseEntity<Champions>> retrieveUsersPreferredChampions(Long discordId, ServerWebExchange exchange) {
        return userService.retrieveUser(discordId.intValue())
                .map(player -> Champions.builder().champions(player.getChampions()).build())
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @Override
    public Mono<ResponseEntity<Subscription>> subscribeUser(Long discordId, String subscription, ServerWebExchange exchange) {
        return userService.toggleUserSubscription(discordId.intValue(), subscription, true)
                .map(subscriptionMap -> Subscription.builder()
                        .key(SubscriptionType.fromValue(subscription))
                        .isOn(subscriptionMap.get(UserSubscription.fromValue(subscription)))
                        .build())
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @Override
    public Mono<ResponseEntity<Subscription>> unsubscribeUser(Long discordId, String subscription, ServerWebExchange exchange) {
        return userService.toggleUserSubscription(discordId.intValue(), subscription, false)
                .map(subscriptionMap -> Subscription.builder()
                        .key(SubscriptionType.fromValue(subscription))
                        .isOn(subscriptionMap.get(UserSubscription.fromValue(subscription)))
                        .build())
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @Override
    public Mono<ResponseEntity<Player>> updateUser(Mono<CreateUserRequest> createUserRequest, ServerWebExchange exchange) {
        return createUserRequest
                .map(userMapper::createUserRequestToUser)
                .flatMap(userService::updateUser)
                .map(ResponseEntity::ok);
    }

}
