package com.poss.clash.bot.services;

import com.poss.clash.bot.ClashBotTestingConfig;
import com.poss.clash.bot.daos.models.ClashTournament;
import com.poss.clash.bot.services.api.RiotApiService;
import com.poss.clash.bot.services.models.RiotClashTournament;
import com.poss.clash.bot.utils.TournamentMapper;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.publisher.PublisherProbe;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@Import(ClashBotTestingConfig.class)
public class RiotServiceTest {

  @InjectMocks
  RiotService riotService;

  @Mock
  RiotApiService riotApiService;

  @Mock
  TournamentService tournamentService;

  @Spy
  TournamentMapper tournamentMapper = Mappers.getMapper(TournamentMapper.class);

  @Autowired
  EasyRandom easyRandom;

  @Nested
  class RetrieveAndPersistTourneys {

    @Test
    @DisplayName("Should retrieve tournaments from Riot and persist them into the list of available tournaments.")
    void test_retrieveAndPersistTournaments() {
      RiotClashTournament riotClashTournament = easyRandom.nextObject(RiotClashTournament.class);
      ClashTournament mappedClashTournament = tournamentMapper.riotClashTournamentToClashTournament(
          riotClashTournament);
      when(riotApiService.retrieveClashTournaments())
          .thenReturn(Flux.just(riotClashTournament));
      PublisherProbe<ClashTournament> clashTournamentProbe = PublisherProbe.of(Mono.just(mappedClashTournament));
      when(tournamentService.saveTournament(mappedClashTournament))
          .thenReturn(clashTournamentProbe.mono());

      StepVerifier
          .create(riotService.retrieveAndPersistTournaments())
          .recordWith(ArrayList::new)
          .expectNextCount(1)
          .consumeRecordedWith(tournaments -> assertEquals(List.of(mappedClashTournament), tournaments))
          .verifyComplete();

      clashTournamentProbe.assertWasSubscribed();
      assertEquals(1, clashTournamentProbe.subscribeCount());
    }

  }

}
