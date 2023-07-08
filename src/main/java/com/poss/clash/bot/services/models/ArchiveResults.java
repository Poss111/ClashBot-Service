package com.poss.clash.bot.services.models;

import com.poss.clash.bot.daos.models.ArchivedClashTeam;
import com.poss.clash.bot.daos.models.ArchivedClashTournament;
import com.poss.clash.bot.daos.models.ArchivedTentativeQueue;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ArchiveResults {

  private List<ArchivedClashTeam> teamsArchived;
  private List<ArchivedClashTournament> inactiveTournaments;
  private List<ArchivedTentativeQueue> tentativeQueues;

}
