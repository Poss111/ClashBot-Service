package com.poss.clash.bot.controllers;

import com.poss.clash.bot.openapi.api.TentativeApi;
import com.poss.clash.bot.openapi.model.Tentative;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class TentativeController implements TentativeApi {

    @Override
    public Mono<ResponseEntity<Flux<Tentative>>> retrieveTentativeQueues(Boolean onlyActive,
                                                                         ServerWebExchange exchange) {
        return null;
    }
}
