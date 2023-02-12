package com.poss.clash.bot.controllers;

import com.poss.clash.bot.openapi.api.ServersApi;
import com.poss.clash.bot.openapi.model.Servers;
import com.poss.clash.bot.services.ServerService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
@AllArgsConstructor
public class ServerController implements ServersApi {

    private final ServerService serverService;

    @Override
    public Mono<ResponseEntity<Servers>> retrieveServers(ServerWebExchange exchange) {
        return serverService.retrieveAllClashServers()
                .collectList()
                .map(servers -> Servers.builder().servers(servers).build())
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

}
