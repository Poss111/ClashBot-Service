package com.poss.clash.bot.controllers;

import com.poss.clash.bot.openapi.api.TentativeApi;
import com.poss.clash.bot.openapi.model.PlacePlayerOnTentativeRequest;
import com.poss.clash.bot.openapi.model.Tentative;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class TentativeController implements TentativeApi {

    @Override
    public Mono<ResponseEntity<Flux<Tentative>>> getTentativeDetails(Integer serverId, String tournamentName, String tournamentDay, ServerWebExchange exchange) {
        return null;
    }

    @Override
    public Mono<ResponseEntity<Tentative>> placePlayerOnTentative(Mono<PlacePlayerOnTentativeRequest> placePlayerOnTentativeRequest, ServerWebExchange exchange) {
        return null;
    }

    @Override
    public Mono<ResponseEntity<Tentative>> removePlayerFromTentative(Integer serverId, Integer discordId, String tournament, String tournamentDay, ServerWebExchange exchange) {
        return null;
    }

}
