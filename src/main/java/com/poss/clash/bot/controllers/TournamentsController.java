package com.poss.clash.bot.controllers;

import com.poss.clash.bot.openapi.api.TournamentsApi;
import com.poss.clash.bot.openapi.model.Tournament;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class TournamentsController implements TournamentsApi {

    @Override
    public Mono<ResponseEntity<Flux<Tournament>>> getTournaments(String tournament, String day, ServerWebExchange exchange) {
        return null;
    }

}
