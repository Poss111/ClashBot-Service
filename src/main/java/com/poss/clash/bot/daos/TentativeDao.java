package com.poss.clash.bot.daos;

import com.poss.clash.bot.daos.models.TentativeId;
import com.poss.clash.bot.daos.models.TentativeQueue;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TentativeDao extends ReactiveCrudRepository<TentativeQueue, TentativeId> {
}
