package com.poss.clash.bot.controllers;

import com.poss.clash.bot.exceptions.ClashBotControllerException;
import com.poss.clash.bot.openapi.api.TentativesApi;
import com.poss.clash.bot.openapi.model.*;
import com.poss.clash.bot.services.ArchivedService;
import com.poss.clash.bot.services.TentativeService;
import com.poss.clash.bot.services.UserAssignmentService;
import com.poss.clash.bot.services.UserService;
import com.poss.clash.bot.utils.TentativeMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
@Slf4j
public class TentativeController implements TentativesApi {

    private final UserAssignmentService userAssignmentService;
    private final TentativeService tentativeService;
    private final ArchivedService archivedService;
    private final UserService userService;
    private final TentativeMapper tentativeMapper;

    @Override
    public Mono<ResponseEntity<Tentative>> assignUserToATentativeQueue(String tentativeId, Long discordId, ServerWebExchange exchange) {
        return userAssignmentService.assignUserToTentativeQueue(discordId.intValue(), tentativeId)
                .map(tentativeMapper::tentativeQueueToTentative)
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<Tentative>> createTentativeQueue(Mono<TentativeRequired> tentativeRequired, ServerWebExchange exchange) {
        return tentativeRequired
                .map(this::validateTentativeRequest)
                .flatMap(tentativeRequest -> userAssignmentService.createTentativeQueueAndAssignUser(
                        tentativeRequest.getTentativePlayers().stream()
                                .map(TentativePlayer::getDiscordId)
                                .collect(Collectors.toSet()),
                        tentativeRequest.getServerId(),
                        tentativeRequest.getTournamentDetails().getTournamentName(),
                        tentativeRequest.getTournamentDetails().getTournamentDay()
                ))
                .map(tentativeMapper::tentativeQueueToTentative)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @Override
    public Mono<ResponseEntity<Tentative>> removeUserFromTentativeQueue(String tentativeId, Long discordId, ServerWebExchange exchange) {
        return userAssignmentService.findAndRemoveUserFromTentativeQueue(discordId.intValue(), tentativeId)
                .map(tentativeMapper::tentativeQueueToTentative)
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<Tentative>> retrieveTentativeQueue(String tentativeId, ServerWebExchange exchange) {
        return tentativeService.findById(tentativeId)
                .map(tentativeMapper::tentativeQueueToTentative)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @Override
    public Mono<ResponseEntity<Tentatives>> retrieveTentativeQueues(Boolean archived, Long discordId, Long serverId, String tournamentName, String tournamentDay, ServerWebExchange exchange) {
        return swapFluxBasedOnArchivedFlag(archived, discordId, serverId, tournamentName, tournamentDay)
                .collectList()
                .map(this::buildTupleOfTentativesAndSetOfDiscordIds)
                .flatMap(this::populateTupleWithTentativePlayerDetails)
                // Map Players to Tentative Players for response
                .map(tuple -> {
                    for (Tentative queue : tuple.getT1().getQueues()) {
                        for (TentativePlayer tentativePlayer : queue.getTentativePlayers()) {
                            TentativePlayer detailedPlayer = tuple.getT2().get(tentativePlayer.getDiscordId());
                            tentativePlayer.setName(detailedPlayer.getName());
                            tentativePlayer.setChampions(detailedPlayer.getChampions());
                            tentativePlayer.setRole(detailedPlayer.getRole());
                        }
                    }
                    tuple.getT1().setCount(tuple.getT1().getQueues().size());
                    return tuple.getT1();
                })
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    protected Tuple2<Tentatives, Set<Integer>> buildTupleOfTentativesAndSetOfDiscordIds(List<Tentative> list) {
        return Tuples.of(Tentatives.builder().queues(list).build(), list.stream().map(
                        Tentative::getTentativePlayers)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet())
                .stream()
                .map(TentativePlayer::getDiscordId)
                .collect(Collectors.toSet()));
    }

    protected Mono<Tuple2<Tentatives, Map<Integer, TentativePlayer>>> populateTupleWithTentativePlayerDetails(Tuple2<Tentatives, Set<Integer>> tuple) {
        return Mono.zip(Mono.just(tuple.getT1()),
                Mono.just(tuple.getT2())
                        .flatMapIterable(ids -> ids)
                        .flatMap(id -> userService.retrieveUser(id)
                                .defaultIfEmpty(Player.builder().discordId(id).build()))
                        .map(tentativeMapper::playerToTentativePlayer)
                        .collectMap(TentativePlayer::getDiscordId, Function.identity()));
    }

    protected Flux<Tentative> swapFluxBasedOnArchivedFlag(Boolean archived, Long discordId, Long serverId, String tournamentName, String tournamentDay) {
        Flux<Tentative> tentativeFlux;
        if (archived) {
            tentativeFlux = archivedService.retrieveArchivedTentativeQueues(discordId, serverId, tournamentName, tournamentDay)
                    .map(tentativeMapper::archivedTentativeQueueToTentative);
        } else {
            tentativeFlux = tentativeService.retrieveTentativeQueues(discordId, serverId, tournamentName, tournamentDay)
                    .map(tentativeMapper::tentativeQueueToTentative);
        }
        return tentativeFlux;
    }

    protected TentativeRequired validateTentativeRequest(TentativeRequired tentativePayload) {
        if (null == tentativePayload.getTentativePlayers()
                || 0 == tentativePayload.getTentativePlayers().size()
                || null == tentativePayload.getTentativePlayers().get(0).getDiscordId()) {
            throw new ClashBotControllerException("To create a Tentative Queue, you must pass a valid Tentative Player.", HttpStatus.BAD_REQUEST);
        }
        return tentativePayload;
    }
}
