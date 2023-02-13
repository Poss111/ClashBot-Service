package com.poss.clash.bot.daos.models;

import lombok.*;

import java.io.Serializable;

@EqualsAndHashCode
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserAssociationKey implements Serializable {

    private Integer discordId;
    private TournamentId tournamentId;

}
