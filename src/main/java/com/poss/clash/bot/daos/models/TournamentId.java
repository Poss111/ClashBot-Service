package com.poss.clash.bot.daos.models;

import lombok.*;

@EqualsAndHashCode
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TournamentId {

    private String tournamentName;
    private String tournamentDay;

}
