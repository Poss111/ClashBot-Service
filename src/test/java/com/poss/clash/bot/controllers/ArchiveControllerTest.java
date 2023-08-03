package com.poss.clash.bot.controllers;

import com.poss.clash.bot.ClashBotTestingConfig;
import com.poss.clash.bot.daos.models.ArchivedClashTeam;
import com.poss.clash.bot.daos.models.ArchivedClashTournament;
import com.poss.clash.bot.daos.models.ArchivedTentativeQueue;
import com.poss.clash.bot.openapi.model.ArchiveMetadata;
import com.poss.clash.bot.services.ArchivedService;
import com.poss.clash.bot.services.models.ArchiveResults;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.publisher.PublisherProbe;

import java.util.List;

import static ch.qos.logback.core.CoreConstants.CAUSED_BY;
import static org.mockito.Mockito.when;

@ExtendWith({SpringExtension.class})
@Import(ClashBotTestingConfig.class)
class ArchiveControllerTest {

  @InjectMocks
  ArchiveController archiveController;

  @Mock
  ArchivedService archivedService;

  @Autowired
  EasyRandom easyRandom;

  @Test
  @DisplayName("200 - archiveTeamsAndTentativeQueues - should invoke the archive process and return a list of Archived Teams")
  void test1() {
    String xCausedBy = "12345";
    ArchiveResults archiveResults = ArchiveResults
        .builder()
        .teamsArchived(List.of(easyRandom.nextObject(ArchivedClashTeam.class)))
        .tentativeQueues(List.of(easyRandom.nextObject(ArchivedTentativeQueue.class)))
        .inactiveTournaments(List.of(easyRandom.nextObject(ArchivedClashTournament.class)))
        .build();
    ArchiveMetadata response = ArchiveMetadata
        .builder()
        .teamsMoved(archiveResults
                        .getTeamsArchived()
                        .size())
        .tentativeQueuesMoved(archiveResults
                                  .getTentativeQueues()
                                  .size())
        .build();

    PublisherProbe<ArchiveResults> archiveResultsPublisherProbe = PublisherProbe.of(Mono.just(archiveResults));
    when(archivedService.archiveBasedOnInactiveTournaments())
        .thenReturn(archiveResultsPublisherProbe.mono());

    StepVerifier
        .create(archiveController.archiveTeamsAndTentativeQueues(xCausedBy, null))
        .expectAccessibleContext()
        .contains(CAUSED_BY, xCausedBy)
        .then()
        .expectNext(ResponseEntity.ok(response))
        .verifyComplete();

    archiveResultsPublisherProbe.assertWasSubscribed();
  }

}
