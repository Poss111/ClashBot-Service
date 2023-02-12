package com.poss.clash.bot.controllers;

import com.poss.clash.bot.daos.TentativeDao;
import com.poss.clash.bot.exceptions.ClashBotControllerException;
import com.poss.clash.bot.openapi.api.TentativesApi;
import com.poss.clash.bot.openapi.model.*;
import com.poss.clash.bot.services.TentativeService;
import com.poss.clash.bot.services.TournamentService;
import com.poss.clash.bot.services.UserService;
import com.poss.clash.bot.utils.TentativeMapper;
import com.poss.clash.bot.utils.TournamentMapper;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
public class TentativeController implements TentativesApi {

    private final TournamentService tournamentService;
    private final TentativeService tentativeService;
    private final UserService userService;
    private final TentativeMapper tentativeMapper;
    private final TournamentMapper tournamentMapper;
    private final TentativeDao tentativeDao;

    @Override
    public Mono<ResponseEntity<Tentative>> assignUserToATentativeQueue(String tentativeId, Long discordId, ServerWebExchange exchange) {
        return null;
    }

    @Override
    public Mono<ResponseEntity<Tentative>> createTentativeQueue(Mono<TentativeRequired> tentativeRequired, ServerWebExchange exchange) {
        return Mono.zip(tentativeRequired,
                tentativeRequired
                        .flatMap(createQueueDetails -> tentativeService.retrieveTentativeQueues(
                                createQueueDetails.getServerId(),
                                createQueueDetails.getTournamentDetails().getTournamentName(),
                                createQueueDetails.getTournamentDetails().getTournamentDay())
                                .defaultIfEmpty(Tentative.builder().build())
                                .last()
                                .map(tentative -> {
                                    if (null != tentative.getId()) {
                                        throw new ClashBotControllerException(
                                                MessageFormat.format("Tentative queue exists for Server {0} and Tournament {1}",
                                                        tentative.getServerId(),
                                                        tentative.getTournamentDetails()),
                                                HttpStatus.BAD_REQUEST);
                                    } else {
                                        return tentative;
                                    }
                                })))
                .map(tuple -> tentativeMapper.tentativeRequiredToTentativeQueue(tuple.getT1()))
                .flatMap(tentativeService::saveTentativeQueue)
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<Team>> removeUserFromTentativeQueue(String tentativeId, Long discordId, ServerWebExchange exchange) {
        return null;
    }

    @Override
    public Mono<ResponseEntity<Tentative>> retrieveTentativeQueue(String tentativeId, ServerWebExchange exchange) {
        return null;
    }

    @Override
    public Mono<ResponseEntity<Tentatives>> retrieveTentativeQueues(Boolean inactive, Long discordId, Long serverId, String tournamentName, String tournamentDay, ServerWebExchange exchange) {
        return swapFluxBasedOnActiveFlag(inactive)
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

    protected Flux<Tentative> swapFluxBasedOnActiveFlag(Boolean onlyActive) {
        Flux<Tentative> tentativeFlux;
        if (!onlyActive) {
            tentativeFlux = Mono.defer(() -> tournamentService.retrieveAllTournaments(true)
                            .map(tournamentMapper::detailedTournamentToBaseTournament)
                            .collect(Collectors.toSet()))
                    .flatMapMany(upcomingTournaments -> tentativeService.retrieveTentativeQueues().map(tentative -> Tuples.of(tentative, upcomingTournaments)))
                    .filter(tentativeRecord -> tentativeRecord.getT2().contains(tentativeRecord.getT1().getTournamentDetails()))
                    .map(Tuple2::getT1);
        } else {
            tentativeFlux = tentativeService.retrieveTentativeQueues();
        }
        return tentativeFlux;
    }
}
