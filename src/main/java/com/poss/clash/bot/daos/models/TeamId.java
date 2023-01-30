package com.poss.clash.bot.daos.models;

import lombok.*;

@EqualsAndHashCode
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TeamId {

    private Integer teamId;
    private TournamentId tournamentId;

}
