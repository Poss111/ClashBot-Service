package com.poss.clash.bot.controllers;

import com.poss.clash.bot.openapi.api.TentativeApi;
import com.poss.clash.bot.openapi.model.Tentative;
import com.poss.clash.bot.openapi.model.TentativePlayer;
import com.poss.clash.bot.openapi.model.Tentatives;
import com.poss.clash.bot.services.TentativeService;
import com.poss.clash.bot.services.UserService;
import com.poss.clash.bot.utils.TentativeMapper;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
public class TentativeController implements TentativeApi {

    private final TentativeService tentativeService;
    private final UserService userService;
    private final TentativeMapper tentativeMapper;

    @Override
    public Mono<ResponseEntity<Tentatives>> retrieveTentativeQueues(Boolean onlyActive,
                                                                    ServerWebExchange exchange) {
        return tentativeService.retrieveTentativeQueues()
                .collectList()
                .map(list -> Tuples.of(Tentatives.builder().queues(list).build(), list.stream().map(
                        Tentative::getTentativePlayers)
                        .flatMap(Collection::stream)
                        .collect(Collectors.toSet())
                        .stream()
                        .map(TentativePlayer::getDiscordId)
                        .collect(Collectors.toSet())))
                .flatMap(tuple -> Mono.zip(Mono.just(tuple.getT1()),
                                           Mono.just(tuple.getT2())
                                                .flatMapIterable(ids -> ids)
                                                .flatMap(userService::retrieveUser)
                                                   .map(tentativeMapper::playerToTentativePlayer)
                                                   .collectMap(TentativePlayer::getDiscordId, Function.identity())))
                // Map Players to Tentative Players for response
                .map(tuple -> {
                    for (Tentative queue : tuple.getT1().getQueues()) {
                        for (TentativePlayer tentativePlayer : queue.getTentativePlayers()) {
                            TentativePlayer detailedPlayer = tuple.getT2().get(tentativePlayer.getDiscordId());
                            tentativePlayer.setName(detailedPlayer.getName());
                            tentativePlayer.setChampions(detailedPlayer.getChampions());
                        }
                    }
                    return tuple.getT1();
                })
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

}
