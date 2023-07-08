package com.poss.clash.bot.daos.models;

import lombok.*;

import java.util.Set;

@EqualsAndHashCode
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BasePlayerRecord {

  private String discordId;
  private Set<LoLChampion> championsToPlay;
  private String name;

}
