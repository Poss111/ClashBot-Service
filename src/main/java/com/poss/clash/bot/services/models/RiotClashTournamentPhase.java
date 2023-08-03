package com.poss.clash.bot.services.models;

import lombok.Data;

@Data
public class RiotClashTournamentPhase {

  private int id;
  private long registrationTime;
  private long startTime;
  private boolean cancelled;

}
