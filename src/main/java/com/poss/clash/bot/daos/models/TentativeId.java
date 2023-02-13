package com.poss.clash.bot.daos.models;

import lombok.*;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class  TentativeId implements Serializable {

    private String tentativeId;
    private TournamentId tournamentId;
    private Integer serverId;

}
