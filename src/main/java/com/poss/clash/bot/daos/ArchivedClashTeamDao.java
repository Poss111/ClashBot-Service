package com.poss.clash.bot.daos;

import com.poss.clash.bot.daos.models.ArchivedClashTeam;
import com.poss.clash.bot.daos.models.TeamId;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface ArchivedClashTeamDao extends ReactiveCrudRepository<ArchivedClashTeam, TeamId> {

}
