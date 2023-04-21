package com.poss.clash.bot.utils;

import com.poss.clash.bot.daos.models.BasePlayerRecord;
import com.poss.clash.bot.daos.models.ClashTeam;
import com.poss.clash.bot.daos.models.TeamId;
import com.poss.clash.bot.daos.models.TournamentId;
import com.poss.clash.bot.openapi.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TeamMapperTest {

    TeamMapper teamMapper = Mappers.getMapper(TeamMapper.class);

    @Test
    @DisplayName("ClashTeam -> Team")
    void test_clashTeamToTeam() {
        String teamId = "ct-1234";
        String awesomeTeam = "Awesome Team";
        HashMap<Role, BasePlayerRecord> positions = new HashMap<>();
        String discordId = "1";
        String discordIdTwo = "2";
        String discordIdThree = "3";
        String discordIdFour = "4";
        String discordIdFive = "5";
        positions.put(Role.TOP, BasePlayerRecord.builder()
                .discordId(discordId)
                .build());
        positions.put(Role.JG, BasePlayerRecord.builder()
                .discordId(discordIdTwo)
                .build());
        positions.put(Role.MID, BasePlayerRecord.builder()
                .discordId(discordIdThree)
                .build());
        positions.put(Role.BOT, BasePlayerRecord.builder()
                .discordId(discordIdFour)
                .build());
        positions.put(Role.SUPP, BasePlayerRecord.builder()
                .discordId(discordIdFive)
                .build());

        TournamentId tournamentId = TournamentId.builder()
                .tournamentName("awesome_sauce")
                .tournamentDay("1")
                .build();
        String serverId = "1234";
        ClashTeam clashTeamEntity = ClashTeam.builder()
                .teamId(TeamId.builder()
                        .id(teamId)
                        .tournamentId(tournamentId)
                        .build())
                .teamName(awesomeTeam)
                .serverId(serverId)
                .positions(positions)
                .build();

        Team expectedTeamObject = Team.builder()
                .id(teamId)
                .tournament(BaseTournament.builder()
                        .tournamentName(tournamentId.getTournamentName())
                        .tournamentDay(tournamentId.getTournamentDay())
                        .build())
                .serverId(serverId)
                .playerDetails(TeamPlayerDetails.builder()
                        .top(TeamPlayer.builder().discordId(discordId).build())
                        .jg(TeamPlayer.builder().discordId(discordIdTwo).build())
                        .mid(TeamPlayer.builder().discordId(discordIdThree).build())
                        .bot(TeamPlayer.builder().discordId(discordIdFour).build())
                        .supp(TeamPlayer.builder().discordId(discordIdFive).build())
                        .build())
                .name(awesomeTeam)
                .build();

        assertEquals(expectedTeamObject, teamMapper.clashTeamToTeam(clashTeamEntity));
    }

}
