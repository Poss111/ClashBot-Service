package com.poss.clash.bot.controllers;

import com.poss.clash.bot.openapi.api.ArchiveApi;
import com.poss.clash.bot.openapi.model.ArchiveMetadata;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@RestController
public class ArchiveController implements ArchiveApi {

    @Override
    public Mono<ResponseEntity<ArchiveMetadata>> archiveTeamsAndTentativeQueues(String xCausedBy, ServerWebExchange exchange) {
        return null;
    }

}
