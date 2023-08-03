package com.poss.clash.bot.source;

import com.poss.clash.bot.ClashBotTestingConfig;
import com.poss.clash.bot.openapi.model.*;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.StepVerifierOptions;
import reactor.util.context.Context;
import reactor.util.function.Tuple4;
import reactor.util.function.Tuples;

import java.text.MessageFormat;
import java.util.List;
import java.util.stream.Stream;

import static com.poss.clash.bot.constants.GlobalConstants.CAUSED_BY_KEY;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@Import(ClashBotTestingConfig.class)
public class TeamSourceTest {

  @InjectMocks
  TeamSource teamSource;

  @Autowired
  EasyRandom easyRandom;

  @Nested
  @DisplayName("Team Events")
  class TeamEvents {

    @TestFactory
    Stream<DynamicTest> testTeamEvents() {
      Team team = easyRandom.nextObject(Team.class);
      team
          .getPlayerDetails()
          .getTop()
          .setChampions(List.of());
      team
          .getPlayerDetails()
          .getJg()
          .setChampions(List.of());
      team
          .getPlayerDetails()
          .getMid()
          .setChampions(List.of());
      team
          .getPlayerDetails()
          .getBot()
          .setChampions(List.of());
      team
          .getPlayerDetails()
          .getSupp()
          .setChampions(List.of());
      String causedById = easyRandom.nextObject(String.class);

      List<Tuple4<String, EventType, Team, String>> testInput = List.of(
          Tuples.of(causedById, EventType.JOINED, team, causedById + " joined Team " + team.getName()),
          Tuples.of(causedById, EventType.UPDATED, team, causedById + " updated Team " + team.getName()),
          Tuples.of(causedById, EventType.CREATED, team, causedById + " created Team " + team.getName()),
          Tuples.of(causedById, EventType.REMOVED, team, causedById + " removed Team " + team.getName())
      );

      return testInput
          .stream()
          .map(dom -> DynamicTest.dynamicTest(MessageFormat.format(
                                                  "Test event publishing for Team with eventType of {0}",
                                                  dom.getT2()),
                                              () -> {
                                                Team inputTeam = dom.getT3();
                                                var event = Event
                                                    .builder()
                                                    .teamEvent(TeamEvent
                                                                   .builder()
                                                                   .eventType(dom.getT2())
                                                                   .team(inputTeam)
                                                                   .build())
                                                    .summary(dom.getT4())
                                                    .serverId(inputTeam.getServerId())
                                                    .causedBy(causedById)
                                                    .build();

                                                Mono<Event> publisher = null;
                                                switch (dom.getT2()) {
                                                  case JOINED:
                                                    publisher = teamSource.sendTeamJoinedEvent(inputTeam);
                                                    break;
                                                  case CREATED:
                                                    publisher = teamSource.sendTeamCreateEvent(inputTeam);
                                                    break;
                                                  case REMOVED:
                                                    publisher = teamSource.sendTeamRemovedEvent(inputTeam);
                                                    break;
                                                  case UPDATED:
                                                    publisher = teamSource.sendTeamUpdateEvent(inputTeam);
                                                    break;
                                                  default:
                                                    fail("Event type not supported.");
                                                }
                                                StepVerifier
                                                    .create(publisher,
                                                            StepVerifierOptions
                                                                .create()
                                                                .withInitialContext(
                                                                    Context.of(CAUSED_BY_KEY, dom.getT1())))
                                                    .expectNextMatches(eventPublished -> {
                                                      assertNotNull(eventPublished.getId());
                                                      assertEquals(inputTeam, event
                                                          .getTeamEvent()
                                                          .getTeam());
                                                      assertEquals(event.getTeamEvent(), eventPublished.getTeamEvent());
                                                      assertEquals(event.getCausedBy(), eventPublished.getCausedBy());
                                                      assertEquals(event.getSummary(), eventPublished.getSummary());
                                                      return true;
                                                    })
                                                    .verifyComplete();
                                              }));
    }

  }

  @Nested
  @DisplayName("Tentative Events")
  class TentativeEvents {

    @TestFactory
    Stream<DynamicTest> testTentativeEvents() {
      Tentative tentative = Tentative
          .builder()
          .id("12345")
          .tournamentDetails(easyRandom.nextObject(BaseTournament.class))
          .build();
      String causedById = easyRandom.nextObject(String.class);

      List<Tuple4<String, EventType, Tentative, String>> testInput = List.of(
          Tuples.of(causedById, EventType.JOINED, tentative, causedById + " joined Tentative Queue for " + tentative
              .getTournamentDetails()
              .getTournamentName() + " " + tentative
                                                                 .getTournamentDetails()
                                                                 .getTournamentDay()),
          Tuples.of(causedById, EventType.CREATED, tentative, causedById + " created Tentative Queue for " + tentative
              .getTournamentDetails()
              .getTournamentName() + " " + tentative
                                                                  .getTournamentDetails()
                                                                  .getTournamentDay()),
          Tuples.of(causedById, EventType.REMOVED, tentative, causedById + " removed from Tentative Queue for " +
                                                              tentative
                                                                  .getTournamentDetails()
                                                                  .getTournamentName() + " " + tentative
                                                                  .getTournamentDetails()
                                                                  .getTournamentDay())
      );

      return testInput
          .stream()
          .map(dom -> DynamicTest.dynamicTest(MessageFormat.format(
                                                  "Test event publishing for Tentative with eventType of {0}",
                                                  dom.getT2()),
                                              () -> {
                                                Tentative inputTentative = dom.getT3();
                                                var event = Event
                                                    .builder()
                                                    .teamEvent(TeamEvent
                                                                   .builder()
                                                                   .eventType(dom.getT2())
                                                                   .tentative(inputTentative)
                                                                   .build())
                                                    .summary(dom.getT4())
                                                    .serverId(inputTentative.getServerId())
                                                    .causedBy(causedById)
                                                    .build();

                                                Mono<Event> publisher = null;
                                                switch (dom.getT2()) {
                                                  case JOINED:
                                                    publisher = teamSource.sendTentativeQueueJoinedEvent(
                                                        inputTentative);
                                                    break;
                                                  case CREATED:
                                                    publisher = teamSource.sendTentativeQueueCreateEvent(
                                                        inputTentative);
                                                    break;
                                                  case REMOVED:
                                                    publisher = teamSource.sendTentativeQueueRemovedEvent(
                                                        inputTentative);
                                                    break;
                                                  default:
                                                    fail("Event type not supported.");
                                                }
                                                StepVerifier
                                                    .create(publisher,
                                                            StepVerifierOptions
                                                                .create()
                                                                .withInitialContext(
                                                                    Context.of(CAUSED_BY_KEY, dom.getT1())))
                                                    .expectNextMatches(eventPublished -> {
                                                      assertNotNull(eventPublished.getId());
                                                      assertEquals(inputTentative, event
                                                          .getTeamEvent()
                                                          .getTentative());
                                                      assertEquals(event.getTeamEvent(), eventPublished.getTeamEvent());
                                                      assertEquals(event.getCausedBy(), eventPublished.getCausedBy());
                                                      assertEquals(event.getSummary(), eventPublished.getSummary());
                                                      return true;
                                                    })
                                                    .verifyComplete();
                                              }));
    }

  }

}
