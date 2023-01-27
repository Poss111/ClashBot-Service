package com.poss.clash.bot.controllers;

import com.poss.clash.bot.openapi.api.UserApi;
import com.poss.clash.bot.openapi.model.*;
import com.poss.clash.bot.services.UserService;
import com.poss.clash.bot.utils.UserMapper;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Optional;

@RestController
@AllArgsConstructor
@Slf4j
public class UserController implements UserApi {

    UserService userService;

    UserMapper userMapper;

    @Override
    public Mono<ResponseEntity<AddToListOfPreferredChampions200Response>> addToListOfPreferredChampions(String id, Flux<String> champions, ServerWebExchange exchange) {
        return Flux.merge(userService.retrieveUser(id)
                .flatMapIterable(Player::getChampions),
                        champions)
                .flatMap(t1 -> {
                    if (null != t1.getT1().getChampions()
                            && !t1.getT1().getChampions().contains(t1.getT2().getChampionName())) {
                        t1.getT1().getChampions().add(t1.getT2().getChampionName());
                    }
                    return Mono.just(t1.getT1());
                })
    }

    @Override
    public Mono<ResponseEntity<Flux<Champion>>> createNewListOfPreferredChampions(String id, Mono<CreateNewListOfPreferredChampionsRequest> createNewListOfPreferredChampionsRequest, ServerWebExchange exchange) {
        return null;
    }

    @Override
    public Mono<ResponseEntity<Player>> createUser(Mono<CreateUserRequest> createUserRequest, ServerWebExchange exchange) {
        return createUserRequest
                .map(userMapper::createUserRequestToUser)
                .flatMap(userService::saveUser)
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<Player>> getUser(String id, ServerWebExchange exchange) {
        return userService.retrieveUser(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @Override
    public Mono<ResponseEntity<Flux<Champion>>> removeFromListOfPreferredChampions(String id, String champion, ServerWebExchange exchange) {
        return null;
    }

    @Override
    public Mono<ResponseEntity<Flux<Champion>>> retrieveListOfUserPreferredChampions(String id, ServerWebExchange exchange) {
        return null;
    }

    @Override
    public Mono<ResponseEntity<Flux<Subscription>>> retrieveUserSubscriptions(String id, ServerWebExchange exchange) {
        return null;
    }

    @Override
    public Mono<ResponseEntity<Flux<Subscription>>> subscribeUser(String id, ServerWebExchange exchange) {
        return null;
    }

    @Override
    public Mono<ResponseEntity<Flux<Subscription>>> unsubscribeUser(String id, ServerWebExchange exchange) {
        return null;
    }

    @Override
    public Mono<ResponseEntity<Player>> updateUser(Mono<CreateUserRequest> createUserRequest, ServerWebExchange exchange) {
        return null;
    }

}
