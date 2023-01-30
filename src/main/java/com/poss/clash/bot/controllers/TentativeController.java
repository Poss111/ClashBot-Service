package com.poss.clash.bot.controllers;

import com.poss.clash.bot.openapi.api.TentativeApi;
import com.poss.clash.bot.openapi.model.Tentatives;
import com.poss.clash.bot.services.TentativeService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
@AllArgsConstructor
public class TentativeController implements TentativeApi {

    private final TentativeService tentativeService;

    @Override
    public Mono<ResponseEntity<Tentatives>> retrieveTentativeQueues(Boolean onlyActive,
                                                                    ServerWebExchange exchange) {
        return tentativeService.retrieveTentativeQueues()
                .collectList()
                .map(list -> Tentatives.builder().queues(list).build())
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

}
