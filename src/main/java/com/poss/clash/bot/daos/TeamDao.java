package com.poss.clash.bot.daos;

import com.poss.clash.bot.daos.models.ClashTeam;
import com.poss.clash.bot.daos.models.TeamId;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeamDao extends ReactiveCrudRepository<ClashTeam, TeamId> {

}
