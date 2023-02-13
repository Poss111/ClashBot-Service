package com.poss.clash.bot.daos.models;

import lombok.*;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TournamentId implements Serializable {

    private String tournamentName;
    private String tournamentDay;

}
