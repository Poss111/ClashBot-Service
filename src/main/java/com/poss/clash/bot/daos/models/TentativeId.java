package com.poss.clash.bot.daos.models;

import lombok.*;

@EqualsAndHashCode
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TentativeId {

    private TournamentId tournamentId;
    private Integer serverId;

}
