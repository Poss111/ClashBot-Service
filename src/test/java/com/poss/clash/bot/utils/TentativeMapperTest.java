package com.poss.clash.bot.utils;

import com.poss.clash.bot.ClashBotTestingConfig;
import com.poss.clash.bot.daos.models.TentativeId;
import com.poss.clash.bot.daos.models.TentativeQueue;
import com.poss.clash.bot.daos.models.TournamentId;
import com.poss.clash.bot.daos.models.User;
import com.poss.clash.bot.openapi.model.BaseTournament;
import com.poss.clash.bot.openapi.model.Champion;
import com.poss.clash.bot.openapi.model.Tentative;
import com.poss.clash.bot.openapi.model.TentativePlayer;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@Import(ClashBotTestingConfig.class)
class TentativeMapperTest {

  TentativeMapper tentativeMapper = Mappers.getMapper(TentativeMapper.class);

  @Autowired
  EasyRandom easyRandom;

  @Test
  @DisplayName("TentativeMapper - Tentative -> TentativeQueue")
  void test_tentativeToTentativeQueue() {
    String expectedTournamentName = "awesome_sauce";
    String expectedTournamentDay = "3";
    String serverId = "1234";
    List<TentativePlayer> expectedTentativeQueue = new ArrayList<>();
    expectedTentativeQueue.add(TentativePlayer
                                   .builder()
                                   .discordId("1")
                                   .build());
    expectedTentativeQueue.add(TentativePlayer
                                   .builder()
                                   .discordId("2")
                                   .build());
    expectedTentativeQueue.add(TentativePlayer
                                   .builder()
                                   .discordId("3")
                                   .build());
    Tentative source = Tentative
        .builder()
        .tentativePlayers(expectedTentativeQueue)
        .serverId(serverId)
        .tournamentDetails(BaseTournament
                               .builder()
                               .tournamentName(expectedTournamentName)
                               .tournamentDay(expectedTournamentDay)
                               .build())
        .build();

    TentativeQueue target = TentativeQueue
        .builder()
        .tentativeId(
            TentativeId
                .builder()
                .serverId(serverId)
                .tournamentId(TournamentId
                                  .builder()
                                  .tournamentName(expectedTournamentName)
                                  .tournamentDay(expectedTournamentDay)
                                  .build())
                .build()
        )
        .discordIds(
            expectedTentativeQueue
                .stream()
                .map(TentativePlayer::getDiscordId)
                .collect(Collectors.toSet()))
        .build();

    assertEquals(target, tentativeMapper.tentativeToTentativeQueue(source));
  }

  @Test
  @DisplayName("TentativeMapper - TentativeQueue -> Tentative")
  void test_tentativeQueueToTentative() {
    String expectedTournamentName = "awesome_sauce";
    String expectedTournamentDay = "3";
    String serverId = "1234";
    List<TentativePlayer> expectedTentativeQueue = new ArrayList<>();
    expectedTentativeQueue.add(TentativePlayer
                                   .builder()
                                   .discordId("1")
                                   .build());
    expectedTentativeQueue.add(TentativePlayer
                                   .builder()
                                   .discordId("2")
                                   .build());
    expectedTentativeQueue.add(TentativePlayer
                                   .builder()
                                   .discordId("3")
                                   .build());
    Tentative target = Tentative
        .builder()
        .tentativePlayers(expectedTentativeQueue)
        .serverId(serverId)
        .tournamentDetails(BaseTournament
                               .builder()
                               .tournamentName(expectedTournamentName)
                               .tournamentDay(expectedTournamentDay)
                               .build())
        .build();

    TentativeQueue source = TentativeQueue
        .builder()
        .tentativeId(
            TentativeId
                .builder()
                .serverId(serverId)
                .tournamentId(TournamentId
                                  .builder()
                                  .tournamentName(expectedTournamentName)
                                  .tournamentDay(expectedTournamentDay)
                                  .build())
                .build()
        )
        .discordIds(
            expectedTentativeQueue
                .stream()
                .map(TentativePlayer::getDiscordId)
                .collect(Collectors.toSet()))
        .build();

    assertEquals(target, tentativeMapper.tentativeQueueToTentative(source));
  }

  @Test
  @DisplayName("User -> TentativePlayer")
  void test_userToTentativePlayer() {
    User user = easyRandom.nextObject(User.class);

    TentativePlayer tentativePlayer = TentativePlayer
        .builder()
        .name(user.getName())
        .discordId(user.getDiscordId())
        .role(user.getDefaultRole())
        .champions(user
                       .getPreferredChampions()
                       .stream()
                       .map(champ -> Champion
                           .builder()
                           .name(champ.getName())
                           .build())
                       .collect(Collectors.toList()))
        .build();

    assertEquals(tentativePlayer, tentativeMapper.userToTentativePlayer(user));
  }

}
