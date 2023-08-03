package com.poss.clash.bot.services.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.poss.clash.bot.ClashBotTestingConfig;
import com.poss.clash.bot.exceptions.ClashBotDependentApiException;
import com.poss.clash.bot.services.models.RiotClashTournament;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockserver.integration.ClientAndServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.matchers.Times.exactly;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@ExtendWith(SpringExtension.class)
@Import(ClashBotTestingConfig.class)
public class RiotApiServiceTest {

  RiotApiService riotService;

  @Autowired
  EasyRandom easyRandom;

  static ClientAndServer mockServer;

  @BeforeAll
  static void beforeAll() {
    mockServer = startClientAndServer(8090);
  }

  @BeforeEach
  void before() {
    riotService = new RiotApiService(WebClient
                                         .builder()
                                         .baseUrl("http://" + mockServer
                                             .remoteAddress()
                                             .getHostName() + ":" + mockServer.getPort())
                                         .build());
  }

  @AfterAll
  static void afterAll() {
    mockServer.stop();
  }

  @Nested
  class RetrieveTournaments {

    private final String path = "/lol/clash/v1/tournaments";
    private final String method = "GET";

    @Test
    @DisplayName("retrieveClashTournaments - " + method + " " + path +
                 " - should retrieve upcoming Tournaments and parse them into ClashTournaments")
    void test_retrieveClashTournaments() throws JsonProcessingException {
      RiotClashTournament riotClashTournament = easyRandom.nextObject(RiotClashTournament.class);
      List<RiotClashTournament> tournaments = List.of(riotClashTournament);

      ObjectMapper objectMapper = new ObjectMapper();

      mockServer
          .when(
              request()
                  .withMethod(method)
                  .withPath(path),
              exactly(1))
          .respond(
              response()
                  .withStatusCode(200)
                  .withHeader("Content-type", "application/json")
                  .withBody(objectMapper.writeValueAsString(tournaments))
                  .withDelay(TimeUnit.SECONDS, 1)
          );

      StepVerifier
          .create(riotService.retrieveClashTournaments())
          .recordWith(ArrayList::new)
          .expectNextCount(1)
          .consumeRecordedWith(list -> assertEquals(tournaments, list))
          .verifyComplete();
    }

    @Test
    @DisplayName("retrieveClashTournaments - " + method + " " + path +
                 " (any exception) - should return with an appropriate exception.")
    void test_retrieveClashTournaments_ifAnExceptionIsReturned() {
      mockServer
          .when(
              request()
                  .withMethod(method)
                  .withPath(path),
              exactly(1))
          .respond(
              response()
                  .withStatusCode(401)
                  .withHeader("Content-type", "application/json")
                  .withBody("{ \"status\": {\"message\": \"Forbidden\", \"status_code\": 403 } }")
                  .withDelay(TimeUnit.SECONDS, 1)
          );

      StepVerifier
          .create(riotService.retrieveClashTournaments())
          .expectError(ClashBotDependentApiException.class)
          .verify();
    }

  }

}
