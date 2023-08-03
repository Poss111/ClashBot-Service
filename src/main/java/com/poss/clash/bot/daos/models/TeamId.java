package com.poss.clash.bot.daos.models;

import lombok.*;

@EqualsAndHashCode
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TeamId {

  private String id;
  private TournamentId tournamentId;

}
