package com.poss.clash.bot.services.models;

import lombok.Data;

import java.util.List;

@Data
public class RiotClashTournament {

  private int id;
  private int themeId;
  private String nameKey;
  private String nameKeySecondary;
  private List<RiotClashTournamentPhase> schedule;

}
