package com.poss.clash.bot.daos.models;

import lombok.*;

@EqualsAndHashCode
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserAssociationKey {

    private Integer discordId;
    private TournamentId tournamentId;
    private Integer teamId;
    private TentativeId tentativeId;

}
