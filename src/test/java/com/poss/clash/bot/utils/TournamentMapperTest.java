package com.poss.clash.bot.utils;

import com.poss.clash.bot.ClashBotTestingConfig;
import com.poss.clash.bot.daos.models.ClashTournament;
import com.poss.clash.bot.daos.models.TournamentId;
import com.poss.clash.bot.services.models.RiotClashTournament;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@Import(ClashBotTestingConfig.class)
public class TournamentMapperTest {

  TournamentMapper tournamentMapper = Mappers.getMapper(TournamentMapper.class);

  @Autowired
  EasyRandom easyRandom;

  @Test
  @DisplayName("RiotClashTournament -> ClashTournament")
  void test_riotClashTournamentToClashTournament() {
    RiotClashTournament riotClashTournament = easyRandom.nextObject(RiotClashTournament.class);

    ClashTournament mappedClashTournament = ClashTournament
        .builder()
        .tournamentId(
            TournamentId
                .builder()
                .tournamentName(riotClashTournament.getNameKey())
                .tournamentDay(riotClashTournament.getNameKeySecondary())
                .build()
        )
        .registrationTime(
            Instant.ofEpochMilli(riotClashTournament
                                     .getSchedule()
                                     .get(0)
                                     .getRegistrationTime())
        )
        .startTime(
            Instant.ofEpochMilli(riotClashTournament
                                     .getSchedule()
                                     .get(0)
                                     .getStartTime())
        )
        .build();

    assertEquals(mappedClashTournament, tournamentMapper.riotClashTournamentToClashTournament(riotClashTournament));
  }

}
