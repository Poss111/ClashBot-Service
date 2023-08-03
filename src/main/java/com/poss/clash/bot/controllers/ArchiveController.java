package com.poss.clash.bot.controllers;

import com.poss.clash.bot.openapi.api.ArchiveApi;
import com.poss.clash.bot.openapi.model.ArchiveMetadata;
import com.poss.clash.bot.services.ArchivedService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import static ch.qos.logback.core.CoreConstants.CAUSED_BY;

@AllArgsConstructor
@RestController
public class ArchiveController implements ArchiveApi {

  private final ArchivedService archivedService;

  @Override
  public Mono<ResponseEntity<ArchiveMetadata>> archiveTeamsAndTentativeQueues(
      String xCausedBy, ServerWebExchange exchange
  ) {
    return archivedService
        .archiveBasedOnInactiveTournaments()
        .map(archiveResults -> ArchiveMetadata
            .builder()
            .teamsMoved(archiveResults
                            .getTeamsArchived()
                            .size())
            .tentativeQueuesMoved(archiveResults
                                      .getTentativeQueues()
                                      .size())
            .build())
        .map(ResponseEntity::ok)
        .contextWrite(Context.of(CAUSED_BY, xCausedBy));
  }

}
