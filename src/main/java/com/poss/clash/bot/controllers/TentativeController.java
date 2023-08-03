package com.poss.clash.bot.controllers;

import com.poss.clash.bot.daos.models.User;
import com.poss.clash.bot.exceptions.ClashBotControllerException;
import com.poss.clash.bot.openapi.api.TentativesApi;
import com.poss.clash.bot.openapi.model.Tentative;
import com.poss.clash.bot.openapi.model.TentativePlayer;
import com.poss.clash.bot.openapi.model.TentativeRequired;
import com.poss.clash.bot.openapi.model.Tentatives;
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

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.poss.clash.bot.constants.GlobalConstants.CAUSED_BY_KEY;

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
  public Mono<ResponseEntity<Tentative>> assignUserToATentativeQueue(
      String xCausedBy, String tentativeId, String discordId, ServerWebExchange exchange
  ) {
    return userAssignmentService
        .assignUserToTentativeQueue(discordId, tentativeId)
        .map(tentativeMapper::tentativeQueueToTentative)
        .map(ResponseEntity::ok)
        .contextWrite(ctx -> ctx.put(CAUSED_BY_KEY, xCausedBy));
  }

  @Override
  public Mono<ResponseEntity<Tentative>> createTentativeQueue(
      String xCausedBy, Mono<TentativeRequired> tentativeRequired, ServerWebExchange exchange
  ) {
    return tentativeRequired
        .map(this::validateTentativeRequest)
        .flatMap(tentativeRequest -> userAssignmentService.createTentativeQueueAndAssignUser(
            tentativeRequest
                .getTentativePlayers()
                .stream()
                .map(TentativePlayer::getDiscordId)
                .collect(Collectors.toSet()),
            tentativeRequest.getServerId(),
            tentativeRequest
                .getTournamentDetails()
                .getTournamentName(),
            tentativeRequest
                .getTournamentDetails()
                .getTournamentDay()
        ))
        .map(tentativeMapper::tentativeQueueToTentative)
        .map(ResponseEntity::ok)
        .defaultIfEmpty(ResponseEntity
                            .notFound()
                            .build())
        .contextWrite(ctx -> ctx.put(CAUSED_BY_KEY, xCausedBy));
  }

  @Override
  public Mono<ResponseEntity<Tentative>> removeUserFromTentativeQueue(
      String xCausedBy, String tentativeId, String discordId, ServerWebExchange exchange
  ) {
    return userAssignmentService
        .findAndRemoveUserFromTentativeQueue(discordId, tentativeId)
        .map(tentativeMapper::tentativeQueueToTentative)
        .map(ResponseEntity::ok)
        .contextWrite(ctx -> ctx.put(CAUSED_BY_KEY, xCausedBy));
  }

  @Override
  public Mono<ResponseEntity<Tentative>> retrieveTentativeQueue(
      String xCausedBy, String tentativeId, ServerWebExchange exchange
  ) {
    return tentativeService
        .findById(tentativeId)
        .map(tentativeMapper::tentativeQueueToTentative)
        .map(ResponseEntity::ok)
        .defaultIfEmpty(ResponseEntity
                            .notFound()
                            .build())
        .contextWrite(ctx -> ctx.put(CAUSED_BY_KEY, xCausedBy));
  }

  @Override
  public Mono<ResponseEntity<Tentatives>> retrieveTentativeQueues(
      String xCausedBy, Boolean archived, String discordId, String serverId, String tournamentName,
      String tournamentDay, ServerWebExchange exchange
  ) {
    return swapFluxBasedOnArchivedFlag(archived, discordId, serverId, tournamentName, tournamentDay)
        .collectList()
        .map(this::buildTupleOfTentativesAndSetOfDiscordIds)
        .flatMap(this::populateTupleWithTentativePlayerDetails)
        // Map Players to Tentative Players for response
        .map(tuple -> {
          for (Tentative queue : tuple
              .getT1()
              .getQueues()) {
            if (null != queue.getTentativePlayers()) {
              for (TentativePlayer tentativePlayer : queue.getTentativePlayers()) {
                TentativePlayer detailedPlayer = tuple
                    .getT2()
                    .get(tentativePlayer.getDiscordId());
                tentativePlayer.setName(detailedPlayer.getName());
                tentativePlayer.setChampions(detailedPlayer.getChampions());
                tentativePlayer.setRole(detailedPlayer.getRole());
              }
            }
          }
          tuple
              .getT1()
              .setCount(tuple
                            .getT1()
                            .getQueues()
                            .size());
          return tuple.getT1();
        })
        .map(ResponseEntity::ok)
        .defaultIfEmpty(ResponseEntity
                            .notFound()
                            .build())
        .contextWrite(ctx -> ctx.put(CAUSED_BY_KEY, xCausedBy));
  }


  protected Tuple2<Tentatives, Set<String>> buildTupleOfTentativesAndSetOfDiscordIds(List<Tentative> list) {
    Stream<List<TentativePlayer>> listOfTentativePlayers = list
        .stream()
        .map(
            Tentative::getTentativePlayers);
    return Tuples.of(Tentatives
                         .builder()
                         .queues(list)
                         .build(), listOfTentativePlayers
                         .filter(Objects::nonNull)
                         .flatMap(Collection::stream)
                         .collect(Collectors.toSet())
                         .stream()
                         .map(TentativePlayer::getDiscordId)
                         .collect(Collectors.toSet()));
  }

  protected Mono<Tuple2<Tentatives, Map<String, TentativePlayer>>> populateTupleWithTentativePlayerDetails(
      Tuple2<Tentatives, Set<String>> tuple
  ) {
    return Mono.zip(Mono.just(tuple.getT1()),
                    Mono
                        .just(tuple.getT2())
                        .flatMapIterable(ids -> ids)
                        .flatMap(id -> userService
                            .retrieveUser(id)
                            .defaultIfEmpty(User
                                                .builder()
                                                .discordId(id)
                                                .build()))
                        .map(tentativeMapper::userToTentativePlayer)
                        .collectMap(TentativePlayer::getDiscordId, Function.identity()));
  }

  protected Flux<Tentative> swapFluxBasedOnArchivedFlag(
      Boolean archived, String discordId, String serverId, String tournamentName, String tournamentDay
  ) {
    Flux<Tentative> tentativeFlux;
    if (archived) {
      tentativeFlux = archivedService
          .retrieveArchivedTentativeQueues(discordId, serverId, tournamentName, tournamentDay)
          .map(tentativeMapper::archivedTentativeQueueToTentative);
    } else {
      tentativeFlux = tentativeService
          .retrieveTentativeQueues(discordId, serverId, tournamentName, tournamentDay)
          .map(tentativeMapper::tentativeQueueToTentative);
    }
    return tentativeFlux;
  }

  protected TentativeRequired validateTentativeRequest(TentativeRequired tentativePayload) {
    if (null == tentativePayload.getTentativePlayers()
        || 0 == tentativePayload
        .getTentativePlayers()
        .size()
        || null == tentativePayload
        .getTentativePlayers()
        .get(0)
        .getDiscordId()) {
      throw new ClashBotControllerException("To create a Tentative Queue, you must pass a valid Tentative Player.",
                                            HttpStatus.BAD_REQUEST);
    }
    return tentativePayload;
  }

}
